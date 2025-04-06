package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.CartaoCredito;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.Despesa;
import com.gastos.model.MeioPagamento;
import com.gastos.model.Parcelamento;
import com.gastos.model.Responsavel;
import com.gastos.model.SubCategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO para a entidade Despesa. Refatorada para usar DAOTemplate.
 */
public class DespesaDAO {

	// Consultas SQL
	private static final String SQL_INSERT = "INSERT INTO despesas (descricao, valor, data_compra, data_vencimento, pago, fixo, "
			+ "categoria_id, subcategoria_id, responsavel_id, meio_pagamento_id, cartao_id, parcelamento_id) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SQL_UPDATE = "UPDATE despesas SET descricao = ?, valor = ?, data_compra = ?, data_vencimento = ?, "
			+ "pago = ?, fixo = ?, categoria_id = ?, subcategoria_id = ?, responsavel_id = ?, "
			+ "meio_pagamento_id = ?, cartao_id = ?, parcelamento_id = ? WHERE id = ?";

	private static final String SQL_DELETE = "DELETE FROM despesas WHERE id = ?";
	private static final String SQL_FIND_BY_ID = "SELECT * FROM despesas WHERE id = ?";
	private static final String SQL_FIND_ALL = "SELECT * FROM despesas ORDER BY data_compra DESC";
	private static final String SQL_FIND_BY_MONTH = "SELECT * FROM despesas WHERE "
			+ "(data_compra BETWEEN ? AND ?) OR (data_vencimento BETWEEN ? AND ?) " + "ORDER BY data_compra DESC";
	private static final String SQL_FIND_BY_CATEGORIA = "SELECT * FROM despesas WHERE categoria_id = ? ORDER BY data_vencimento DESC";
	private static final String SQL_FIND_BY_RESPONSAVEL = "SELECT * FROM despesas WHERE responsavel_id = ? ORDER BY data_vencimento DESC";
	private static final String SQL_FIND_BY_CARTAO = "SELECT * FROM despesas WHERE cartao_id = ? ORDER BY data_vencimento DESC";
	private static final String SQL_FIND_FIXED = "SELECT * FROM despesas WHERE fixo = 1 ORDER BY data_vencimento DESC";
	private static final String SQL_FIND_INSTALLMENT = "SELECT * FROM despesas WHERE parcelamento_id IS NOT NULL ORDER BY data_compra DESC";
	private static final String SQL_SUM_BY_CATEGORY = "SELECT c.nome, SUM(d.valor) as total " + "FROM despesas d "
			+ "JOIN categorias c ON d.categoria_id = c.id " + "WHERE (d.data_vencimento BETWEEN ? AND ?) OR "
			+ "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?) " + "GROUP BY c.nome "
			+ "ORDER BY total DESC";
	private static final String SQL_SUM_BY_RESPONSAVEL = "SELECT r.nome, SUM(d.valor) as total " + "FROM despesas d "
			+ "JOIN responsaveis r ON d.responsavel_id = r.id " + "WHERE (d.data_vencimento BETWEEN ? AND ?) OR "
			+ "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?) " + "GROUP BY r.nome "
			+ "ORDER BY total DESC";

	private final DAOTemplate daoTemplate;
	private final RowMapper<Despesa> despesaMapper;

	/**
	 * Construtor padrão que inicializa o DAOTemplate e o RowMapper.
	 */
	public DespesaDAO() {
		this.daoTemplate = new DAOTemplate();
		this.despesaMapper = this::construirDespesa;
	}

