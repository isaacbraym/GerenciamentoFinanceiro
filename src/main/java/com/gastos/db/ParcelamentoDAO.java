package com.gastos.db;

import com.gastos.model.Parcelamento;
import com.gastos.model.Parcelamento.Parcela;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade Parcelamento.
 */
public class ParcelamentoDAO {
    
    // Consultas SQL comuns
    private static final String SQL_INSERT_PARCELAMENTO = 
        "INSERT INTO parcelamentos (valor_total, total_parcelas, parcelas_restantes, data_inicio) VALUES (?, ?, ?, ?)";
    
    private static final String SQL_INSERT_PARCELA = 
        "INSERT INTO parcelas (parcelamento_id, numero_parcela, valor, data_vencimento, paga) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE_PARCELAMENTO = 
        "UPDATE parcelamentos SET valor_total = ?, total_parcelas = ?, parcelas_restantes = ?, data_inicio = ? WHERE id = ?";
    
    private static final String SQL_DELETE_PARCELAS = 
        "DELETE FROM parcelas WHERE parcelamento_id = ?";
    
    private static final String SQL_DELETE_PARCELAMENTO = 
        "DELETE FROM parcelamentos WHERE id = ?";
    
    private static final String SQL_FIND_BY_ID = 
        "SELECT * FROM parcelamentos WHERE id = ?";
    
    private static final String SQL_FIND_ALL = 
        "SELECT * FROM parcelamentos ORDER BY data_inicio DESC";
    
    private static final String SQL_FIND_PARCELAS_BY_PARCELAMENTO = 
        "SELECT * FROM parcelas WHERE parcelamento_id = ? ORDER BY numero_parcela";
    
    /**
     * Insere um novo parcelamento no banco de dados.
     * @param parcelamento o parcelamento a ser inserido
     * @return o ID do parcelamento inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Parcelamento parcelamento) throws SQLException {
        validarParcelamento(parcelamento);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        boolean originalAutoCommit = true;
        
        try {
            conn = ConexaoBanco.getConexao();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            // Inserir parcelamento
            stmt = conn.prepareStatement(SQL_INSERT_PARCELAMENTO, Statement.RETURN_GENERATED_KEYS);
            stmt.setDouble(1, parcelamento.getValorTotal());
            stmt.setInt(2, parcelamento.getTotalParcelas());
            stmt.setInt(3, parcelamento.getParcelasRestantes());
            stmt.setString(4, parcelamento.getDataInicio().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir parcelamento, nenhuma linha afetada.");
            }
            
            int parcelamentoId;
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                parcelamentoId = generatedKeys.getInt(1);
                parcelamento.setId(parcelamentoId);
            } else {
                throw new SQLException("Falha ao inserir parcelamento, nenhum ID foi retornado.");
            }
            
            // Inserir as parcelas
            for (Parcela parcela : parcelamento.getParcelas()) {
                inserirParcela(conn, parcela, parcelamentoId);
            }
            
            conn.commit();
            return parcelamentoId;
        } catch (SQLException e) {
            if (conn != null && originalAutoCommit) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Erro ao inserir parcelamento: " + e.getMessage(), e);
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            
            if (conn != null && originalAutoCommit) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Valida os dados do parcelamento antes de inserir/atualizar.
     * @param parcelamento o parcelamento a ser validado
     * @throws SQLException se o parcelamento for inválido
     */
    private void validarParcelamento(Parcelamento parcelamento) throws SQLException {
        if (parcelamento.getTotalParcelas() <= 0) {
            throw new SQLException("O número de parcelas deve ser maior que zero");
        }
        
        if (parcelamento.getValorTotal() <= 0) {
            throw new SQLException("O valor total deve ser maior que zero");
        }
        
        if (parcelamento.getDataInicio() == null) {
            throw new SQLException("A data de início do parcelamento não pode ser nula");
        }
        
        if (parcelamento.getParcelas() == null || parcelamento.getParcelas().isEmpty()) {
            throw new SQLException("O parcelamento deve ter parcelas geradas antes de ser salvo");
        }
    }
    
