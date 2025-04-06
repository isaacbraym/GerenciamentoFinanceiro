package com.gastos.db.util;

import com.gastos.db.ConexaoBanco;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Classe utilitária para operações comuns de banco de dados.
 * Reduz a duplicação de código em classes DAO.
 */
public class DAOTemplate {

    /**
     * Executa uma operação de atualização (INSERT, UPDATE, DELETE).
     * 
     * @param sql Query SQL a ser executada
     * @param params Parâmetros para a query
     * @return Número de linhas afetadas
     * @throws SQLException em caso de erro no banco de dados
     */
    public int executarUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            definirParametros(stmt, params);
            
            // Garantir commit se a conexão estiver em modo manual
            if (!conn.getAutoCommit()) conn.setAutoCommit(true);
            return stmt.executeUpdate();
        }
    }

    /**
     * Executa uma operação de INSERT e retorna a chave gerada.
     * 
     * @param sql Query SQL de inserção
     * @param params Parâmetros para a query
     * @return Chave gerada ou empty se nenhuma chave for gerada
     * @throws SQLException em caso de erro no banco de dados
     */
    public Optional<Integer> inserirEObterChave(String sql, Object... params) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            definirParametros(stmt, params);
            
            // Garantir commit
            if (!conn.getAutoCommit()) conn.setAutoCommit(true);
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return Optional.of(generatedKeys.getInt(1));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Executa uma query que retorna uma lista de objetos.
     * 
     * @param <T> Tipo do objeto retornado
     * @param sql Query SQL a ser executada
     * @param rowMapper Função para mapear ResultSet para objeto
     * @param params Parâmetros para a query
     * @return Lista de objetos do tipo T
     * @throws SQLException em caso de erro no banco de dados
     */
    public <T> List<T> listar(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        List<T> resultados = new ArrayList<>();
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            definirParametros(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(rowMapper.mapRow(rs));
                }
            }
        }
        return resultados;
    }

    /**
     * Executa uma query que retorna um único objeto.
     * 
     * @param <T> Tipo do objeto retornado
     * @param sql Query SQL a ser executada
     * @param rowMapper Função para mapear ResultSet para objeto
     * @param params Parâmetros para a query
     * @return Objeto do tipo T ou empty se nenhum resultado for encontrado
     * @throws SQLException em caso de erro no banco de dados
     */
    public <T> Optional<T> buscar(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            definirParametros(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rowMapper.mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Executa operações dentro de uma transação.
     * 
     * @param acao Função que contém as operações a serem executadas na transação
     * @throws SQLException em caso de erro no banco de dados
     */
    public void executarEmTransacao(Consumer<Connection> acao) throws SQLException {
        Connection conn = null;
        boolean autoCommitOriginal = true;
        
        try {
            conn = ConexaoBanco.getConexao();
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false); // Inicia transação

            acao.accept(conn); // Executa as operações

            conn.commit(); // Confirma a transação
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Reverte em caso de erro
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
            throw e; // Propaga a exceção original
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(autoCommitOriginal); // Restaura autoCommit
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar autocommit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Define os parâmetros em um PreparedStatement.
     * 
     * @param stmt O PreparedStatement
     * @param params Os valores dos parâmetros
     * @throws SQLException em caso de erro ao definir parâmetros
     */
    private void definirParametros(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            
            if (param == null) {
                stmt.setNull(i + 1, Types.VARCHAR);
            } else if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Double) {
                stmt.setDouble(i + 1, (Double) param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof LocalDate) {
                stmt.setString(i + 1, ((LocalDate) param).toString());
            } else {
                stmt.setObject(i + 1, param);
            }
        }
    }
}