	/**
	 * Insere uma nova despesa no banco de dados.
	 */
	public int inserir(Despesa despesa) throws SQLException {
		if (despesa.getCategoria() == null || despesa.getCategoria().getId() <= 0) {
			throw new SQLException("É necessário informar uma categoria válida para a despesa.");
		}

		// Usar uma lista para armazenar o ID gerado, já que listas são mutáveis dentro
		// de lambdas
		final List<Integer> idGerado = new ArrayList<>();

		daoTemplate.executarEmTransacao(conn -> {
			try {
				// Inserir parcelamento primeiro, se existir
				if (despesa.getParcelamento() != null) {
					ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
					int parcelamentoId = parcelamentoDAO.inserir(despesa.getParcelamento());
					despesa.getParcelamento().setId(parcelamentoId);
				}

				// Preparar statement para inserção
				PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
				preencherStatement(stmt, despesa);

				int affectedRows = stmt.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Falha ao inserir despesa, nenhuma linha afetada.");
				}

				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					idGerado.add(generatedKeys.getInt(1));
				} else {
					throw new SQLException("Falha ao inserir despesa, nenhum ID foi retornado.");
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});

		if (idGerado.isEmpty()) {
			throw new SQLException("Não foi possível obter o ID da despesa inserida");
		}

		return idGerado.get(0);
	}

