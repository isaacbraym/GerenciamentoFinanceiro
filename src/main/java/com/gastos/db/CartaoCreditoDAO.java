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
    
    /**
     * Insere um novo cartão no banco de dados.
     * @param cartao o cartão a ser inserido
     * @return o ID do cartão inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(CartaoCredito cartao) throws SQLException {
        String sql = "INSERT INTO cartoes_credito (nome, bandeira, limite, dia_fechamento, dia_vencimento, cor) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, cartao.getNome());
            stmt.setString(2, cartao.getBandeira());
            stmt.setDouble(3, cartao.getLimite());
            stmt.setInt(4, cartao.getDiaFechamento());
            stmt.setInt(5, cartao.getDiaVencimento());
            stmt.setString(6, cartao.getCor());
            
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
        String sql = "UPDATE cartoes_credito SET nome = ?, bandeira = ?, limite = ?, " +
                     "dia_fechamento = ?, dia_vencimento = ?, cor = ? WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cartao.getNome());
            stmt.setString(2, cartao.getBandeira());
            stmt.setDouble(3, cartao.getLimite());
            stmt.setInt(4, cartao.getDiaFechamento());
            stmt.setInt(5, cartao.getDiaVencimento());
            stmt.setString(6, cartao.getCor());
            stmt.setInt(7, cartao.getId());
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui um cartão do banco de dados.
     * @param id o ID do cartão a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM cartoes_credito WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT * FROM cartoes_credito WHERE id = ?";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
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
        String sql = "SELECT * FROM cartoes_credito ORDER BY nome";
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
        double total = 0.0;
        
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1).minusDays(1);
        
        String sql = "SELECT SUM(valor) as total FROM despesas " +
                     "WHERE cartao_id = ? AND " +
                     "((data_vencimento BETWEEN ? AND ?) OR " +
                     "(data_vencimento IS NULL AND data_compra BETWEEN ? AND ?))";
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, cartaoId);
            stmt.setString(2, inicio.toString());
            stmt.setString(3, fim.toString());
            stmt.setString(4, inicio.toString());
            stmt.setString(5, fim.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    total = rs.getDouble("total");
                }
            }
        }
        
        return total;
    }
}