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
    
    /**
     * Insere um novo responsável no banco de dados.
     * @param responsavel o responsável a ser inserido
     * @return o ID do responsável inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Responsavel responsavel) throws SQLException {
        String sql = "INSERT INTO responsaveis (nome) VALUES (?)";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
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
        String sql = "UPDATE responsaveis SET nome = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
        String sql = "DELETE FROM responsaveis WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT * FROM responsaveis WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT * FROM responsaveis ORDER BY nome";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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