    /**
     * Insere uma nova parcela no banco de dados.
     * @param conn a conexão com o banco de dados
     * @param parcela a parcela a ser inserida
     * @param parcelamentoId o ID do parcelamento associado
     * @throws SQLException se ocorrer um erro de SQL
     */
    private void inserirParcela(Connection conn, Parcela parcela, int parcelamentoId) throws SQLException {
        // Validar dados da parcela
        if (parcela.getDataVencimento() == null) {
            throw new SQLException("A data de vencimento da parcela não pode ser nula");
        }
        
        if (parcela.getValor() <= 0) {
            throw new SQLException("O valor da parcela deve ser maior que zero");
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_PARCELA, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, parcelamentoId);
            stmt.setInt(2, parcela.getNumeroParcela());
            stmt.setDouble(3, parcela.getValor());
            stmt.setString(4, parcela.getDataVencimento().toString());
            stmt.setBoolean(5, parcela.isPaga());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir parcela, nenhuma linha afetada.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    parcela.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter ID da parcela inserida.");
                }
            }
        }
    }
    
    /**
     * Atualiza um parcelamento existente no banco de dados.
     * @param parcelamento o parcelamento a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(Parcelamento parcelamento) throws SQLException {
        validarParcelamento(parcelamento);
        
        if (parcelamento.getId() <= 0) {
            throw new SQLException("ID do parcelamento inválido");
        }
        
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean originalAutoCommit = true;
        
        try {
            conn = ConexaoBanco.getConexao();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            // Atualizar parcelamento
            stmt = conn.prepareStatement(SQL_UPDATE_PARCELAMENTO);
            stmt.setDouble(1, parcelamento.getValorTotal());
            stmt.setInt(2, parcelamento.getTotalParcelas());
            stmt.setInt(3, parcelamento.getParcelasRestantes());
            stmt.setString(4, parcelamento.getDataInicio().toString());
            stmt.setInt(5, parcelamento.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao atualizar parcelamento, nenhuma linha afetada.");
            }
            
            // Excluir parcelas antigas
            try (PreparedStatement stmtDelete = conn.prepareStatement(SQL_DELETE_PARCELAS)) {
                stmtDelete.setInt(1, parcelamento.getId());
                stmtDelete.executeUpdate();
            }
            
            // Inserir novas parcelas
            for (Parcela parcela : parcelamento.getParcelas()) {
                inserirParcela(conn, parcela, parcelamento.getId());
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null && originalAutoCommit) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Erro ao atualizar parcelamento: " + e.getMessage(), e);
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            
            if (conn != null && originalAutoCommit) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Exclui um parcelamento do banco de dados.
     * @param id o ID do parcelamento a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        
        try {
            conn = ConexaoBanco.getConexao();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            // Excluir parcelas
            try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PARCELAS)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // Excluir parcelamento
            try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PARCELAMENTO)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null && originalAutoCommit) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new SQLException("Erro ao excluir parcelamento: " + e.getMessage(), e);
        } finally {
            if (conn != null && originalAutoCommit) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Busca um parcelamento pelo ID.
     * @param id o ID do parcelamento a ser buscado
     * @return o parcelamento encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Parcelamento buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Parcelamento parcelamento = construirParcelamento(rs);
                    parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                    return parcelamento;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lista todos os parcelamentos do banco de dados.
     * @return a lista de parcelamentos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarTodos() throws SQLException {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            
            while (rs.next()) {
                Parcelamento parcelamento = construirParcelamento(rs);
                parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                parcelamentos.add(parcelamento);
            }
        }
        
        return parcelamentos;
    }
    
    /**
     * Lista os parcelamentos ativos (que ainda têm parcelas a pagar).
     * @return a lista de parcelamentos ativos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarParcelamentosAtivos() throws SQLException {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        String sql = "SELECT * FROM parcelamentos WHERE parcelas_restantes > 0 ORDER BY data_inicio DESC";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Parcelamento parcelamento = construirParcelamento(rs);
                parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                parcelamentos.add(parcelamento);
            }
        }
        
        return parcelamentos;
    }
    
    /**
     * Busca as parcelas de um parcelamento.
     * @param parcelamentoId o ID do parcelamento
     * @return a lista de parcelas do parcelamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    private List<Parcela> buscarParcelas(int parcelamentoId) throws SQLException {
        List<Parcela> parcelas = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_PARCELAS_BY_PARCELAMENTO)) {
            
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parcela parcela = new Parcela();
                    parcela.setId(rs.getInt("id"));
                    parcela.setNumeroParcela(rs.getInt("numero_parcela"));
                    parcela.setValor(rs.getDouble("valor"));
                    parcela.setDataVencimento(LocalDate.parse(rs.getString("data_vencimento")));
                    parcela.setPaga(rs.getBoolean("paga"));
                    
                    parcelas.add(parcela);
                }
            }
        }
        
        return parcelas;
    }
    
    /**
     * Constrói um objeto Parcelamento a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados do parcelamento
     * @return o parcelamento construído
     * @throws SQLException se ocorrer um erro de SQL
     */
    private Parcelamento construirParcelamento(ResultSet rs) throws SQLException {
        Parcelamento parcelamento = new Parcelamento();
        
        parcelamento.setId(rs.getInt("id"));
        parcelamento.setValorTotal(rs.getDouble("valor_total"));
        parcelamento.setTotalParcelas(rs.getInt("total_parcelas"));
        parcelamento.setParcelasRestantes(rs.getInt("parcelas_restantes"));
        parcelamento.setDataInicio(LocalDate.parse(rs.getString("data_inicio")));
        
        return parcelamento;
    }
    
    /**
     * Atualiza o status de pagamento de uma parcela.
     * @param parcelaId o ID da parcela
     * @param paga o novo status de pagamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void marcarParcelaPaga(int parcelaId, boolean paga) throws SQLException {
        String sql = "UPDATE parcelas SET paga = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, paga);
            stmt.setInt(2, parcelaId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Buscar o parcelamento_id da parcela
                int parcelamentoId = buscarParcelamentoIdDaParcela(parcelaId);
                if (parcelamentoId > 0) {
                    // Atualizar o número de parcelas restantes
                    atualizarParcelasRestantes(parcelamentoId);
                }
            }
        }
    }
    
    /**
     * Busca o ID do parcelamento associado a uma parcela.
     * @param parcelaId o ID da parcela
     * @return o ID do parcelamento ou -1 se não encontrado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int buscarParcelamentoIdDaParcela(int parcelaId) throws SQLException {
        String sql = "SELECT parcelamento_id FROM parcelas WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parcelaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("parcelamento_id");
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Atualiza o número de parcelas restantes do parcelamento.
     * @param parcelamentoId o ID do parcelamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizarParcelasRestantes(int parcelamentoId) throws SQLException {
        String sqlCount = "SELECT COUNT(*) as restantes FROM parcelas WHERE parcelamento_id = ? AND paga = 0";
        String sqlUpdate = "UPDATE parcelamentos SET parcelas_restantes = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            conn.setAutoCommit(true);
            
            // Contar parcelas não pagas
            int parcelasRestantes = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlCount)) {
                stmt.setInt(1, parcelamentoId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        parcelasRestantes = rs.getInt("restantes");
                    }
                }
            }
            
            // Atualizar o parcelamento
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setInt(1, parcelasRestantes);
                stmt.setInt(2, parcelamentoId);
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Busca as parcelas a vencer no próximo mês.
     * @return a lista de parcelas a vencer
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcela> buscarParcelasAVencer() throws SQLException {
        List<Parcela> parcelas = new ArrayList<>();
        
        LocalDate hoje = LocalDate.now();
        LocalDate inicioProximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        LocalDate fimProximoMes = inicioProximoMes.plusMonths(1).minusDays(1);
        
        String sql = "SELECT p.*, par.id as parcelamento_id FROM parcelas p " +
                     "JOIN parcelamentos par ON p.parcelamento_id = par.id " +
                     "WHERE p.paga = 0 AND p.data_vencimento BETWEEN ? AND ? " +
                     "ORDER BY p.data_vencimento";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inicioProximoMes.toString());
            stmt.setString(2, fimProximoMes.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parcela parcela = new Parcela();
                    parcela.setId(rs.getInt("id"));
                    parcela.setNumeroParcela(rs.getInt("numero_parcela"));
                    parcela.setValor(rs.getDouble("valor"));
                    parcela.setDataVencimento(LocalDate.parse(rs.getString("data_vencimento")));
                    parcela.setPaga(rs.getBoolean("paga"));
                    parcela.setParcelamentoId(rs.getInt("parcelamento_id"));
                    
                    parcelas.add(parcela);
                }
            }
        }
        
        return parcelas;
    }
}