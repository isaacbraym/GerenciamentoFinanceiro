package com.gastos.db;

import com.gastos.db.ConexaoBanco;
import com.gastos.model.Responsavel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade Responsavel.
 */
public class ResponsavelDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = "INSERT INTO responsaveis (nome) VALUES (?)";
    private static final String SQL_UPDATE = "UPDATE responsaveis SET nome = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM responsaveis WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM responsaveis WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM responsaveis ORDER BY nome";
    
    /**
     * Insere um novo responsável no banco de dados.
     * @param responsavel o responsável a ser inserido
     * @return o ID do responsável inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Responsavel responsavel) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, responsavel.getNome());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao inserir responsável, nenhum ID foi retornado.");
                }
            }
        }
    }
    
    /**
     * Atualiza um responsável existente no banco de dados.
     * @param responsavel o responsável a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(Responsavel responsavel) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            stmt.setString(1, responsavel.getNome());
            stmt.setInt(2, responsavel.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui um responsável do banco de dados.
     * @param id o ID do responsável a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Busca um responsável pelo ID.
     * @param id o ID do responsável a ser buscado
     * @return o responsável encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Responsavel buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirResponsavel(rs);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Lista todos os responsáveis do banco de dados.
     * @return a lista de responsáveis
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Responsavel> listarTodos() throws SQLException {
        List<Responsavel> responsaveis = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            
            while (rs.next()) {
                responsaveis.add(construirResponsavel(rs));
            }
        }
        
        return responsaveis;
    }
    
    /**
     * Constrói um objeto Responsavel a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados do responsável
     * @return o responsável construído
     * @throws SQLException se ocorrer um erro de SQL
     */
    private Responsavel construirResponsavel(ResultSet rs) throws SQLException {
        Responsavel responsavel = new Responsavel();
        
        responsavel.setId(rs.getInt("id"));
        responsavel.setNome(rs.getString("nome"));
        
        return responsavel;
    }
}