package com.gastos.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gastos.model.Despesa;

/**
 * Classe DAO (Data Access Object) para a entidade Despesa.
 */
public class DespesaDAO {
    
    /**
     * Insere uma nova despesa no banco de dados.
     * @param despesa a despesa a ser inserida
     * @return o ID da despesa inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Despesa despesa) throws SQLException {
        String sql = "INSERT INTO despesas (descricao, valor, data_compra, data_vencimento, pago, fixo, " +
                     "categoria_id, subcategoria_id, responsavel_id, meio_pagamento_id, cartao_id, parcelamento_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Antes de iniciar a transação, validar a referência da categoria
            if (despesa.getCategoria() == null || despesa.getCategoria().getId() <= 0) {
                throw new SQLException("É necessário informar uma categoria válida para a despesa.");
            }
            
            conn.setAutoCommit(false); // Iniciar transação
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
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
            
            if (despesa.getCategoria() != null) {
                stmt.setInt(7, despesa.getCategoria().getId());
            } else {
                // Lançamos uma exceção pois categoria é obrigatória
                throw new SQLException("É necessário informar uma categoria para a despesa.");
            }
            
            if (despesa.getSubCategoria() != null) {
                stmt.setInt(8, despesa.getSubCategoria().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            
            if (despesa.getResponsavel() != null) {
                stmt.setInt(9, despesa.getResponsavel().getId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            if (despesa.getMeioPagamento() != null) {
                stmt.setInt(10, despesa.getMeioPagamento().getId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            
            if (despesa.getCartaoCredito() != null) {
                stmt.setInt(11, despesa.getCartaoCredito().getId());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            
            // Se tiver parcelamento, inserir primeiro o parcelamento e obter o ID
            if (despesa.getParcelamento() != null) {
                ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
                int parcelamentoId = parcelamentoDAO.inserir(despesa.getParcelamento());
                despesa.getParcelamento().setId(parcelamentoId);
                stmt.setInt(12, parcelamentoId);
            } else {
                stmt.setNull(12, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir despesa, nenhuma linha afetada.");
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                conn.commit();
                return id;
            } else {
                throw new SQLException("Falha ao inserir despesa, nenhum ID foi retornado.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro ao inserir despesa: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar autocommit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Atualiza uma despesa existente no banco de dados.
     * @param despesa a despesa a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(Despesa despesa) throws SQLException {
        String sql = "UPDATE despesas SET descricao = ?, valor = ?, data_compra = ?, data_vencimento = ?, " +
                     "pago = ?, fixo = ?, categoria_id = ?, subcategoria_id = ?, responsavel_id = ?, " +
                     "meio_pagamento_id = ?, cartao_id = ?, parcelamento_id = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false); // Iniciar transação
            
            stmt = conn.prepareStatement(sql);
            
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
            
            if (despesa.getCategoria() != null) {
                stmt.setInt(7, despesa.getCategoria().getId());
            } else {
                // Lançamos uma exceção pois categoria é obrigatória
                throw new SQLException("É necessário informar uma categoria para a despesa.");
            }
            
            if (despesa.getSubCategoria() != null) {
                stmt.setInt(8, despesa.getSubCategoria().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            
            if (despesa.getResponsavel() != null) {
                stmt.setInt(9, despesa.getResponsavel().getId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            if (despesa.getMeioPagamento() != null) {
                stmt.setInt(10, despesa.getMeioPagamento().getId());
            } else {
                stmt.setNull(10, Types.INTEGER);
            }
            
            if (despesa.getCartaoCredito() != null) {
                stmt.setInt(11, despesa.getCartaoCredito().getId());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            
            // Se tem parcelamento, atualizar primeiro o parcelamento
            if (despesa.getParcelamento() != null) {
                ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
                if (despesa.getParcelamento().getId() == 0) {
                    int parcelamentoId = parcelamentoDAO.inserir(despesa.getParcelamento());
                    despesa.getParcelamento().setId(parcelamentoId);
                    stmt.setInt(12, parcelamentoId);
                } else {
                    parcelamentoDAO.atualizar(despesa.getParcelamento());
                    stmt.setInt(12, despesa.getParcelamento().getId());
                }
            } else {
                stmt.setNull(12, Types.INTEGER);
            }
            
            stmt.setInt(13, despesa.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar despesa, nenhuma linha afetada.");
            }
            
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro ao atualizar despesa: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar autocommit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Exclui uma despesa do banco de dados.
     * @param id o ID da despesa a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        String sqlBusca = "SELECT parcelamento_id FROM despesas WHERE id = ?";
        String sql = "DELETE FROM despesas WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtBusca = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false); // Iniciar transação
            
            // Primeiro verificar se a despesa tem parcelamento
            stmtBusca = conn.prepareStatement(sqlBusca);
            stmtBusca.setInt(1, id);
            rs = stmtBusca.executeQuery();
            
            Integer parcelamentoId = null;
            if (rs.next() && !rs.wasNull()) {
                parcelamentoId = rs.getInt("parcelamento_id");
            }
            
            // Excluir a despesa
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            // Se tem parcelamento, excluir também
            if (parcelamentoId != null) {
                ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
                parcelamentoDAO.excluir(parcelamentoId);
            }
            
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro ao excluir despesa: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmtBusca != null) try { stmtBusca.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar autocommit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    // O restante dos métodos permanece igual...
    // Incluindo buscarPorId, listarTodas, construirDespesa, etc.
    // Para economizar espaço, não estou incluindo esses métodos aqui,
    // já que você já os tem no código original
    
    /**
     * Busca uma despesa pelo ID.
     * @param id o ID da despesa a ser buscada
     * @return a despesa encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Despesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM despesas WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirDespesa(rs);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Lista todas as despesas do banco de dados.
     * @return a lista de despesas
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarTodas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                despesas.add(construirDespesa(rs));
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas do mês atual.
     * @return a lista de despesas do mês atual
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarDespesasDoMes() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        System.out.println("Buscando despesas entre " + inicio + " e " + fim);
        
        // Esta consulta SQL foi modificada para capturar todas as despesas 
        // relevantes para o mês atual, independentemente da data de vencimento
        String sql = "SELECT * FROM despesas WHERE " +
                     "(data_vencimento BETWEEN ? AND ?) OR " +
                     "(data_vencimento IS NULL AND data_compra BETWEEN ? AND ?) OR " +
                     "(data_compra BETWEEN ? AND ?) " +
                     "ORDER BY data_vencimento DESC, data_compra DESC";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());
            stmt.setString(4, fim.toString());
            stmt.setString(5, inicio.toString());
            stmt.setString(6, fim.toString());
            
            rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                Despesa despesa = construirDespesa(rs);
                despesas.add(despesa);
                count++;
                
                // Log para debug
                System.out.println("Despesa encontrada: ID " + despesa.getId() + 
                                  " - " + despesa.getDescricao() + 
                                  " - R$ " + despesa.getValor() + 
                                  " - Data: " + despesa.getDataCompra() +
                                  (despesa.getDataVencimento() != null ? 
                                   " - Venc.: " + despesa.getDataVencimento() : ""));
            }
            
            System.out.println("Total de despesas do mês encontradas: " + count);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas por categoria.
     * @param categoriaId o ID da categoria
     * @return a lista de despesas da categoria
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarPorCategoria(int categoriaId) throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE categoria_id = ? ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoriaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    despesas.add(construirDespesa(rs));
                }
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas por responsável.
     * @param responsavelId o ID do responsável
     * @return a lista de despesas do responsável
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarPorResponsavel(int responsavelId) throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE responsavel_id = ? ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, responsavelId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    despesas.add(construirDespesa(rs));
                }
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas por cartão de crédito.
     * @param cartaoId o ID do cartão de crédito
     * @return a lista de despesas do cartão
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarPorCartao(int cartaoId) throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE cartao_id = ? ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, cartaoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    despesas.add(construirDespesa(rs));
                }
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas fixas.
     * @return a lista de despesas fixas
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarDespesasFixas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE fixo = 1 ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                despesas.add(construirDespesa(rs));
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas parceladas.
     * @return a lista de despesas parceladas
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarDespesasParceladas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE parcelamento_id IS NOT NULL ORDER BY data_compra DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                despesas.add(construirDespesa(rs));
            }
        }
        
        return despesas;
    }
    
    /**
     * Constrói um objeto Despesa a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados da despesa
     * @return a despesa construída
     * @throws SQLException se ocorrer um erro de SQL
     */
    private Despesa construirDespesa(ResultSet rs) throws SQLException {
        Despesa despesa = new Despesa();
        
        despesa.setId(rs.getInt("id"));
        despesa.setDescricao(rs.getString("descricao"));
        despesa.setValor(rs.getDouble("valor"));
        despesa.setDataCompra(LocalDate.parse(rs.getString("data_compra")));
        
        String dataVencimento = rs.getString("data_vencimento");
        if (dataVencimento != null) {
            despesa.setDataVencimento(LocalDate.parse(dataVencimento));
        }
        
        despesa.setPago(rs.getBoolean("pago"));
        despesa.setFixo(rs.getBoolean("fixo"));
        
        // Carregar categoria
        int categoriaId = rs.getInt("categoria_id");
        if (!rs.wasNull()) {
            CategoriaDespesaDAO categoriaDAO = new CategoriaDespesaDAO();
            despesa.setCategoria(categoriaDAO.buscarPorId(categoriaId));
        }
        
        // Carregar subcategoria
        int subcategoriaId = rs.getInt("subcategoria_id");
        if (!rs.wasNull()) {
            SubCategoriaDAO subcategoriaDAO = new SubCategoriaDAO();
            despesa.setSubCategoria(subcategoriaDAO.buscarPorId(subcategoriaId));
        }
        
        // Carregar responsável
        int responsavelId = rs.getInt("responsavel_id");
        if (!rs.wasNull()) {
            ResponsavelDAO responsavelDAO = new ResponsavelDAO();
            despesa.setResponsavel(responsavelDAO.buscarPorId(responsavelId));
        }
        
        // Carregar meio de pagamento
        int meioPagamentoId = rs.getInt("meio_pagamento_id");
        if (!rs.wasNull()) {
            MeioPagamentoDAO meioPagamentoDAO = new MeioPagamentoDAO();
            despesa.setMeioPagamento(meioPagamentoDAO.buscarPorId(meioPagamentoId));
        }
        
        // Carregar cartão de crédito
        int cartaoId = rs.getInt("cartao_id");
        if (!rs.wasNull()) {
            CartaoCreditoDAO cartaoDAO = new CartaoCreditoDAO();
            despesa.setCartaoCredito(cartaoDAO.buscarPorId(cartaoId));
        }
        
        // Carregar parcelamento
        int parcelamentoId = rs.getInt("parcelamento_id");
        if (!rs.wasNull()) {
            ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
            despesa.setParcelamento(parcelamentoDAO.buscarPorId(parcelamentoId));
        }
        
        return despesa;
    }
    
