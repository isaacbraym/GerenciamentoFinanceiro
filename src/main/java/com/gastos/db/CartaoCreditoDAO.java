package com.gastos.db;

import com.gastos.model.CartaoCredito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO (Data Access Object) para a entidade CartaoCredito.
 */
public class CartaoCreditoDAO {
    
    // SQL queries como constantes para facilitar manutenção
    private static final String SQL_INSERT = 
            "INSERT INTO cartoes_credito (nome, bandeira, limite, dia_fechamento, dia_vencimento, cor) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = 
            "UPDATE cartoes_credito SET nome = ?, bandeira = ?, limite = ?, dia_fechamento = ?, dia_vencimento = ?, cor = ? WHERE id = ?";
    private static final String SQL_DELETE = 
            "DELETE FROM cartoes_credito WHERE id = ?";
    private static final String SQL_FIND_BY_ID = 
            "SELECT * FROM cartoes_credito WHERE id = ?";
    private static final String SQL_FIND_ALL = 
            "SELECT * FROM cartoes_credito ORDER BY nome";
    private static final String SQL_CALC_GASTOS_MES = 
            "SELECT SUM(valor) as total FROM despesas " +
            "WHERE cartao_id = ? AND " +
            "((data_vencimento BETWEEN ? AND ?) OR " +
            "(data_vencimento IS NULL AND data_compra BETWEEN ? AND ?))";
    
    /**
     * Insere um novo cartão no banco de dados.
     * @param cartao o cartão a ser inserido
     * @return o ID do cartão inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(CartaoCredito cartao) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            preencherStatement(stmt, cartao);
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Falha ao inserir cartão, nenhum ID foi retornado.");
                }
            }
        }
    }
    
    /**
     * Atualiza um cartão existente no banco de dados.
     * @param cartao o cartão a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(CartaoCredito cartao) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            preencherStatement(stmt, cartao);
            stmt.setInt(7, cartao.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Preenche um PreparedStatement com os dados do cartão.
     * @param stmt o PreparedStatement a ser preenchido
     * @param cartao o cartão com os dados
     * @throws SQLException se ocorrer um erro de SQL
     */
    private void preencherStatement(PreparedStatement stmt, CartaoCredito cartao) throws SQLException {
        stmt.setString(1, cartao.getNome());
        stmt.setString(2, cartao.getBandeira());
        stmt.setDouble(3, cartao.getLimite());
        stmt.setInt(4, cartao.getDiaFechamento());
        stmt.setInt(5, cartao.getDiaVencimento());
        stmt.setString(6, cartao.getCor());
    }
    
    /**
     * Exclui um cartão do banco de dados.
     * @param id o ID do cartão a ser excluído
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
     * Busca um cartão pelo ID.
     * @param id o ID do cartão a ser buscado
     * @return o cartão encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public CartaoCredito buscarPorId(int id) throws SQLException {
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirCartao(rs);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Lista todos os cartões do banco de dados.
     * @return a lista de cartões
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<CartaoCredito> listarTodos() throws SQLException {
        List<CartaoCredito> cartoes = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {
            
            while (rs.next()) {
                cartoes.add(construirCartao(rs));
            }
        }
        
        return cartoes;
    }
    
    /**
     * Constrói um objeto CartaoCredito a partir de um ResultSet.
     * @param rs o ResultSet contendo os dados do cartão
     * @return o cartão construído
     * @throws SQLException se ocorrer um erro de SQL
     */
    private CartaoCredito construirCartao(ResultSet rs) throws SQLException {
        CartaoCredito cartao = new CartaoCredito();
        
        cartao.setId(rs.getInt("id"));
        cartao.setNome(rs.getString("nome"));
        cartao.setBandeira(rs.getString("bandeira"));
        cartao.setLimite(rs.getDouble("limite"));
        cartao.setDiaFechamento(rs.getInt("dia_fechamento"));
        cartao.setDiaVencimento(rs.getInt("dia_vencimento"));
        cartao.setCor(rs.getString("cor"));
        
        return cartao;
    }
    
    /**
     * Calcula o total de gastos em um cartão específico para o mês atual.
     * @param cartaoId o ID do cartão
     * @return o valor total gasto no cartão no mês atual
     * @throws SQLException se ocorrer um erro de SQL
     */
    public double calcularGastosNoMes(int cartaoId) throws SQLException {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_CALC_GASTOS_MES)) {
            
            stmt.setInt(1, cartaoId);
            stmt.setString(2, inicio.toString());
            stmt.setString(3, fim.toString());
            stmt.setString(4, inicio.toString());
            stmt.setString(5, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0.0;
    }
}