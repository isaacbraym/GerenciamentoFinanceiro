package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.MeioPagamento;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade MeioPagamento.
 * Refatorada para usar DAOTemplate.
 */
public class MeioPagamentoDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = "INSERT INTO meios_pagamento (nome, cartao_credito) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE meios_pagamento SET nome = ?, cartao_credito = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM meios_pagamento WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM meios_pagamento WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM meios_pagamento ORDER BY nome";
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<MeioPagamento> rowMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e o RowMapper.
     */
    public MeioPagamentoDAO() {
        this.daoTemplate = new DAOTemplate();
        this.rowMapper = this::construirMeioPagamento;
    }
    
    /**
     * Insere um novo meio de pagamento no banco de dados.
     * @param meioPagamento o meio de pagamento a ser inserido
     * @return o ID do meio de pagamento inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(MeioPagamento meioPagamento) throws SQLException {
        Optional<Integer> id = daoTemplate.inserirEObterChave(
            SQL_INSERT, 
            meioPagamento.getNome(), 
            meioPagamento.isCartaoCredito()
        );
        
        return id.orElseThrow(() -> new SQLException("Falha ao inserir meio de pagamento, nenhum ID foi retornado."));
    }
    
    /**
     * Atualiza um meio de pagamento existente no banco de dados.
     * @param meioPagamento o meio de pagamento a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(MeioPagamento meioPagamento) throws SQLException {
        daoTemplate.executarUpdate(
            SQL_UPDATE, 
            meioPagamento.getNome(), 
            meioPagamento.isCartaoCredito(), 
            meioPagamento.getId()
        );
    }
    
    /**
     * Exclui um meio de pagamento do banco de dados.
     * @param id o ID do meio de pagamento a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        daoTemplate.executarUpdate(SQL_DELETE, id);
    }
    
    /**
     * Busca um meio de pagamento pelo ID.
     * @param id o ID do meio de pagamento a ser buscado
     * @return o meio de pagamento encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public MeioPagamento buscarPorId(int id) throws SQLException {
        Optional<MeioPagamento> meioPagamento = daoTemplate.buscar(SQL_FIND_BY_ID, rowMapper, id);
        return meioPagamento.orElse(null);
    }
    
    /**
     * Lista todos os meios de pagamento do banco de dados.
     * @return a lista de meios de pagamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<MeioPagamento> listarTodos() throws SQLException {
        return daoTemplate.listar(SQL_FIND_ALL, rowMapper);
    }
    
    /**
     * Constrói um objeto MeioPagamento a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados do meio de pagamento
     * @return o meio de pagamento construído
     * @throws SQLException se ocorrer um erro de SQL
     */
    private MeioPagamento construirMeioPagamento(ResultSet rs) throws SQLException {
        MeioPagamento meioPagamento = new MeioPagamento();
        
        meioPagamento.setId(rs.getInt("id"));
        meioPagamento.setNome(rs.getString("nome"));
        meioPagamento.setCartaoCredito(rs.getBoolean("cartao_credito"));
        
        return meioPagamento;
    }
}