package com.gastos.service;

import com.gastos.controller.DespesaController;
import com.gastos.db.ConexaoBanco;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.Despesa;
import com.gastos.model.Responsavel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Serviço para filtragem e consulta de despesas.
 */
public class DespesaFiltroService {
    private final DespesaController despesaController;
    
    /**
     * Constante para SQL de busca de despesas
     */
    private static final String SQL_DESPESAS_DIRETAS = 
            "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, d.fixo, " +
            "c.id as categoria_id, c.nome as categoria_nome " +
            "FROM despesas d " +
            "LEFT JOIN categorias c ON d.categoria_id = c.id " +
            "ORDER BY d.id DESC";
    
    /**
     * Construtor padrão.
     */
    public DespesaFiltroService() {
        this.despesaController = new DespesaController();
    }
    
    /**
     * Carrega todas as despesas.
     * 
     * @return Lista observável de despesas
     */
    public ObservableList<Despesa> carregarTodasDespesas() {
        try {
            // Tentar primeiro com o controller
            ObservableList<Despesa> despesas = despesaController.listarTodasDespesas();
            
            // Verificar se obteve algum resultado
            if (despesas == null || despesas.isEmpty()) {
                System.out.println("Controller não retornou despesas. Tentando acesso direto ao banco...");
                despesas = buscarDespesasDiretamente();
            }
            
            System.out.println("Despesas carregadas: " + despesas.size());
            return despesas;
        } catch (Exception e) {
            System.err.println("Erro ao carregar despesas: " + e.getMessage());
            e.printStackTrace();
            
            // Em caso de falha, tentar buscar diretamente
            ObservableList<Despesa> despesasDiretas = buscarDespesasDiretamente();
            System.out.println("Despesas carregadas diretamente: " + 
                              (despesasDiretas != null ? despesasDiretas.size() : 0));
            return despesasDiretas != null ? despesasDiretas : FXCollections.observableArrayList();
        }
    }
    
    /**
     * Filtra as despesas de acordo com os critérios especificados.
     * 
     * @param dataInicio Data inicial para filtro
     * @param dataFim Data final para filtro
     * @param tipo Tipo de despesa (Todos, Normal, Fixa, Parcelada)
     * @param status Status da despesa (Todos, Pago, A Pagar)
     * @param termoBusca Texto para busca na descrição ou categoria
     * @return Lista filtrada de despesas
     */
    public ObservableList<Despesa> filtrarDespesas(
            LocalDate dataInicio, 
            LocalDate dataFim, 
            String tipo, 
            String status, 
            String termoBusca) {
        
        try {
            ObservableList<Despesa> todasDespesas = carregarTodasDespesas();
            
            // Criar uma lista de predicados para aplicar os filtros
            List<Predicate<Despesa>> filtros = new ArrayList<>();
            
            // Filtro de data
            if (dataInicio != null && dataFim != null) {
                filtros.add(criarFiltroPorData(dataInicio, dataFim));
            }
            
            // Filtro de tipo
            if (tipo != null && !tipo.equals("Todos")) {
                filtros.add(criarFiltroPorTipo(tipo));
            }
            
            // Filtro de status
            if (status != null && !status.equals("Todos")) {
                boolean statusPago = status.equals("Pago");
                filtros.add(despesa -> despesa.isPago() == statusPago);
            }
            
            // Filtro de busca
            if (termoBusca != null && !termoBusca.trim().isEmpty()) {
                filtros.add(criarFiltroPorBusca(termoBusca));
            }
            
            // Aplicar todos os filtros
            Predicate<Despesa> filtroComposto = filtros.stream()
                    .reduce(Predicate::and)
                    .orElse(d -> true); // Se não houver filtros, aceita tudo
                    
            List<Despesa> despesasFiltradas = todasDespesas.stream()
                    .filter(filtroComposto)
                    .collect(Collectors.toList());
            
            System.out.println("Despesas filtradas: " + despesasFiltradas.size());
            return FXCollections.observableArrayList(despesasFiltradas);
            
        } catch (Exception e) {
            System.err.println("Erro ao filtrar despesas: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Cria um predicado para filtrar por intervalo de datas.
     */
    private Predicate<Despesa> criarFiltroPorData(LocalDate dataInicio, LocalDate dataFim) {
        return despesa -> {
            LocalDate dataCompra = despesa.getDataCompra();
            LocalDate dataVencimento = despesa.getDataVencimento();
            
            boolean dentroDataCompra = 
                    !dataCompra.isBefore(dataInicio) && !dataCompra.isAfter(dataFim);
                    
            boolean dentroDataVencimento = dataVencimento != null && 
                    !dataVencimento.isBefore(dataInicio) && 
                    !dataVencimento.isAfter(dataFim);
                    
            return dentroDataCompra || dentroDataVencimento;
        };
    }
    
    /**
     * Cria um predicado para filtrar por tipo de despesa.
     */
    private Predicate<Despesa> criarFiltroPorTipo(String tipo) {
        switch (tipo) {
            case "Normal":
                return despesa -> !despesa.isFixo() && !despesa.isParcelada();
            case "Fixa":
                return Despesa::isFixo;
            case "Parcelada":
                return Despesa::isParcelada;
            default:
                return d -> true;
        }
    }
    
    /**
     * Cria um predicado para filtrar por termo de busca.
     */
    private Predicate<Despesa> criarFiltroPorBusca(String termoBusca) {
        String termo = termoBusca.toLowerCase().trim();
        return despesa -> {
            boolean encontrouDescricao = despesa.getDescricao().toLowerCase().contains(termo);
            boolean encontrouCategoria = despesa.getCategoria() != null && 
                    despesa.getCategoria().getNome().toLowerCase().contains(termo);
                    
            return encontrouDescricao || encontrouCategoria;
        };
    }
    
    /**
     * Busca despesas diretamente do banco em caso de falha.
     */
    private ObservableList<Despesa> buscarDespesasDiretamente() {
        List<Despesa> despesas = new ArrayList<>();

        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESPESAS_DIRETAS);
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
                despesa.setFixo(rs.getBoolean("fixo"));

                // Categoria
                int categoriaId = rs.getInt("categoria_id");
                if (!rs.wasNull()) {
                    CategoriaDespesa categoria = new CategoriaDespesa();
                    categoria.setId(categoriaId);
                    categoria.setNome(rs.getString("categoria_nome"));
                    despesa.setCategoria(categoria);
                }

                despesas.add(despesa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(despesas);
    }
    
    /**
     * Exclui uma despesa.
     * 
     * @param despesaId ID da despesa a ser excluída
     * @return true se excluído com sucesso
     */
    public boolean excluirDespesa(int despesaId) {
        return despesaController.excluirDespesa(despesaId);
    }
    
    /**
     * Marca uma despesa como paga ou não paga.
     * 
     * @param despesa Despesa a ser atualizada
     * @param paga Status de pagamento
     * @return Resultado da operação
     */
    public DespesaController.Resultado marcarStatusPagamento(Despesa despesa, boolean paga) {
        despesa.setPago(paga);
        return despesaController.salvarDespesa(despesa);
    }
    
    /**
     * Busca uma despesa pelo ID.
     * 
     * @param id ID da despesa
     * @return Despesa encontrada ou null
     */
    public Despesa buscarDespesaPorId(int id) {
        return despesaController.buscarDespesaPorId(id);
    }
}