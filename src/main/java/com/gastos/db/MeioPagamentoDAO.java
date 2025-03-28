package com.gastos.db;

import com.gastos.model.MeioPagamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade MeioPagamento.
 */
public class MeioPagamentoDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = "INSERT INTO meios_pagamento (nome, cartao_credito) VALUES (?, ?)";
    private static final String SQL_UPDATE = "UPDATE meios_pagamento SET nome = ?, cartao_credito = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM meios_pagamento WHERE id = ?";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM meios_pagamento WHERE id = ?";
    private static final String SQL_FIND_ALL = "SELECT * FROM meios_pagamento ORDER BY nome";
    
    /**
     * Insere um novo meio de pagamento no banco de dados.
     * @param meioPagamento o meio de pagamento a ser inserido
     * @return o ID do meio de pagamento inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(MeioPagamento meioPagamento) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, meioPagamento.getNome());
            stmt.setBoolean(2, meioPagamento.isCartaoCredito());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao inserir meio de pagamento, nenhum ID foi retornado.");
                }
            }
        }
    }
    
    /**
     * Atualiza um meio de pagamento existente no banco de dados.
     * @param meioPagamento o meio de pagamento a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(MeioPagamento meioPagamento) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            stmt.setString(1, meioPagamento.getNome());
            stmt.setBoolean(2, meioPagamento.isCartaoCredito());
            stmt.setInt(3, meioPagamento.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui um meio de pagamento do banco de dados.
     * @param id o ID do meio de pagamento a ser excluído
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
     * Busca um meio de pagamento pelo ID.
     * @param id o ID do meio de pagamento a ser buscado
     * @return o meio de pagamento encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public MeioPagamento buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirMeioPagamento(rs);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Lista todos os meios de pagamento do banco de dados.
     * @return a lista de meios de pagamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<MeioPagamento> listarTodos() throws SQLException {
        List<MeioPagamento> meiosPagamento = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            
            while (rs.next()) {
                meiosPagamento.add(construirMeioPagamento(rs));
            }
        }
        
        return meiosPagamento;
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