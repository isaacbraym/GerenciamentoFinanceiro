package com.gastos.db;

import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade CategoriaDespesa.
 */
public class CategoriaDespesaDAO {
    
    /**
     * Insere uma nova categoria no banco de dados.
     * @param categoria a categoria a ser inserida
     * @return o ID da categoria inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(CategoriaDespesa categoria) throws SQLException {
        String sql = "INSERT INTO categorias (nome) VALUES (?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Garante que autocommit está ativado para essa operação
            conn.setAutoCommit(true);
            
            System.out.println("Tentando inserir categoria: " + categoria.getNome());
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, categoria.getNome());
            int affectedRows = stmt.executeUpdate();
            
            System.out.println("Linhas afetadas na inserção: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir categoria, nenhuma linha afetada.");
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                categoria.setId(id);
                System.out.println("Categoria inserida com ID: " + id);
                return id;
            } else {
                throw new SQLException("Falha ao inserir categoria, nenhum ID foi retornado.");
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Não fechamos a conexão aqui pois ela é gerenciada pela classe ConexaoBanco
        }
    }
    
    /**
     * Atualiza uma categoria existente no banco de dados.
     * @param categoria a categoria a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(CategoriaDespesa categoria) throws SQLException {
        String sql = "UPDATE categorias SET nome = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Garante que autocommit está ativado para essa operação
            conn.setAutoCommit(true);
            
            System.out.println("Atualizando categoria ID: " + categoria.getId() + ", Nome: " + categoria.getNome());
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoria.getNome());
            stmt.setInt(2, categoria.getId());
            
            int affectedRows = stmt.executeUpdate();
            System.out.println("Linhas afetadas na atualização: " + affectedRows);
            
            if (affectedRows == 0) {
                System.out.println("AVISO: Nenhuma linha foi atualizada para o ID: " + categoria.getId());
            }
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Exclui uma categoria do banco de dados.
     * @param id o ID da categoria a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        // Primeiro excluir as subcategorias associadas
        String sqlSubcategorias = "DELETE FROM subcategorias WHERE categoria_id = ?";
        
        // Depois excluir a categoria
        String sqlCategoria = "DELETE FROM categorias WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtSub = null;
        PreparedStatement stmtCat = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            conn.setAutoCommit(false); // Iniciar transação
            
            System.out.println("Excluindo categoria ID: " + id);
            
            // Excluir subcategorias
            stmtSub = conn.prepareStatement(sqlSubcategorias);
            stmtSub.setInt(1, id);
            int subRows = stmtSub.executeUpdate();
            System.out.println("Subcategorias excluídas: " + subRows);
            
            // Excluir categoria
            stmtCat = conn.prepareStatement(sqlCategoria);
            stmtCat.setInt(1, id);
            int catRows = stmtCat.executeUpdate();
            System.out.println("Categorias excluídas: " + catRows);
            
            conn.commit(); // Confirmar transação
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Reverter em caso de erro
                    System.out.println("Transação revertida devido a erro");
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (stmtSub != null) try { stmtSub.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmtCat != null) try { stmtCat.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar autocommit
                } catch (SQLException ex) {
                    System.err.println("Erro ao restaurar autocommit: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Busca uma categoria pelo ID.
     * @param id o ID da categoria a ser buscada
     * @return a categoria encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public CategoriaDespesa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM categorias WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            rs = stmt.executeQuery();
            if (rs.next()) {
                CategoriaDespesa categoria = construirCategoria(rs);
                categoria.setSubCategorias(buscarSubcategorias(categoria.getId()));
                return categoria;
            } else {
                return null;
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Lista todas as categorias do banco de dados.
     * @return a lista de categorias
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<CategoriaDespesa> listarTodas() throws SQLException {
        List<CategoriaDespesa> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nome";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.createStatement();
            
            System.out.println("Buscando todas as categorias...");
            
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                CategoriaDespesa categoria = construirCategoria(rs);
                categoria.setSubCategorias(buscarSubcategorias(categoria.getId()));
                categorias.add(categoria);
                
                System.out.println("Categoria encontrada: " + categoria.getId() + " - " + categoria.getNome() + 
                                  " com " + categoria.getSubCategorias().size() + " subcategorias");
            }
            
            System.out.println("Total de categorias encontradas: " + categorias.size());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return categorias;
    }
    
    /**
     * Constrói um objeto CategoriaDespesa a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados da categoria
     * @return a categoria construída
     * @throws SQLException se ocorrer um erro de SQL
     */
    private CategoriaDespesa construirCategoria(ResultSet rs) throws SQLException {
        CategoriaDespesa categoria = new CategoriaDespesa();
        
        categoria.setId(rs.getInt("id"));
        categoria.setNome(rs.getString("nome"));
        
        return categoria;
    }
    
