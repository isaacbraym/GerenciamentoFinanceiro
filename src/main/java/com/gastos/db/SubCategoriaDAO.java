package com.gastos.db;

import com.gastos.model.SubCategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade SubCategoria.
 */
public class SubCategoriaDAO {
    
    /**
     * Insere uma nova subcategoria no banco de dados.
     * @param subCategoria a subcategoria a ser inserida
     * @return o ID da subcategoria inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(SubCategoria subCategoria) throws SQLException {
        String sql = "INSERT INTO subcategorias (nome, categoria_id) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, subCategoria.getNome());
            stmt.setInt(2, subCategoria.getCategoriaId());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao inserir subcategoria, nenhum ID foi retornado.");
                }
            }
        }
    }
    
    /**
     * Atualiza uma subcategoria existente no banco de dados.
     * @param subCategoria a subcategoria a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(SubCategoria subCategoria) throws SQLException {
        String sql = "UPDATE subcategorias SET nome = ?, categoria_id = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subCategoria.getNome());
            stmt.setInt(2, subCategoria.getCategoriaId());
            stmt.setInt(3, subCategoria.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui uma subcategoria do banco de dados.
     * @param id o ID da subcategoria a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM subcategorias WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Busca uma subcategoria pelo ID.
     * @param id o ID da subcategoria a ser buscada
     * @return a subcategoria encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public SubCategoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM subcategorias WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirSubCategoria(rs);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Lista todas as subcategorias do banco de dados.
     * @return a lista de subcategorias
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<SubCategoria> listarTodas() throws SQLException {
        List<SubCategoria> subCategorias = new ArrayList<>();
        String sql = "SELECT * FROM subcategorias ORDER BY nome";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                subCategorias.add(construirSubCategoria(rs));
            }
        }
        
        return subCategorias;
    }
    
    /**
     * Lista todas as subcategorias de uma categoria específica.
     * @param categoriaId o ID da categoria
     * @return a lista de subcategorias da categoria
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<SubCategoria> listarPorCategoria(int categoriaId) throws SQLException {
        List<SubCategoria> subCategorias = new ArrayList<>();
        String sql = "SELECT * FROM subcategorias WHERE categoria_id = ? ORDER BY nome";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoriaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    subCategorias.add(construirSubCategoria(rs));
                }
            }
        }
        
        return subCategorias;
    }
    
    /**
     * Constrói um objeto SubCategoria a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados da subcategoria
     * @return a subcategoria construída
     * @throws SQLException se ocorrer um erro de SQL
     */
    private SubCategoria construirSubCategoria(ResultSet rs) throws SQLException {
        SubCategoria subCategoria = new SubCategoria();
        
        subCategoria.setId(rs.getInt("id"));
        subCategoria.setNome(rs.getString("nome"));
        subCategoria.setCategoriaId(rs.getInt("categoria_id"));
        
        return subCategoria;
    }
}