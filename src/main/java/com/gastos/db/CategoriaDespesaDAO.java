package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade CategoriaDespesa.
 * Refatorada para usar DAOTemplate.
 */
public class CategoriaDespesaDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT_CATEGORIA = "INSERT INTO categorias (nome) VALUES (?)";
    private static final String SQL_UPDATE_CATEGORIA = "UPDATE categorias SET nome = ? WHERE id = ?";
    private static final String SQL_DELETE_SUBCATEGORIAS = "DELETE FROM subcategorias WHERE categoria_id = ?";
    private static final String SQL_DELETE_CATEGORIA = "DELETE FROM categorias WHERE id = ?";
    private static final String SQL_FIND_CATEGORIA_BY_ID = "SELECT * FROM categorias WHERE id = ?";
    private static final String SQL_FIND_ALL_CATEGORIAS = "SELECT * FROM categorias ORDER BY nome";
    private static final String SQL_FIND_SUBCATEGORIAS = "SELECT * FROM subcategorias WHERE categoria_id = ? ORDER BY nome";
    private static final String SQL_INSERT_SUBCATEGORIA = "INSERT INTO subcategorias (nome, categoria_id) VALUES (?, ?)";
    private static final String SQL_UPDATE_SUBCATEGORIA = "UPDATE subcategorias SET nome = ?, categoria_id = ? WHERE id = ?";
    private static final String SQL_DELETE_SUBCATEGORIA = "DELETE FROM subcategorias WHERE id = ?";
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<CategoriaDespesa> categoriaMapper;
    private final RowMapper<SubCategoria> subcategoriaMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e os RowMappers.
     */
    public CategoriaDespesaDAO() {
        this.daoTemplate = new DAOTemplate();
        this.categoriaMapper = this::construirCategoria;
        this.subcategoriaMapper = this::construirSubCategoria;
    }
    
    /**
     * Insere uma nova categoria no banco de dados.
     * @param categoria a categoria a ser inserida
     * @return o ID da categoria inserida
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(CategoriaDespesa categoria) throws SQLException {
        System.out.println("Tentando inserir categoria: " + categoria.getNome());
        
        Optional<Integer> id = daoTemplate.inserirEObterChave(SQL_INSERT_CATEGORIA, categoria.getNome());
        
        int categoriaId = id.orElseThrow(() -> 
            new SQLException("Falha ao inserir categoria, nenhum ID foi retornado."));
        
        categoria.setId(categoriaId);
        System.out.println("Categoria inserida com ID: " + categoriaId);
        
        return categoriaId;
    }
    
    /**
     * Atualiza uma categoria existente no banco de dados.
     * @param categoria a categoria a ser atualizada
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(CategoriaDespesa categoria) throws SQLException {
        System.out.println("Atualizando categoria ID: " + categoria.getId() + ", Nome: " + categoria.getNome());
        
        int linhasAfetadas = daoTemplate.executarUpdate(SQL_UPDATE_CATEGORIA, 
                categoria.getNome(), categoria.getId());
        
        System.out.println("Linhas afetadas na atualização: " + linhasAfetadas);
        
        if (linhasAfetadas == 0) {
            System.out.println("AVISO: Nenhuma linha foi atualizada para o ID: " + categoria.getId());
        }
    }
    
    /**
     * Exclui uma categoria do banco de dados.
     * @param id o ID da categoria a ser excluída
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        System.out.println("Excluindo categoria ID: " + id);
        
        daoTemplate.executarEmTransacao(conn -> {
            try {
                // Excluir subcategorias
                int subRows = excluirSubcategorias(conn, id);
                System.out.println("Subcategorias excluídas: " + subRows);
                
                // Excluir categoria
                int catRows = excluirCategoria(conn, id);
                System.out.println("Categorias excluídas: " + catRows);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Exclui as subcategorias de uma categoria.
     * Método auxiliar usado dentro de uma transação.
     */
    private int excluirSubcategorias(Connection conn, int categoriaId) throws SQLException {
        try (var stmt = conn.prepareStatement(SQL_DELETE_SUBCATEGORIAS)) {
            stmt.setInt(1, categoriaId);
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui uma categoria.
     * Método auxiliar usado dentro de uma transação.
     */
    private int excluirCategoria(Connection conn, int categoriaId) throws SQLException {
        try (var stmt = conn.prepareStatement(SQL_DELETE_CATEGORIA)) {
            stmt.setInt(1, categoriaId);
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Busca uma categoria pelo ID.
     * @param id o ID da categoria a ser buscada
     * @return a categoria encontrada ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public CategoriaDespesa buscarPorId(int id) throws SQLException {
        Optional<CategoriaDespesa> categoriaOpt = daoTemplate.buscar(SQL_FIND_CATEGORIA_BY_ID, categoriaMapper, id);
        
        if (categoriaOpt.isPresent()) {
            CategoriaDespesa categoria = categoriaOpt.get();
            categoria.setSubCategorias(buscarSubcategorias(categoria.getId()));
            return categoria;
        }
        
        return null;
    }
    
    /**
     * Lista todas as categorias do banco de dados.
     * @return a lista de categorias
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<CategoriaDespesa> listarTodas() throws SQLException {
        System.out.println("Buscando todas as categorias...");
        
        List<CategoriaDespesa> categorias = daoTemplate.listar(SQL_FIND_ALL_CATEGORIAS, categoriaMapper);
        
        // Carregar subcategorias para cada categoria
        for (CategoriaDespesa categoria : categorias) {
            List<SubCategoria> subcategorias = buscarSubcategorias(categoria.getId());
            categoria.setSubCategorias(subcategorias);
            
            System.out.println("Categoria encontrada: " + categoria.getId() + " - " + categoria.getNome() + 
                               " com " + categoria.getSubCategorias().size() + " subcategorias");
        }
        
        System.out.println("Total de categorias encontradas: " + categorias.size());
        return categorias;
    }
    
    /**
     * Constrói um objeto CategoriaDespesa a partir de um ResultSet.
     */
    private CategoriaDespesa construirCategoria(ResultSet rs) throws SQLException {
        CategoriaDespesa categoria = new CategoriaDespesa();
        
        categoria.setId(rs.getInt("id"));
        categoria.setNome(rs.getString("nome"));
        
        return categoria;
    }
    
    /**
     * Constrói um objeto SubCategoria a partir de um ResultSet.
     */
    private SubCategoria construirSubCategoria(ResultSet rs) throws SQLException {
        SubCategoria subcategoria = new SubCategoria();
        
        subcategoria.setId(rs.getInt("id"));
        subcategoria.setNome(rs.getString("nome"));
        subcategoria.setCategoriaId(rs.getInt("categoria_id"));
        
        return subcategoria;
    }
    
    /**
     * Busca todas as subcategorias de uma categoria específica.
     */
    private List<SubCategoria> buscarSubcategorias(int categoriaId) throws SQLException {
        return daoTemplate.listar(SQL_FIND_SUBCATEGORIAS, subcategoriaMapper, categoriaId);
    }
    
    /**
     * Insere uma nova subcategoria no banco de dados.
     */
    public int inserirSubcategoria(SubCategoria subcategoria) throws SQLException {
        System.out.println("Inserindo subcategoria: " + subcategoria.getNome() + 
                          " na categoria ID: " + subcategoria.getCategoriaId());
        
        Optional<Integer> id = daoTemplate.inserirEObterChave(
            SQL_INSERT_SUBCATEGORIA, 
            subcategoria.getNome(), 
            subcategoria.getCategoriaId()
        );
        
        int subcategoriaId = id.orElseThrow(() -> 
            new SQLException("Falha ao inserir subcategoria, nenhum ID foi retornado."));
        
        subcategoria.setId(subcategoriaId);
        System.out.println("Subcategoria inserida com ID: " + subcategoriaId);
        
        return subcategoriaId;
    }
    
    /**
     * Atualiza uma subcategoria existente no banco de dados.
     */
    public void atualizarSubcategoria(SubCategoria subcategoria) throws SQLException {
        System.out.println("Atualizando subcategoria ID: " + subcategoria.getId() + 
                          ", Nome: " + subcategoria.getNome());
        
        int linhasAfetadas = daoTemplate.executarUpdate(
            SQL_UPDATE_SUBCATEGORIA, 
            subcategoria.getNome(), 
            subcategoria.getCategoriaId(), 
            subcategoria.getId()
        );
        
        System.out.println("Linhas afetadas na atualização da subcategoria: " + linhasAfetadas);
        
        if (linhasAfetadas == 0) {
            System.out.println("AVISO: Nenhuma linha foi atualizada para subcategoria ID: " + subcategoria.getId());
        }
    }
    
    /**
     * Exclui uma subcategoria do banco de dados.
     */
    public void excluirSubcategoria(int id) throws SQLException {
        System.out.println("Excluindo subcategoria ID: " + id);
        
        int linhasAfetadas = daoTemplate.executarUpdate(SQL_DELETE_SUBCATEGORIA, id);
        
        System.out.println("Linhas afetadas na exclusão da subcategoria: " + linhasAfetadas);
        
        if (linhasAfetadas == 0) {
            System.out.println("AVISO: Nenhuma linha foi excluída para subcategoria ID: " + id);
        }
    }
}