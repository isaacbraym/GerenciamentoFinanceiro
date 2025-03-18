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
    
    /**
     * Insere um novo parcelamento no banco de dados.
     * @param parcelamento o parcelamento a ser inserido
     * @return o ID do parcelamento inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Parcelamento parcelamento) throws SQLException {
        String sql = "INSERT INTO parcelamentos (valor_total, total_parcelas, parcelas_restantes, data_inicio) " +
                     "VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            
            // Validações básicas antes de inserir
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
            
            // Apenas para logging
            System.out.println("Inserindo parcelamento: " + parcelamento.getTotalParcelas() + 
                               " parcelas de R$ " + parcelamento.getValorParcela() + 
                               " (total: R$ " + parcelamento.getValorTotal() + ")");
            
            conn.setAutoCommit(false); // Iniciar transação
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                try {
                    inserirParcela(conn, parcela, parcelamentoId);
                } catch (SQLException e) {
                    System.err.println("Erro ao inserir parcela #" + parcela.getNumeroParcela() + 
                                      ": " + e.getMessage());
                    throw new SQLException("Erro ao inserir parcela #" + parcela.getNumeroParcela() + 
                                          ": " + e.getMessage(), e);
                }
            }
            
            conn.commit();
            System.out.println("Parcelamento inserido com sucesso. ID: " + parcelamentoId);
            return parcelamentoId;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Rollback executado devido a erro: " + e.getMessage());
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro detalhado ao inserir parcelamento: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao inserir parcelamento: " + e.getMessage(), e);
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
     * Insere uma nova parcela no banco de dados.
     * @param conn a conexão com o banco de dados
     * @param parcela a parcela a ser inserida
     * @param parcelamentoId o ID do parcelamento associado
     * @throws SQLException se ocorrer um erro de SQL
     */
    private void inserirParcela(Connection conn, Parcela parcela, int parcelamentoId) throws SQLException {
        String sql = "INSERT INTO parcelas (parcelamento_id, numero_parcela, valor, data_vencimento, paga) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            // Validar dados da parcela
            if (parcela.getDataVencimento() == null) {
                throw new SQLException("A data de vencimento da parcela não pode ser nula");
            }
            
            if (parcela.getValor() <= 0) {
                throw new SQLException("O valor da parcela deve ser maior que zero");
            }
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, parcelamentoId);
            stmt.setInt(2, parcela.getNumeroParcela());
            stmt.setDouble(3, parcela.getValor());
            stmt.setString(4, parcela.getDataVencimento().toString());
            stmt.setBoolean(5, parcela.isPaga());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir parcela, nenhuma linha afetada.");
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                parcela.setId(generatedKeys.getInt(1));
                System.out.println("Parcela " + parcela.getNumeroParcela() + " inserida com ID: " + parcela.getId());
            } else {
                throw new SQLException("Falha ao obter ID da parcela inserida.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir parcela: " + e.getMessage());
            throw e; // Propagando a exceção para ser tratada no método chamador
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Atualiza um parcelamento existente no banco de dados.
     * @param parcelamento o parcelamento a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(Parcelamento parcelamento) throws SQLException {
        String sql = "UPDATE parcelamentos SET valor_total = ?, total_parcelas = ?, " +
                     "parcelas_restantes = ?, data_inicio = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Validações básicas
            if (parcelamento.getId() <= 0) {
                throw new SQLException("ID do parcelamento inválido");
            }
            
            if (parcelamento.getTotalParcelas() <= 0) {
                throw new SQLException("O número de parcelas deve ser maior que zero");
            }
            
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(sql);
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
            String sqlDeleteParcelas = "DELETE FROM parcelas WHERE parcelamento_id = ?";
            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteParcelas)) {
                stmtDelete.setInt(1, parcelamento.getId());
                int deletedRows = stmtDelete.executeUpdate();
                System.out.println(deletedRows + " parcelas antigas excluídas.");
            }
            
            // Inserir novas parcelas
            for (Parcela parcela : parcelamento.getParcelas()) {
                try {
                    inserirParcela(conn, parcela, parcelamento.getId());
                } catch (SQLException e) {
                    System.err.println("Erro ao inserir parcela #" + parcela.getNumeroParcela() + 
                                      " durante atualização: " + e.getMessage());
                    throw e;
                }
            }
            
            conn.commit();
            System.out.println("Parcelamento atualizado com sucesso. ID: " + parcelamento.getId());
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Rollback executado devido a erro na atualização: " + e.getMessage());
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro detalhado ao atualizar parcelamento: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao atualizar parcelamento: " + e.getMessage(), e);
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
     * Exclui um parcelamento do banco de dados.
     * @param id o ID do parcelamento a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        // Primeiro excluir as parcelas associadas
        String sqlParcelas = "DELETE FROM parcelas WHERE parcelamento_id = ?";
        
        // Depois excluir o parcelamento
        String sqlParcelamento = "DELETE FROM parcelamentos WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtParcelas = null;
        PreparedStatement stmtParcelamento = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false);
            
            // Excluir parcelas
            stmtParcelas = conn.prepareStatement(sqlParcelas);
            stmtParcelas.setInt(1, id);
            int parcelasExcluidas = stmtParcelas.executeUpdate();
            System.out.println("Parcelas excluídas: " + parcelasExcluidas);
            
            // Excluir parcelamento
            stmtParcelamento = conn.prepareStatement(sqlParcelamento);
            stmtParcelamento.setInt(1, id);
            int parcelamentoExcluido = stmtParcelamento.executeUpdate();
            System.out.println("Parcelamento excluído: " + (parcelamentoExcluido > 0));
            
            conn.commit();
            System.out.println("Parcelamento ID " + id + " excluído com sucesso.");
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Rollback executado devido a erro na exclusão: " + e.getMessage());
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.err.println("Erro detalhado ao excluir parcelamento: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao excluir parcelamento: " + e.getMessage(), e);
        } finally {
            if (stmtParcelas != null) try { stmtParcelas.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmtParcelamento != null) try { stmtParcelamento.close(); } catch (SQLException e) { e.printStackTrace(); }
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
     * Busca um parcelamento pelo ID.
     * @param id o ID do parcelamento a ser buscado
     * @return o parcelamento encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Parcelamento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM parcelamentos WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                Parcelamento parcelamento = construirParcelamento(rs);
                parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                System.out.println("Parcelamento ID " + id + " encontrado com " + 
                                  parcelamento.getParcelas().size() + " parcelas.");
                return parcelamento;
            } else {
                System.out.println("Parcelamento ID " + id + " não encontrado.");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelamento por ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao buscar parcelamento: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Lista todos os parcelamentos do banco de dados.
     * @return a lista de parcelamentos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarTodos() throws SQLException {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        String sql = "SELECT * FROM parcelamentos ORDER BY data_inicio DESC";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Parcelamento parcelamento = construirParcelamento(rs);
                parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                parcelamentos.add(parcelamento);
            }
            
            System.out.println("Total de parcelamentos listados: " + parcelamentos.size());
            return parcelamentos;
        } catch (SQLException e) {
            System.err.println("Erro ao listar parcelamentos: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao listar parcelamentos: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Lista os parcelamentos ativos (que ainda têm parcelas a pagar).
     * @return a lista de parcelamentos ativos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarParcelamentosAtivos() throws SQLException {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        String sql = "SELECT * FROM parcelamentos WHERE parcelas_restantes > 0 ORDER BY data_inicio DESC";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Parcelamento parcelamento = construirParcelamento(rs);
                parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
                parcelamentos.add(parcelamento);
            }
            
            System.out.println("Total de parcelamentos ativos listados: " + parcelamentos.size());
            return parcelamentos;
        } catch (SQLException e) {
            System.err.println("Erro ao listar parcelamentos ativos: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao listar parcelamentos ativos: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Busca as parcelas de um parcelamento.
     * @param parcelamentoId o ID do parcelamento
     * @return a lista de parcelas do parcelamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    private List<Parcela> buscarParcelas(int parcelamentoId) throws SQLException {
        List<Parcela> parcelas = new ArrayList<>();
        String sql = "SELECT * FROM parcelas WHERE parcelamento_id = ? ORDER BY numero_parcela";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, parcelamentoId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                Parcela parcela = new Parcela();
                parcela.setId(rs.getInt("id"));
                parcela.setNumeroParcela(rs.getInt("numero_parcela"));
                parcela.setValor(rs.getDouble("valor"));
                parcela.setDataVencimento(LocalDate.parse(rs.getString("data_vencimento")));
                parcela.setPaga(rs.getBoolean("paga"));
                
                parcelas.add(parcela);
            }
            
            System.out.println("Parcelas encontradas para parcelamento ID " + parcelamentoId + ": " + parcelas.size());
            return parcelas;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelas do parcelamento ID " + parcelamentoId + ": " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao buscar parcelas: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
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
                System.out.println("Parcela ID " + parcelaId + " marcada como " + (paga ? "paga" : "não paga"));
                
                // Buscar o parcelamento_id da parcela
                int parcelamentoId = buscarParcelamentoIdDaParcela(parcelaId);
                if (parcelamentoId > 0) {
                    // Atualizar o número de parcelas restantes
                    atualizarParcelasRestantes(parcelamentoId);
                }
            } else {
                System.err.println("Parcela ID " + parcelaId + " não encontrada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao marcar parcela como paga: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao marcar parcela como paga: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca o ID do parcelamento associado a uma parcela.
     * @param parcelaId o ID da parcela
     * @return o ID do parcelamento ou -1 se não encontrado
     * @throws SQLException se ocorrer um erro de SQL
     */
    private int buscarParcelamentoIdDaParcela(int parcelaId) throws SQLException {
        String sql = "SELECT parcelamento_id FROM parcelas WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, parcelaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("parcelamento_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelamento_id da parcela: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Atualiza o número de parcelas restantes do parcelamento.
     * @param parcelamentoId o ID do parcelamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizarParcelasRestantes(int parcelamentoId) throws SQLException {
        String sql = "SELECT COUNT(*) as restantes FROM parcelas WHERE parcelamento_id = ? AND paga = 0";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(true); // Garantir que esta operação seja atômica
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, parcelamentoId);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                int parcelasRestantes = rs.getInt("restantes");
                
                // Atualiza o parcelamento
                String updateSql = "UPDATE parcelamentos SET parcelas_restantes = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, parcelasRestantes);
                    updateStmt.setInt(2, parcelamentoId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Parcelamento ID " + parcelamentoId + 
                                         " atualizado com " + parcelasRestantes + " parcelas restantes.");
                    } else {
                        System.err.println("Parcelamento ID " + parcelamentoId + " não encontrado ao atualizar parcelas restantes.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar parcelas restantes: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao atualizar parcelas restantes: " + e.getMessage(), e);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
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
                    // Armazenar o parcelamento_id para uso posterior
                    parcela.setParcelamentoId(rs.getInt("parcelamento_id"));
                    
                    parcelas.add(parcela);
                }
            }
            
            System.out.println("Parcelas a vencer no próximo mês: " + parcelas.size());
            return parcelas;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelas a vencer: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Erro ao buscar parcelas a vencer: " + e.getMessage(), e);
        }
    }
}