    /**
     * Busca todas as subcategorias de uma categoria específica.
     * @param categoriaId o ID da categoria
     * @return a lista de subcategorias
     * @throws SQLException se ocorrer um erro de SQL
     */
    private List<SubCategoria> buscarSubcategorias(int categoriaId) throws SQLException {
        List<SubCategoria> subcategorias = new ArrayList<>();
        String sql = "SELECT * FROM subcategorias WHERE categoria_id = ? ORDER BY nome";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, categoriaId);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                SubCategoria subcategoria = new SubCategoria();
                subcategoria.setId(rs.getInt("id"));
                subcategoria.setNome(rs.getString("nome"));
                subcategoria.setCategoriaId(rs.getInt("categoria_id"));
                
                subcategorias.add(subcategoria);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return subcategorias;
    }
    
    /**
     * Insere uma nova subcategoria no banco de dados.
     * @param subcategoria a subcategoria a ser inserida
     * @return o ID da subcategoria inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserirSubcategoria(SubCategoria subcategoria) throws SQLException {
        String sql = "INSERT INTO subcategorias (nome, categoria_id) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Garante que autocommit está ativado para essa operação
            conn.setAutoCommit(true);
            
            System.out.println("Inserindo subcategoria: " + subcategoria.getNome() + 
                              " na categoria ID: " + subcategoria.getCategoriaId());
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, subcategoria.getNome());
            stmt.setInt(2, subcategoria.getCategoriaId());
            
            int affectedRows = stmt.executeUpdate();
            System.out.println("Linhas afetadas na inserção da subcategoria: " + affectedRows);
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir subcategoria, nenhuma linha afetada.");
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                subcategoria.setId(id);
                System.out.println("Subcategoria inserida com ID: " + id);
                return id;
            } else {
                throw new SQLException("Falha ao inserir subcategoria, nenhum ID foi retornado.");
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Atualiza uma subcategoria existente no banco de dados.
     * @param subcategoria a subcategoria a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizarSubcategoria(SubCategoria subcategoria) throws SQLException {
        String sql = "UPDATE subcategorias SET nome = ?, categoria_id = ? WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Garante que autocommit está ativado para essa operação
            conn.setAutoCommit(true);
            
            System.out.println("Atualizando subcategoria ID: " + subcategoria.getId() + 
                              ", Nome: " + subcategoria.getNome());
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, subcategoria.getNome());
            stmt.setInt(2, subcategoria.getCategoriaId());
            stmt.setInt(3, subcategoria.getId());
            
            int affectedRows = stmt.executeUpdate();
            System.out.println("Linhas afetadas na atualização da subcategoria: " + affectedRows);
            
            if (affectedRows == 0) {
                System.out.println("AVISO: Nenhuma linha foi atualizada para subcategoria ID: " + subcategoria.getId());
            }
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Exclui uma subcategoria do banco de dados.
     * @param id o ID da subcategoria a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluirSubcategoria(int id) throws SQLException {
        String sql = "DELETE FROM subcategorias WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            // Garante que autocommit está ativado para essa operação
            conn.setAutoCommit(true);
            
            System.out.println("Excluindo subcategoria ID: " + id);
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            System.out.println("Linhas afetadas na exclusão da subcategoria: " + affectedRows);
            
            if (affectedRows == 0) {
                System.out.println("AVISO: Nenhuma linha foi excluída para subcategoria ID: " + id);
            }
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}