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
 * Refatorada para melhorar organização e reduzir duplicação de código.
 */
public class DespesaDAO {
    
    // Consultas SQL
    private static final String SQL_INSERT = 
            "INSERT INTO despesas (descricao, valor, data_compra, data_vencimento, pago, fixo, " +
            "categoria_id, subcategoria_id, responsavel_id, meio_pagamento_id, cartao_id, parcelamento_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE = 
            "UPDATE despesas SET descricao = ?, valor = ?, data_compra = ?, data_vencimento = ?, " +
            "pago = ?, fixo = ?, categoria_id = ?, subcategoria_id = ?, responsavel_id = ?, " +
            "meio_pagamento_id = ?, cartao_id = ?, parcelamento_id = ? WHERE id = ?";
    
    private static final String SQL_DELETE = "DELETE FROM despesas WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM despesas WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM despesas ORDER BY data_compra DESC";
    private static final String SQL_FIND_BY_MONTH = 
            "SELECT * FROM despesas WHERE " +
            "(data_compra BETWEEN ? AND ?) OR (data_vencimento BETWEEN ? AND ?) " +
            "ORDER BY data_compra DESC";
    private static final String SQL_FIXED_EXPENSES = 
            "SELECT * FROM despesas WHERE fixo = 1 ORDER BY data_vencimento DESC";
    private static final String SQL_INSTALLMENT_EXPENSES = 
            "SELECT * FROM despesas WHERE parcelamento_id IS NOT NULL ORDER BY data_compra DESC";
    
