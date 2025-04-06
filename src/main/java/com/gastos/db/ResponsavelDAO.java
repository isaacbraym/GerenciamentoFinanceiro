package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.Responsavel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade Responsavel.
 * Refatorada para usar DAOTemplate.
 */
public class ResponsavelDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = "INSERT INTO responsaveis (nome) VALUES (?)";
    private static final String SQL_UPDATE = "UPDATE responsaveis SET nome = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM responsaveis WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM responsaveis WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM responsaveis ORDER BY nome";
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<Responsavel> rowMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e o RowMapper.
     */
    public ResponsavelDAO() {
        this.daoTemplate = new DAOTemplate();
        this.rowMapper = this::construirResponsavel;
    }
    
    /**
     * Insere um novo responsável no banco de dados.
     * @param responsavel o responsável a ser inserido
     * @return o ID do responsável inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Responsavel responsavel) throws SQLException {
        Optional<Integer> id = daoTemplate.inserirEObterChave(SQL_INSERT, responsavel.getNome());
        return id.orElseThrow(() -> new SQLException("Falha ao inserir responsável, nenhum ID foi retornado."));
    }
    
    /**
     * Atualiza um responsável existente no banco de dados.
     * @param responsavel o responsável a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(Responsavel responsavel) throws SQLException {
        daoTemplate.executarUpdate(SQL_UPDATE, responsavel.getNome(), responsavel.getId());
    }
    
    /**
     * Exclui um responsável do banco de dados.
     * @param id o ID do responsável a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        daoTemplate.executarUpdate(SQL_DELETE, id);
    }
    
    /**
     * Busca um responsável pelo ID.
     * @param id o ID do responsável a ser buscado
     * @return o responsável encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Responsavel buscarPorId(int id) throws SQLException {
        Optional<Responsavel> responsavel = daoTemplate.buscar(SQL_FIND_BY_ID, rowMapper, id);
        return responsavel.orElse(null);
    }
    
    /**
     * Lista todos os responsáveis do banco de dados.
     * @return a lista de responsáveis
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Responsavel> listarTodos() throws SQLException {
        return daoTemplate.listar(SQL_FIND_ALL, rowMapper);
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