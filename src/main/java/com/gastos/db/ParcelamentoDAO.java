package com.gastos.db;

import com.gastos.db.util.DAOTemplate;
import com.gastos.db.util.RowMapper;
import com.gastos.model.Parcelamento;
import com.gastos.model.Parcelamento.Parcela;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe DAO (Data Access Object) para a entidade Parcelamento.
 * Refatorada para usar DAOTemplate.
 */
public class ParcelamentoDAO {
    
    // Consultas SQL
    private static final String SQL_INSERT_PARCELAMENTO = 
        "INSERT INTO parcelamentos (valor_total, total_parcelas, parcelas_restantes, data_inicio) VALUES (?, ?, ?, ?)";
    
    private static final String SQL_INSERT_PARCELA = 
        "INSERT INTO parcelas (parcelamento_id, numero_parcela, valor, data_vencimento, paga) VALUES (?, ?, ?, ?, ?)";
    
    private static final String SQL_UPDATE_PARCELAMENTO = 
        "UPDATE parcelamentos SET valor_total = ?, total_parcelas = ?, parcelas_restantes = ?, data_inicio = ? WHERE id = ?";
    
    private static final String SQL_DELETE_PARCELAS = 
        "DELETE FROM parcelas WHERE parcelamento_id = ?";
    
    private static final String SQL_DELETE_PARCELAMENTO = 
        "DELETE FROM parcelamentos WHERE id = ?";
    
    private static final String SQL_FIND_BY_ID = 
        "SELECT * FROM parcelamentos WHERE id = ?";
    
    private static final String SQL_FIND_ALL = 
        "SELECT * FROM parcelamentos ORDER BY data_inicio DESC";
    
    private static final String SQL_FIND_PARCELAS_BY_PARCELAMENTO = 
        "SELECT * FROM parcelas WHERE parcelamento_id = ? ORDER BY numero_parcela";
    
    private static final String SQL_FIND_ACTIVE = 
        "SELECT * FROM parcelamentos WHERE parcelas_restantes > 0 ORDER BY data_inicio DESC";
    
    private static final String SQL_UPDATE_PARCELA_STATUS = 
        "UPDATE parcelas SET paga = ? WHERE id = ?";
    
    private static final String SQL_FIND_PARCELAS_NEXT_MONTH = 
        "SELECT p.*, par.id as parcelamento_id FROM parcelas p " +
        "JOIN parcelamentos par ON p.parcelamento_id = par.id " +
        "WHERE p.paga = 0 AND p.data_vencimento BETWEEN ? AND ? " +
        "ORDER BY p.data_vencimento";
    
    private static final String SQL_GET_PARCELAMENTO_ID_FROM_PARCELA = 
        "SELECT parcelamento_id FROM parcelas WHERE id = ?";
    
    private static final String SQL_COUNT_UNPAID_PARCELAS = 
        "SELECT COUNT(*) as restantes FROM parcelas WHERE parcelamento_id = ? AND paga = 0";
    
    private static final String SQL_UPDATE_PARCELAS_RESTANTES = 
        "UPDATE parcelamentos SET parcelas_restantes = ? WHERE id = ?";
    
    private final DAOTemplate daoTemplate;
    private final RowMapper<Parcelamento> parcelamentoMapper;
    private final RowMapper<Parcela> parcelaMapper;
    
    /**
     * Construtor padrão que inicializa o DAOTemplate e os RowMappers.
     */
    public ParcelamentoDAO() {
        this.daoTemplate = new DAOTemplate();
        this.parcelamentoMapper = this::construirParcelamento;
        this.parcelaMapper = this::construirParcela;
    }
    
