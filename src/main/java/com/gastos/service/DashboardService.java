package com.gastos.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.gastos.controller.DespesaController;
import com.gastos.db.ConexaoBanco;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.Despesa;
import com.gastos.model.Responsavel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Serviço para gerenciar os dados do dashboard.
 * Responsável por carregar, filtrar e calcular totais das despesas.
 */
public class DashboardService {
    
    private final DespesaController despesaController;
    
    /**
     * Construtor padrão.
     */
    public DashboardService() {
        this.despesaController = new DespesaController();
    }
    
    /**
     * Calcula o total de despesas do mês.
     * 
     * @param responsavelId ID do responsável para filtro (opcional)
     * @return Valor total das despesas
     */
    public double calcularTotalDespesasDoMes(Integer responsavelId) {
        if (responsavelId != null) {
            return calcularTotalDespesasPorResponsavel(responsavelId);
        } else {
            return despesaController.calcularTotalDespesasDoMes();
        }
    }
    
    /**
     * Calcula o total de despesas pagas do mês.
     * 
     * @param responsavelId ID do responsável para filtro (opcional)
     * @return Valor total das despesas pagas
     */
    public double calcularTotalDespesasPagasDoMes(Integer responsavelId) {
        if (responsavelId != null) {
            return calcularTotalDespesasPagasPorResponsavel(responsavelId);
        } else {
            return despesaController.calcularTotalDespesasPagasDoMes();
        }
    }
    
    /**
     * Calcula o total de despesas a pagar do mês.
     * 
     * @param responsavelId ID do responsável para filtro (opcional)
     * @return Valor total das despesas a pagar
     */
    public double calcularTotalDespesasAPagarDoMes(Integer responsavelId) {
        if (responsavelId != null) {
            return calcularTotalDespesasAPagarPorResponsavel(responsavelId);
        } else {
            return despesaController.calcularTotalDespesasAPagarDoMes();
        }
    }
    
    /**
     * Obtém as despesas do mês atual, com opção de filtro por responsável.
     * 
     * @param responsavelId ID do responsável para filtro (opcional)
     * @return Lista observável de despesas
     */
    public ObservableList<Despesa> obterDespesasDoMes(Integer responsavelId) {
        ObservableList<Despesa> despesas;
        
        // Se tiver filtro ativo, buscar apenas despesas da pessoa selecionada
        if (responsavelId != null) {
            despesas = despesaController.listarDespesasPorResponsavel(responsavelId);
        } else {
            despesas = despesaController.listarDespesasDoMes();
        }
        
        // Se não conseguiu, buscar diretamente
        if (despesas == null || despesas.isEmpty()) {
            despesas = buscarDespesasDiretamente();
            
            // Se tiver filtro, aplicar filtro manualmente
            if (responsavelId != null) {
                despesas = filtrarDespesasPorResponsavel(despesas, responsavelId);
            }
        }
        
        return despesas;
    }
    
    /**
     * Obtém dados para o gráfico por categoria.
     * 
     * @param responsavelId ID do responsável para filtro (opcional)
     * @return Lista de dados para o gráfico
     */
    public List<Object[]> obterDadosGrafico(Integer responsavelId) {
        if (responsavelId != null) {
            return buscarDadosGraficoPorResponsavel(responsavelId);
        } else {
            return despesaController.obterDadosGraficoPorCategoria();
        }
    }
    
    /**
     * Calcula o total de despesas para um responsável específico.
     */
    private double calcularTotalDespesasPorResponsavel(int responsavelId) {
        ObservableList<Despesa> despesas = despesaController.listarDespesasPorResponsavel(responsavelId);
        if (despesas == null || despesas.isEmpty()) {
            return 0.0;
        }
        
        return despesas.stream()
                .filter(d -> d.getDataVencimento() != null && 
                        d.getDataVencimento().getMonth() == LocalDate.now().getMonth() &&
                        d.getDataVencimento().getYear() == LocalDate.now().getYear())
                .mapToDouble(Despesa::getValor)
                .sum();
    }
    
    /**
     * Calcula o total de despesas pagas para um responsável específico.
     */
    private double calcularTotalDespesasPagasPorResponsavel(int responsavelId) {
        ObservableList<Despesa> despesas = despesaController.listarDespesasPorResponsavel(responsavelId);
        if (despesas == null || despesas.isEmpty()) {
            return 0.0;
        }
        
        return despesas.stream()
                .filter(d -> d.isPago() && d.getDataVencimento() != null && 
                        d.getDataVencimento().getMonth() == LocalDate.now().getMonth() &&
                        d.getDataVencimento().getYear() == LocalDate.now().getYear())
                .mapToDouble(Despesa::getValor)
                .sum();
    }
    
