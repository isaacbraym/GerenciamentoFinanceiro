package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.CartaoCredito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade CartaoCredito.
 * Refatorada para usar DAOTemplate.
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
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<CartaoCredito> rowMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e o RowMapper.
     */
    public CartaoCreditoDAO() {
        this.daoTemplate = new DAOTemplate();
        this.rowMapper = this::construirCartao;
    }
    
    /**
     * Insere um novo cartão no banco de dados.
     * @param cartao o cartão a ser inserido
     * @return o ID do cartão inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(CartaoCredito cartao) throws SQLException {
        Optional<Integer> id = daoTemplate.inserirEObterChave(SQL_INSERT, 
                cartao.getNome(), 
                cartao.getBandeira(), 
                cartao.getLimite(), 
                cartao.getDiaFechamento(), 
                cartao.getDiaVencimento(), 
                cartao.getCor());
        
        return id.orElseThrow(() -> new SQLException("Falha ao inserir cartão, nenhum ID foi retornado."));
    }
    
    /**
     * Atualiza um cartão existente no banco de dados.
     * @param cartao o cartão a ser atualizado
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizar(CartaoCredito cartao) throws SQLException {
        daoTemplate.executarUpdate(SQL_UPDATE, 
                cartao.getNome(), 
                cartao.getBandeira(), 
                cartao.getLimite(), 
                cartao.getDiaFechamento(), 
                cartao.getDiaVencimento(), 
                cartao.getCor(), 
                cartao.getId());
    }
    
    /**
     * Exclui um cartão do banco de dados.
     * @param id o ID do cartão a ser excluído
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void excluir(int id) throws SQLException {
        daoTemplate.executarUpdate(SQL_DELETE, id);
    }
    
    /**
     * Busca um cartão pelo ID.
     * @param id o ID do cartão a ser buscado
     * @return o cartão encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public CartaoCredito buscarPorId(int id) throws SQLException {
        Optional<CartaoCredito> cartao = daoTemplate.buscar(SQL_FIND_BY_ID, rowMapper, id);
        return cartao.orElse(null);
    }
    
    /**
     * Lista todos os cartões do banco de dados.
     * @return a lista de cartões
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<CartaoCredito> listarTodos() throws SQLException {
        return daoTemplate.listar(SQL_FIND_ALL, rowMapper);
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
        
        Optional<Double> total = daoTemplate.buscar(
            SQL_CALC_GASTOS_MES,
            rs -> rs.wasNull() ? 0.0 : rs.getDouble("total"),
            cartaoId,
            inicio.toString(),
            fim.toString(),
            inicio.toString(),
            fim.toString()
        );
        
        return total.orElse(0.0);
    }
}