	/**
	 * Atualiza uma despesa existente no banco de dados.
	 */
	public void atualizar(Despesa despesa) throws SQLException {
		daoTemplate.executarEmTransacao(conn -> {
			try {
				// Atualizar ou inserir parcelamento, se existir
				if (despesa.getParcelamento() != null) {
					ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
					if (despesa.getParcelamento().getId() == 0) {
						int parcelamentoId = parcelamentoDAO.inserir(despesa.getParcelamento());
						despesa.getParcelamento().setId(parcelamentoId);
					} else {
						parcelamentoDAO.atualizar(despesa.getParcelamento());
					}
				}

				// Preparar statement para atualização
				PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE);
				preencherStatement(stmt, despesa);
				stmt.setInt(13, despesa.getId());

				int affectedRows = stmt.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Falha ao atualizar despesa, nenhuma linha afetada.");
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Preenche um PreparedStatement com os dados da despesa.
	 */
	private void preencherStatement(PreparedStatement stmt, Despesa despesa) throws SQLException {
		stmt.setString(1, despesa.getDescricao());
		stmt.setDouble(2, despesa.getValor());
		stmt.setString(3, despesa.getDataCompra().toString());

		if (despesa.getDataVencimento() != null) {
			stmt.setString(4, despesa.getDataVencimento().toString());
		} else {
			stmt.setNull(4, Types.VARCHAR);
		}

		stmt.setBoolean(5, despesa.isPago());
		stmt.setBoolean(6, despesa.isFixo());

		// Categoria (obrigatória)
		stmt.setInt(7, despesa.getCategoria().getId());

		// Campos opcionais
		setIntOrNull(stmt, 8, despesa.getSubCategoria() != null ? despesa.getSubCategoria().getId() : null);
		setIntOrNull(stmt, 9, despesa.getResponsavel() != null ? despesa.getResponsavel().getId() : null);
		setIntOrNull(stmt, 10, despesa.getMeioPagamento() != null ? despesa.getMeioPagamento().getId() : null);
		setIntOrNull(stmt, 11, despesa.getCartaoCredito() != null ? despesa.getCartaoCredito().getId() : null);
		setIntOrNull(stmt, 12, despesa.getParcelamento() != null ? despesa.getParcelamento().getId() : null);
	}

	/**
	 * Define um valor inteiro ou null em um PreparedStatement.
	 */
	private void setIntOrNull(PreparedStatement stmt, int index, Integer value) throws SQLException {
		if (value != null) {
			stmt.setInt(index, value);
		} else {
			stmt.setNull(index, Types.INTEGER);
		}
	}

	/**
	 * Exclui uma despesa do banco de dados.
	 */
	public void excluir(int id) throws SQLException {
	    Connection conn = null;
	    try {
	        conn = ConexaoBanco.getConexao();
	        conn.setAutoCommit(false); // Inicia transação

	        // 1. Verificar se a despesa tem parcelamento e obter seu ID
	        PreparedStatement checkStmt = conn.prepareStatement("SELECT parcelamento_id FROM despesas WHERE id = ?");
	        checkStmt.setInt(1, id);
	        ResultSet rs = checkStmt.executeQuery();

	        Integer parcelamentoId = null;
	        if (rs.next()) {
	            parcelamentoId = rs.getInt("parcelamento_id");
	            if (rs.wasNull()) { // Verifica corretamente se o valor é NULL
	                parcelamentoId = null;
	            }
	        }
	        rs.close();
	        checkStmt.close();

	        // 2. Se tem parcelamento, excluir parcelas e parcelamento
	        if (parcelamentoId != null) {
	            // Excluir parcelas
	            PreparedStatement deleteParcelasStmt = conn
	                    .prepareStatement("DELETE FROM parcelas WHERE parcelamento_id = ?");
	            deleteParcelasStmt.setInt(1, parcelamentoId);
	            deleteParcelasStmt.executeUpdate();
	            deleteParcelasStmt.close();

	            // Excluir parcelamento
	            PreparedStatement deleteParcelamentoStmt = conn
	                    .prepareStatement("DELETE FROM parcelamentos WHERE id = ?");
	            deleteParcelamentoStmt.setInt(1, parcelamentoId);
	            deleteParcelamentoStmt.executeUpdate();
	            deleteParcelamentoStmt.close();
	        }

	        // 3. Excluir a despesa
	        PreparedStatement deleteStmt = conn.prepareStatement(SQL_DELETE);
	        deleteStmt.setInt(1, id);
	        deleteStmt.executeUpdate();
	        deleteStmt.close();

	        // Confirmar transação
	        conn.commit();

	    } catch (SQLException e) {
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException ex) {
	                System.err.println("Erro no rollback: " + ex.getMessage());
	            }
	        }
	        throw e;
	    } finally {
	        if (conn != null) {
	            try {
	                conn.setAutoCommit(true);
	            } catch (SQLException e) {
	                System.err.println("Erro ao restaurar autocommit: " + e.getMessage());
	            }
	        }
	    }
	}

	/**
	 * Busca uma despesa pelo ID.
	 */
	public Despesa buscarPorId(int id) throws SQLException {
		Optional<Despesa> despesa = daoTemplate.buscar(SQL_FIND_BY_ID, despesaMapper, id);
		return despesa.orElse(null);
	}

	/**
	 * Lista todas as despesas do banco de dados.
	 */
	public List<Despesa> listarTodas() throws SQLException {
		return daoTemplate.listar(SQL_FIND_ALL, despesaMapper);
	}

	/**
	 * Lista despesas do mês atual.
	 */
	public List<Despesa> listarDespesasDoMes() throws SQLException {
		// Definir o período do mês atual
		LocalDate inicio = LocalDate.now().withDayOfMonth(1);
		LocalDate fim = inicio.plusMonths(1).minusDays(1);

		return daoTemplate.listar(SQL_FIND_BY_MONTH, despesaMapper, inicio.toString(), fim.toString(),
				inicio.toString(), fim.toString());
	}

	/**
	 * Lista despesas por categoria.
	 */
	public List<Despesa> listarPorCategoria(int categoriaId) throws SQLException {
		return daoTemplate.listar(SQL_FIND_BY_CATEGORIA, despesaMapper, categoriaId);
	}

	/**
	 * Lista despesas por responsável.
	 */
	public List<Despesa> listarPorResponsavel(int responsavelId) throws SQLException {
		return daoTemplate.listar(SQL_FIND_BY_RESPONSAVEL, despesaMapper, responsavelId);
	}

	/**
	 * Lista despesas por cartão de crédito.
	 */
	public List<Despesa> listarPorCartao(int cartaoId) throws SQLException {
		return daoTemplate.listar(SQL_FIND_BY_CARTAO, despesaMapper, cartaoId);
	}

	/**
	 * Lista despesas fixas.
	 */
	public List<Despesa> listarDespesasFixas() throws SQLException {
		return daoTemplate.listar(SQL_FIND_FIXED, despesaMapper);
	}

	/**
	 * Lista despesas parceladas.
	 */
	public List<Despesa> listarDespesasParceladas() throws SQLException {
		return daoTemplate.listar(SQL_FIND_INSTALLMENT, despesaMapper);
	}

	/**
	 * Constrói um objeto Despesa a partir de um ResultSet.
	 */
	private Despesa construirDespesa(ResultSet rs) throws SQLException {
		Despesa despesa = new Despesa();

		despesa.setId(rs.getInt("id"));
		despesa.setDescricao(rs.getString("descricao"));
		despesa.setValor(rs.getDouble("valor"));

		String dataCompraStr = rs.getString("data_compra");
		despesa.setDataCompra(
				dataCompraStr != null && !dataCompraStr.isEmpty() ? LocalDate.parse(dataCompraStr) : LocalDate.now());

		String dataVencimentoStr = rs.getString("data_vencimento");
		if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
			try {
				despesa.setDataVencimento(LocalDate.parse(dataVencimentoStr));
			} catch (Exception e) {
				// Manter como null em caso de erro de parsing
			}
		}

		despesa.setPago(rs.getBoolean("pago"));
		despesa.setFixo(rs.getBoolean("fixo"));

		// Carregar objetos relacionados
		carregarObjetosRelacionados(despesa, rs);

		return despesa;
	}