    /**
     * Calcula o total de despesas a pagar para um responsável específico.
     */
    private double calcularTotalDespesasAPagarPorResponsavel(int responsavelId) {
        ObservableList<Despesa> despesas = despesaController.listarDespesasPorResponsavel(responsavelId);
        if (despesas == null || despesas.isEmpty()) {
            return 0.0;
        }
        
        return despesas.stream()
                .filter(d -> !d.isPago() && d.getDataVencimento() != null && 
                        d.getDataVencimento().getMonth() == LocalDate.now().getMonth() &&
                        d.getDataVencimento().getYear() == LocalDate.now().getYear())
                .mapToDouble(Despesa::getValor)
                .sum();
    }
    
    /**
     * Filtra a lista de despesas pelo responsável.
     */
    private ObservableList<Despesa> filtrarDespesasPorResponsavel(ObservableList<Despesa> despesas, int responsavelId) {
        if (despesas == null || despesas.isEmpty()) {
            return FXCollections.observableArrayList();
        }
        
        return FXCollections.observableArrayList(
            despesas.stream()
                .filter(d -> d.getResponsavel() != null && d.getResponsavel().getId() == responsavelId)
                .toList()
        );
    }
    
    /**
     * Busca despesas diretamente do banco em caso de falha.
     */
    private ObservableList<Despesa> buscarDespesasDiretamente() {
        List<Despesa> despesas = new ArrayList<>();

        try (Connection conn = ConexaoBanco.getConexao()) {
            String sql = "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, "
                    + "c.id as categoria_id, c.nome as categoria_nome, "
                    + "r.id as responsavel_id, r.nome as responsavel_nome "
                    + "FROM despesas d "
                    + "LEFT JOIN categorias c ON d.categoria_id = c.id "
                    + "LEFT JOIN responsaveis r ON d.responsavel_id = r.id "
                    + "ORDER BY d.id DESC LIMIT 20";

            try (PreparedStatement stmt = conn.prepareStatement(sql); 
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Despesa despesa = new Despesa();
                    despesa.setId(rs.getInt("id"));
                    despesa.setDescricao(rs.getString("descricao"));
                    despesa.setValor(rs.getDouble("valor"));

                    // Data de compra
                    String dataCompraStr = rs.getString("data_compra");
                    if (dataCompraStr != null && !dataCompraStr.isEmpty()) {
                        despesa.setDataCompra(LocalDate.parse(dataCompraStr));
                    } else {
                        despesa.setDataCompra(LocalDate.now());
                    }

                    // Data de vencimento (opcional)
                    String dataVencimentoStr = rs.getString("data_vencimento");
                    if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
                        despesa.setDataVencimento(LocalDate.parse(dataVencimentoStr));
                    }

                    despesa.setPago(rs.getBoolean("pago"));

                    // Categoria
                    int categoriaId = rs.getInt("categoria_id");
                    if (!rs.wasNull()) {
                        CategoriaDespesa categoria = new CategoriaDespesa();
                        categoria.setId(categoriaId);
                        categoria.setNome(rs.getString("categoria_nome"));
                        despesa.setCategoria(categoria);
                    }
                    
                    // Responsável
                    int responsavelId = rs.getInt("responsavel_id");
                    if (!rs.wasNull()) {
                        Responsavel responsavel = new Responsavel();
                        responsavel.setId(responsavelId);
                        responsavel.setNome(rs.getString("responsavel_nome"));
                        despesa.setResponsavel(responsavel);
                    }

                    despesas.add(despesa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(despesas);
    }
    
    /**
     * Busca dados para o gráfico por responsável específico.
     */
    private List<Object[]> buscarDadosGraficoPorResponsavel(int responsavelId) {
        List<Object[]> resultado = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            String sql = "SELECT c.nome, SUM(d.valor) as total " +
                "FROM despesas d " +
                "JOIN categorias c ON d.categoria_id = c.id " +
                "WHERE d.responsavel_id = ? " +
                "AND ((d.data_vencimento BETWEEN ? AND ?) OR " +
                "(d.data_vencimento IS NULL AND d.data_compra BETWEEN ? AND ?)) " +
                "GROUP BY c.nome " +
                "ORDER BY total DESC";
            
            LocalDate inicio = LocalDate.now().withDayOfMonth(1);
            LocalDate fim = inicio.plusMonths(1).minusDays(1);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, responsavelId);
                stmt.setString(2, inicio.toString());
                stmt.setString(3, fim.toString());
                stmt.setString(4, inicio.toString());
                stmt.setString(5, fim.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String nome = rs.getString(1);
                        double total = rs.getDouble("total");
                        resultado.add(new Object[]{nome, total});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return resultado;
    }
}