package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.SubCategoria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade SubCategoria.
 * Refatorada para usar DAOTemplate.
 */
public class SubCategoriaDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = "INSERT INTO subcategorias (nome, categoria_id) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE subcategorias SET nome = ?, categoria_id = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM subcategorias WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM subcategorias WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM subcategorias ORDER BY nome";
    private static final String SQL_FIND_BY_CATEGORIA = "SELECT * FROM subcategorias WHERE categoria_id = ? ORDER BY nome";
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<SubCategoria> rowMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e o RowMapper.
     */
    public SubCategoriaDAO() {
        this.daoTemplate = new DAOTemplate();
        this.rowMapper = this::construirSubCategoria;
    }
    
    /**
     * Insere uma nova subcategoria no banco de dados.
     * @param subCategoria a subcategoria a ser inserida
     * @return o ID da subcategoria inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(SubCategoria subCategoria) throws SQLException {
        Optional<Integer> id = daoTemplate.inserirEObterChave(
            SQL_INSERT, 
            subCategoria.getNome(), 
            subCategoria.getCategoriaId()
        );
        
        return id.orElseThrow(() -> new SQLException("Falha ao inserir subcategoria, nenhum ID foi retornado."));
    }
    
    /**
     * Atualiza uma subcategoria existente no banco de dados.
     * @param subCategoria a subcategoria a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(SubCategoria subCategoria) throws SQLException {
        daoTemplate.executarUpdate(
            SQL_UPDATE, 
            subCategoria.getNome(), 
            subCategoria.getCategoriaId(), 
            subCategoria.getId()
        );
    }
    
    /**
     * Exclui uma subcategoria do banco de dados.
     * @param id o ID da subcategoria a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        daoTemplate.executarUpdate(SQL_DELETE, id);
    }
    
    /**
     * Busca uma subcategoria pelo ID.
     * @param id o ID da subcategoria a ser buscada
     * @return a subcategoria encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public SubCategoria buscarPorId(int id) throws SQLException {
        Optional<SubCategoria> subCategoria = daoTemplate.buscar(SQL_FIND_BY_ID, rowMapper, id);
        return subCategoria.orElse(null);
    }
    
    /**
     * Lista todas as subcategorias do banco de dados.
     * @return a lista de subcategorias
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<SubCategoria> listarTodas() throws SQLException {
        return daoTemplate.listar(SQL_FIND_ALL, rowMapper);
    }
    
    /**
     * Lista todas as subcategorias de uma categoria específica.
     * @param categoriaId o ID da categoria
     * @return a lista de subcategorias da categoria
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<SubCategoria> listarPorCategoria(int categoriaId) throws SQLException {
        return daoTemplate.listar(SQL_FIND_BY_CATEGORIA, rowMapper, categoriaId);
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