	/**
	 * Carrega os objetos relacionados a uma despesa.
	 */
	private void carregarObjetosRelacionados(Despesa despesa, ResultSet rs) throws SQLException {
		// Categoria
		int categoriaId = rs.getInt("categoria_id");
		if (!rs.wasNull()) {
			try {
				CategoriaDespesaDAO categoriaDAO = new CategoriaDespesaDAO();
				despesa.setCategoria(categoriaDAO.buscarPorId(categoriaId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Subcategoria
		int subcategoriaId = rs.getInt("subcategoria_id");
		if (!rs.wasNull()) {
			try {
				SubCategoriaDAO subcategoriaDAO = new SubCategoriaDAO();
				despesa.setSubCategoria(subcategoriaDAO.buscarPorId(subcategoriaId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Responsável
		int responsavelId = rs.getInt("responsavel_id");
		if (!rs.wasNull()) {
			try {
				ResponsavelDAO responsavelDAO = new ResponsavelDAO();
				despesa.setResponsavel(responsavelDAO.buscarPorId(responsavelId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Meio de Pagamento
		int meioPagamentoId = rs.getInt("meio_pagamento_id");
		if (!rs.wasNull()) {
			try {
				MeioPagamentoDAO meioPagamentoDAO = new MeioPagamentoDAO();
				despesa.setMeioPagamento(meioPagamentoDAO.buscarPorId(meioPagamentoId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Cartão de Crédito
		int cartaoId = rs.getInt("cartao_id");
		if (!rs.wasNull()) {
			try {
				CartaoCreditoDAO cartaoDAO = new CartaoCreditoDAO();
				despesa.setCartaoCredito(cartaoDAO.buscarPorId(cartaoId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Parcelamento
		int parcelamentoId = rs.getInt("parcelamento_id");
		if (!rs.wasNull()) {
			try {
				ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
				despesa.setParcelamento(parcelamentoDAO.buscarPorId(parcelamentoId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * RowMapper para objetos Object[] contendo nome e total.
	 */
	private final RowMapper<Object[]> totalRowMapper = rs -> new Object[] { rs.getString(1), rs.getDouble("total") };

	/**
	 * Calcula o total de despesas do mês por categoria.
	 */
	public List<Object[]> calcularTotalPorCategoria() throws SQLException {
		LocalDate inicio = LocalDate.now().withDayOfMonth(1);
		LocalDate fim = inicio.plusMonths(1).minusDays(1);

		return daoTemplate.listar(SQL_SUM_BY_CATEGORY, totalRowMapper, inicio.toString(), fim.toString(),
				inicio.toString(), fim.toString());
	}

	/**
	 * Calcula o total de despesas do mês por responsável.
	 */
	public List<Object[]> calcularTotalPorResponsavel() throws SQLException {
		LocalDate inicio = LocalDate.now().withDayOfMonth(1);
		LocalDate fim = inicio.plusMonths(1).minusDays(1);

		return daoTemplate.listar(SQL_SUM_BY_RESPONSAVEL, totalRowMapper, inicio.toString(), fim.toString(),
				inicio.toString(), fim.toString());
	}
}