    /**
     * Calcula o total de despesas do mês por categoria.
     * @return uma lista de pares (categoria, valor)
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Object[]> calcularTotalPorCategoria() throws SQLException {
        List<Object[]> totaisPorCategoria = new ArrayList<>();
        
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        String sql = "SELECT c.nome, SUM(d.valor) as total " +
                     "FROM despesas d " +
                     "JOIN categorias c ON d.categoria_id = c.id " +
                     "WHERE (d.data_vencimento BETWEEN ? AND ?) OR " +
                     "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?) " +
                     "GROUP BY d.categoria_id " +
                     "ORDER BY total DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());
            stmt.setString(4, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("nome");
                    double total = rs.getDouble("total");
                    totaisPorCategoria.add(new Object[]{categoria, total});
                }
            }
        }
        
        return totaisPorCategoria;
    }
    
    /**
     * Calcula o total de despesas do mês por responsável.
     * @return uma lista de pares (responsável, valor)
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Object[]> calcularTotalPorResponsavel() throws SQLException {
        List<Object[]> totaisPorResponsavel = new ArrayList<>();
        
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        String sql = "SELECT r.nome, SUM(d.valor) as total " +
                     "FROM despesas d " +
                     "JOIN responsaveis r ON d.responsavel_id = r.id " +
                     "WHERE (d.data_vencimento BETWEEN ? AND ?) OR " +
                     "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?) " +
                     "GROUP BY d.responsavel_id " +
                     "ORDER BY total DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());
            stmt.setString(4, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String responsavel = rs.getString("nome");
                    double total = rs.getDouble("total");
                    totaisPorResponsavel.add(new Object[]{responsavel, total});
                }
            }
        }
        
        return totaisPorResponsavel;
    }
}