    /**
     * Insere um novo parcelamento no banco de dados.
     * @param parcelamento o parcelamento a ser inserido
     * @return o ID do parcelamento inserido
     * @throws SQLException se ocorrer um erro de SQL
     */
    public int inserir(Parcelamento parcelamento) throws SQLException {
        validarParcelamento(parcelamento);
        
        final List<Integer> idGerado = new ArrayList<>();
        
        daoTemplate.executarEmTransacao(conn -> {
            try {
                // Inserir parcelamento
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_PARCELAMENTO, 
                        Statement.RETURN_GENERATED_KEYS);
                
                stmt.setDouble(1, parcelamento.getValorTotal());
                stmt.setInt(2, parcelamento.getTotalParcelas());
                stmt.setInt(3, parcelamento.getParcelasRestantes());
                stmt.setString(4, parcelamento.getDataInicio().toString());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Falha ao inserir parcelamento, nenhuma linha afetada.");
                }
                
                // Obter ID gerado
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        idGerado.add(id);
                        parcelamento.setId(id);
                        
                        // Inserir parcelas
                        for (Parcela parcela : parcelamento.getParcelas()) {
                            inserirParcela(conn, parcela, id);
                        }
                    } else {
                        throw new SQLException("Falha ao inserir parcelamento, nenhum ID foi retornado.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        
        if (idGerado.isEmpty()) {
            throw new SQLException("Não foi possível obter o ID do parcelamento inserido");
        }
        
        return parcelamento.getId();
    }
    
    /**
     * Valida os dados do parcelamento antes de inserir/atualizar.
     */
    private void validarParcelamento(Parcelamento parcelamento) throws SQLException {
        if (parcelamento.getTotalParcelas() <= 0) {
            throw new SQLException("O número de parcelas deve ser maior que zero");
        }
        
        if (parcelamento.getValorTotal() <= 0) {
            throw new SQLException("O valor total deve ser maior que zero");
        }
        
        if (parcelamento.getDataInicio() == null) {
            throw new SQLException("A data de início do parcelamento não pode ser nula");
        }
        
        if (parcelamento.getParcelas() == null || parcelamento.getParcelas().isEmpty()) {
            throw new SQLException("O parcelamento deve ter parcelas geradas antes de ser salvo");
        }
    }
    
    /**
     * Insere uma nova parcela no banco de dados.
     */
    private void inserirParcela(Connection conn, Parcela parcela, int parcelamentoId) throws SQLException {
        // Validar dados da parcela
        if (parcela.getDataVencimento() == null) {
            throw new SQLException("A data de vencimento da parcela não pode ser nula");
        }
        
        if (parcela.getValor() <= 0) {
            throw new SQLException("O valor da parcela deve ser maior que zero");
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_PARCELA, 
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, parcelamentoId);
            stmt.setInt(2, parcela.getNumeroParcela());
            stmt.setDouble(3, parcela.getValor());
            stmt.setString(4, parcela.getDataVencimento().toString());
            stmt.setBoolean(5, parcela.isPaga());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir parcela, nenhuma linha afetada.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    parcela.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter ID da parcela inserida.");
                }
            }
        }
    }
    
    /**
     * Atualiza um parcelamento existente no banco de dados.
     */
    public void atualizar(Parcelamento parcelamento) throws SQLException {
        validarParcelamento(parcelamento);
        
        if (parcelamento.getId() <= 0) {
            throw new SQLException("ID do parcelamento inválido");
        }
        
        daoTemplate.executarEmTransacao(conn -> {
            try {
                // Atualizar parcelamento
                PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PARCELAMENTO);
                stmt.setDouble(1, parcelamento.getValorTotal());
                stmt.setInt(2, parcelamento.getTotalParcelas());
                stmt.setInt(3, parcelamento.getParcelasRestantes());
                stmt.setString(4, parcelamento.getDataInicio().toString());
                stmt.setInt(5, parcelamento.getId());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Falha ao atualizar parcelamento, nenhuma linha afetada.");
                }
                
                // Excluir parcelas antigas
                excluirParcelas(conn, parcelamento.getId());
                
                // Inserir novas parcelas
                for (Parcela parcela : parcelamento.getParcelas()) {
                    inserirParcela(conn, parcela, parcelamento.getId());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Exclui as parcelas de um parcelamento.
     */
    private void excluirParcelas(Connection conn, int parcelamentoId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PARCELAS)) {
            stmt.setInt(1, parcelamentoId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Exclui um parcelamento do banco de dados.
     */
    public void excluir(int id) throws SQLException {
        daoTemplate.executarEmTransacao(conn -> {
            try {
                // Excluir parcelas
                excluirParcelas(conn, id);
                
                // Excluir parcelamento
                PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PARCELAMENTO);
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Busca um parcelamento pelo ID.
     */
    public Parcelamento buscarPorId(int id) throws SQLException {
        Optional<Parcelamento> parcelamentoOpt = daoTemplate.buscar(SQL_FIND_BY_ID, parcelamentoMapper, id);
        
        if (parcelamentoOpt.isPresent()) {
            Parcelamento parcelamento = parcelamentoOpt.get();
            // Carregar parcelas
            parcelamento.setParcelas(buscarParcelas(parcelamento.getId()));
            return parcelamento;
        }
        
        return null;
    }
    
    /**
     * Lista todos os parcelamentos do banco de dados.
     */
    public List<Parcelamento> listarTodos() throws SQLException {
        List<Parcelamento> parcelamentos = daoTemplate.listar(SQL_FIND_ALL, parcelamentoMapper);
        
        // Carregar parcelas para cada parcelamento
        for (Parcelamento p : parcelamentos) {
            p.setParcelas(buscarParcelas(p.getId()));
        }
        
        return parcelamentos;
    }
    
    /**
     * Lista os parcelamentos ativos (que ainda têm parcelas a pagar).
     */
    public List<Parcelamento> listarParcelamentosAtivos() throws SQLException {
        List<Parcelamento> parcelamentos = daoTemplate.listar(SQL_FIND_ACTIVE, parcelamentoMapper);
        
        // Carregar parcelas para cada parcelamento
        for (Parcelamento p : parcelamentos) {
            p.setParcelas(buscarParcelas(p.getId()));
        }
        
        return parcelamentos;
    }
    
    /**
     * Busca as parcelas de um parcelamento.
     */
    private List<Parcela> buscarParcelas(int parcelamentoId) throws SQLException {
        List<Parcela> parcelas = daoTemplate.listar(SQL_FIND_PARCELAS_BY_PARCELAMENTO, parcelaMapper, parcelamentoId);
        
        // Definir o parcelamentoId em cada parcela
        for (Parcela p : parcelas) {
            p.setParcelamentoId(parcelamentoId);
        }
        
        return parcelas;
    }
    
    /**
     * Constrói um objeto Parcela a partir de um ResultSet.
     */
    private Parcela construirParcela(ResultSet rs) throws SQLException {
        Parcela parcela = new Parcela();
        parcela.setId(rs.getInt("id"));
        parcela.setNumeroParcela(rs.getInt("numero_parcela"));
        parcela.setValor(rs.getDouble("valor"));
        
        String dataVencimentoStr = rs.getString("data_vencimento");
        if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
            parcela.setDataVencimento(LocalDate.parse(dataVencimentoStr));
        }
        
        parcela.setPaga(rs.getBoolean("paga"));
        
        return parcela;
    }
    
    /**
     * Constrói um objeto Parcelamento a partir de um ResultSet.
     */
    private Parcelamento construirParcelamento(ResultSet rs) throws SQLException {
        Parcelamento parcelamento = new Parcelamento();
        
        parcelamento.setId(rs.getInt("id"));
        parcelamento.setValorTotal(rs.getDouble("valor_total"));
        parcelamento.setTotalParcelas(rs.getInt("total_parcelas"));
        parcelamento.setParcelasRestantes(rs.getInt("parcelas_restantes"));
        
        String dataInicioStr = rs.getString("data_inicio");
        if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
            parcelamento.setDataInicio(LocalDate.parse(dataInicioStr));
        } else {
            parcelamento.setDataInicio(LocalDate.now());
        }
        
        return parcelamento;
    }
    
    /**
     * Atualiza o status de pagamento de uma parcela.
     */
    public void marcarParcelaPaga(int parcelaId, boolean paga) throws SQLException {
        daoTemplate.executarUpdate(SQL_UPDATE_PARCELA_STATUS, paga, parcelaId);
    }
    
    /**
     * Busca o ID do parcelamento associado a uma parcela.
     */
    public int buscarParcelamentoIdDaParcela(int parcelaId) throws SQLException {
        Optional<Integer> resultado = daoTemplate.buscar(
            SQL_GET_PARCELAMENTO_ID_FROM_PARCELA,
            rs -> rs.getInt("parcelamento_id"),
            parcelaId
        );
        
        return resultado.orElse(-1);
    }
    
    /**
     * Atualiza o número de parcelas restantes do parcelamento.
     */
    public void atualizarParcelasRestantes(int parcelamentoId) throws SQLException {
        Optional<Integer> parcelasRestantes = daoTemplate.buscar(
            SQL_COUNT_UNPAID_PARCELAS,
            rs -> rs.getInt("restantes"),
            parcelamentoId
        );
        
        daoTemplate.executarUpdate(
            SQL_UPDATE_PARCELAS_RESTANTES, 
            parcelasRestantes.orElse(0), 
            parcelamentoId
        );
    }
    
    /**
     * Busca as parcelas a vencer no próximo mês.
     */
    public List<Parcela> buscarParcelasAVencer() throws SQLException {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioProximoMes = hoje.plusMonths(1).withDayOfMonth(1);
        LocalDate fimProximoMes = inicioProximoMes.plusMonths(1).minusDays(1);
        
        List<Parcela> parcelas = daoTemplate.listar(
            SQL_FIND_PARCELAS_NEXT_MONTH, 
            rs -> {
                Parcela p = construirParcela(rs);
                p.setParcelamentoId(rs.getInt("parcelamento_id"));
                return p;
            },
            inicioProximoMes.toString(), 
            fimProximoMes.toString()
        );
        
        return parcelas;
    }
}