    /**
     * Insere uma nova despesa no banco de dados.
     * @param despesa a despesa a ser inserida
     * @return o ID da despesa inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Despesa despesa) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            
            // Validar categoria obrigatória
            if (despesa.getCategoria() == null || despesa.getCategoria().getId() <= 0) {
                throw new SQLException("É necessário informar uma categoria válida para a despesa.");
            }
            
            conn.setAutoCommit(false); // Iniciar transação
            
            // Inserir parcelamento primeiro, se existir
            if (despesa.getParcelamento() != null) {
                ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
                int parcelamentoId = parcelamentoDAO.inserir(despesa.getParcelamento());
                despesa.getParcelamento().setId(parcelamentoId);
            }
            
            // Preparar statement para inserção
            stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            preencherStatement(stmt, despesa);
            
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
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            fecharRecursos(generatedKeys, stmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
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
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false); // Iniciar transação
            
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
            stmt = conn.prepareStatement(SQL_UPDATE);
            preencherStatement(stmt, despesa);
            stmt.setInt(13, despesa.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar despesa, nenhuma linha afetada.");
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            fecharRecursos(null, stmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Preenche um PreparedStatement com os dados da despesa.
     * @param stmt o PreparedStatement a ser preenchido
     * @param despesa a despesa com os dados
     * @throws SQLException se ocorrer um erro de SQL
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
        
        // Subcategoria (opcional)
        setIntOrNull(stmt, 8, despesa.getSubCategoria() != null ? despesa.getSubCategoria().getId() : null);
        
        // Responsável (opcional)
        setIntOrNull(stmt, 9, despesa.getResponsavel() != null ? despesa.getResponsavel().getId() : null);
        
        // Meio de pagamento (opcional)
        setIntOrNull(stmt, 10, despesa.getMeioPagamento() != null ? despesa.getMeioPagamento().getId() : null);
        
        // Cartão de crédito (opcional)
        setIntOrNull(stmt, 11, despesa.getCartaoCredito() != null ? despesa.getCartaoCredito().getId() : null);
        
        // Parcelamento (opcional)
        setIntOrNull(stmt, 12, despesa.getParcelamento() != null ? despesa.getParcelamento().getId() : null);
    }
    
    /**
     * Define um valor inteiro ou null em um PreparedStatement.
     * @param stmt o PreparedStatement
     * @param index o índice do parâmetro
     * @param value o valor a ser definido (pode ser null)
     * @throws SQLException se ocorrer um erro de SQL
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
     * @param id o ID da despesa a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtBusca = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false); // Iniciar transação
            
            // Verificar se a despesa tem parcelamento
            stmtBusca = conn.prepareStatement("SELECT parcelamento_id FROM despesas WHERE id = ?");
            stmtBusca.setInt(1, id);
            rs = stmtBusca.executeQuery();
            
            Integer parcelamentoId = null;
            if (rs.next() && !rs.wasNull()) {
                parcelamentoId = rs.getInt("parcelamento_id");
            }
            
            // Excluir a despesa
            stmt = conn.prepareStatement(SQL_DELETE);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            // Se tem parcelamento, excluir também
            if (parcelamentoId != null) {
                ParcelamentoDAO parcelamentoDAO = new ParcelamentoDAO();
                parcelamentoDAO.excluir(parcelamentoId);
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            fecharRecursos(rs, stmtBusca);
            fecharRecursos(null, stmt);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Busca uma despesa pelo ID.
     * @param id o ID da despesa a ser buscada
     * @return a despesa encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Despesa buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirDespesa(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Lista todas as despesas do banco de dados.
     * @return a lista de despesas
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarTodas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            
            while (rs.next()) {
                try {
                    despesas.add(construirDespesa(rs));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        
        // Definir o período do mês atual
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_MONTH)) {
            
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());
            stmt.setString(4, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        despesas.add(construirDespesa(rs));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return despesas;
    }
    
    /**
     * Lista despesas por um campo específico.
     * @param campo o nome do campo
     * @param valor o valor do campo
     * @return a lista de despesas que correspondem ao critério
     * @throws SQLException se ocorrer um erro de SQL
     */
    private List<Despesa> listarPorCampo(String campo, int valor) throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesas WHERE " + campo + " = ? ORDER BY data_vencimento DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, valor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    despesas.add(construirDespesa(rs));
                }
            }
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
        return listarPorCampo("categoria_id", categoriaId);
    }
    
    /**
     * Lista despesas por responsável.
     * @param responsavelId o ID do responsável
     * @return a lista de despesas do responsável
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarPorResponsavel(int responsavelId) throws SQLException {
        return listarPorCampo("responsavel_id", responsavelId);
    }
    
    /**
     * Lista despesas por cartão de crédito.
     * @param cartaoId o ID do cartão de crédito
     * @return a lista de despesas do cartão
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarPorCartao(int cartaoId) throws SQLException {
        return listarPorCampo("cartao_id", cartaoId);
    }
    
    /**
     * Lista despesas fixas.
     * @return a lista de despesas fixas
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Despesa> listarDespesasFixas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIXED_EXPENSES)) {
            
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
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_INSTALLMENT_EXPENSES)) {
            
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
        
        // Data de compra
        String dataCompraStr = rs.getString("data_compra");
        if (dataCompraStr != null && !dataCompraStr.isEmpty()) {
            try {
                despesa.setDataCompra(LocalDate.parse(dataCompraStr));
            } catch (Exception e) {
                despesa.setDataCompra(LocalDate.now());
            }
        } else {
            despesa.setDataCompra(LocalDate.now());
        }
        
        // Data de vencimento (opcional)
        String dataVencimentoStr = rs.getString("data_vencimento");
        if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
            try {
                despesa.setDataVencimento(LocalDate.parse(dataVencimentoStr));
            } catch (Exception e) {
                // Deixar como null
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
     * @param despesa a despesa a ser carregada
     * @param rs o ResultSet com os dados
     * @throws SQLException se ocorrer um erro de SQL
     */
    private void carregarObjetosRelacionados(Despesa despesa, ResultSet rs) throws SQLException {
        // Carregar categoria
        int categoriaId = rs.getInt("categoria_id");
        if (!rs.wasNull()) {
            try {
                CategoriaDespesaDAO categoriaDAO = new CategoriaDespesaDAO();
                despesa.setCategoria(categoriaDAO.buscarPorId(categoriaId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Carregar subcategoria
        int subcategoriaId = rs.getInt("subcategoria_id");
        if (!rs.wasNull()) {
            try {
                SubCategoriaDAO subcategoriaDAO = new SubCategoriaDAO();
                despesa.setSubCategoria(subcategoriaDAO.buscarPorId(subcategoriaId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Carregar responsável
        int responsavelId = rs.getInt("responsavel_id");
        if (!rs.wasNull()) {
            try {
                ResponsavelDAO responsavelDAO = new ResponsavelDAO();
                despesa.setResponsavel(responsavelDAO.buscarPorId(responsavelId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Carregar meio de pagamento
        int meioPagamentoId = rs.getInt("meio_pagamento_id");
        if (!rs.wasNull()) {
            try {
                MeioPagamentoDAO meioPagamentoDAO = new MeioPagamentoDAO();
                despesa.setMeioPagamento(meioPagamentoDAO.buscarPorId(meioPagamentoId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Carregar cartão de crédito
        int cartaoId = rs.getInt("cartao_id");
        if (!rs.wasNull()) {
            try {
                CartaoCreditoDAO cartaoDAO = new CartaoCreditoDAO();
                despesa.setCartaoCredito(cartaoDAO.buscarPorId(cartaoId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Carregar parcelamento
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
     * Calcula o total por um determinado agrupamento.
     * @param campoNome o campo de nome a ser usado no SELECT
     * @param campoAgrupamento o campo para agrupar os resultados
     * @param campoJoin o campo de join
     * @param tabela a tabela para join
     * @param alias o alias da tabela
     * @return uma lista de pares (nome, valor)
     * @throws SQLException se ocorrer um erro de SQL
     */
    private List<Object[]> calcularTotalAgrupado(String campoNome, String campoAgrupamento, 
                                                String campoJoin, String tabela, String alias) throws SQLException {
        List<Object[]> totais = new ArrayList<>();
        
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        String sql = "SELECT " + campoNome + ", SUM(d.valor) as total " +
                     "FROM despesas d " +
                     "JOIN " + tabela + " " + alias + " ON " + campoAgrupamento + " = " + campoJoin + " " +
                     "WHERE (d.data_vencimento BETWEEN ? AND ?) OR " +
                     "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?) " +
                     "GROUP BY " + campoAgrupamento + " " +
                     "ORDER BY total DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inicio.toString());
            stmt.setString(2, fim.toString());
            stmt.setString(3, inicio.toString());
            stmt.setString(4, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nome = rs.getString(1);
                    double total = rs.getDouble("total");
                    totais.add(new Object[]{nome, total});
                }
            }
        }
        
        return totais;
    }
    
    /**
     * Calcula o total de despesas do mês por categoria.
     * @return uma lista de pares (categoria, valor)
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Object[]> calcularTotalPorCategoria() throws SQLException {
        return calcularTotalAgrupado("c.nome", "d.categoria_id", "c.id", "categorias", "c");
    }
    
    /**
     * Calcula o total de despesas do mês por responsável.
     * @return uma lista de pares (responsável, valor)
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Object[]> calcularTotalPorResponsavel() throws SQLException {
        return calcularTotalAgrupado("r.nome", "d.responsavel_id", "r.id", "responsaveis", "r");
    }
    
    /**
     * Fecha recursos JDBC de forma segura.
     * @param rs o ResultSet a ser fechado (pode ser null)
     * @param stmt o Statement a ser fechado (pode ser null)
     */
    private void fecharRecursos(ResultSet rs, Statement stmt) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}