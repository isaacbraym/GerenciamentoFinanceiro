package com.gastos.controller;

import com.gastos.db.DespesaDAO;
import com.gastos.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para gerenciar as despesas no sistema.
 */
public class DespesaController {
    private final DespesaDAO despesaDAO;
    
    public DespesaController() {
        this.despesaDAO = new DespesaDAO();
    }
    
    /**
     * Salva uma nova despesa no sistema com informações detalhadas de erro.
     * @param despesa a despesa a ser salva
     * @return um objeto Resultado contendo status e mensagem
     */
    public Resultado salvarDespesa(Despesa despesa) {
        try {
            // Validações antes de salvar
            List<String> erros = validarDespesa(despesa);
            if (!erros.isEmpty()) {
                StringBuilder mensagemErro = new StringBuilder("Erros na validação da despesa:\n");
                for (String erro : erros) {
                    mensagemErro.append("- ").append(erro).append("\n");
                }
                return new Resultado(false, mensagemErro.toString());
            }
            
            System.out.println("Iniciando salvamento de despesa: " + despesa.getDescricao());
            
            // Verificar se é uma nova despesa ou uma atualização
            if (despesa.getId() == 0) {
                int id = despesaDAO.inserir(despesa);
                despesa.setId(id);
                System.out.println("Nova despesa inserida com ID: " + id);
            } else {
                despesaDAO.atualizar(despesa);
                System.out.println("Despesa atualizada. ID: " + despesa.getId());
            }
            return new Resultado(true, "Despesa salva com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro detalhado ao salvar despesa: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            
            // Construir mensagem de erro detalhada
            String mensagemErro = "Erro ao salvar despesa: " + e.getMessage();
            
            // Adicionar informações sobre parcelamento se houver
            if (despesa.getParcelamento() != null) {
                mensagemErro += "\n\nDetalhes do parcelamento:\n" +
                               "- Valor total: R$ " + despesa.getParcelamento().getValorTotal() + "\n" +
                               "- Número de parcelas: " + despesa.getParcelamento().getTotalParcelas() + "\n" +
                               "- Data de início: " + despesa.getParcelamento().getDataInicio();
            }
            
            // Verificar se o erro está relacionado ao cartão de crédito
            if (despesa.getCartaoCredito() != null && mensagemErro.toLowerCase().contains("cartao")) {
                mensagemErro += "\n\nVerifique se o cartão de crédito selecionado está correto.";
            }
            
            return new Resultado(false, mensagemErro);
        }
    }
    
    /**
     * Valida os dados da despesa antes de salvar.
     * @param despesa a despesa a ser validada
     * @return lista de mensagens de erro, vazia se não houver erros
     */
    private List<String> validarDespesa(Despesa despesa) {
        List<String> erros = new ArrayList<>();
        
        if (despesa.getDescricao() == null || despesa.getDescricao().trim().isEmpty()) {
            erros.add("A descrição da despesa é obrigatória");
        }
        
        if (despesa.getValor() <= 0) {
            erros.add("O valor da despesa deve ser maior que zero");
        }
        
        if (despesa.getDataCompra() == null) {
            erros.add("A data da compra é obrigatória");
        }
        
        if (despesa.getCategoria() == null) {
            erros.add("A categoria é obrigatória");
        }
        
        if (despesa.getMeioPagamento() != null && despesa.getMeioPagamento().isCartaoCredito() 
            && despesa.getCartaoCredito() == null) {
            erros.add("É necessário selecionar um cartão de crédito quando o meio de pagamento é cartão");
        }
        
        // Validar parcelamento
        if (despesa.getParcelamento() != null) {
            if (despesa.getParcelamento().getTotalParcelas() <= 0) {
                erros.add("O número de parcelas deve ser maior que zero");
            }
            
            if (despesa.getParcelamento().getValorTotal() <= 0) {
                erros.add("O valor total do parcelamento deve ser maior que zero");
            }
            
            if (despesa.getParcelamento().getDataInicio() == null) {
                erros.add("A data de início do parcelamento é obrigatória");
            }
            
            if (despesa.getParcelamento().getParcelas() == null || 
                despesa.getParcelamento().getParcelas().isEmpty()) {
                erros.add("É necessário gerar as parcelas antes de salvar");
            } else {
                // Verificar se todas as parcelas têm data de vencimento
                for (int i = 0; i < despesa.getParcelamento().getParcelas().size(); i++) {
                    Parcelamento.Parcela parcela = despesa.getParcelamento().getParcelas().get(i);
                    if (parcela.getDataVencimento() == null) {
                        erros.add("A parcela " + (i+1) + " está sem data de vencimento");
                    }
                }
            }
        }
        
        return erros;
    }
    
    /**
     * Exclui uma despesa do sistema.
     * @param id o ID da despesa a ser excluída
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirDespesa(int id) {
        try {
            despesaDAO.excluir(id);
            System.out.println("Despesa excluída. ID: " + id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro detalhado ao excluir despesa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Busca uma despesa pelo ID.
     * @param id o ID da despesa
     * @return a despesa encontrada ou null se não encontrar
     */
    public Despesa buscarDespesaPorId(int id) {
        try {
            return despesaDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar despesa: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lista todas as despesas do sistema.
     * @return uma lista observável de despesas
     */
    public ObservableList<Despesa> listarTodasDespesas() {
        try {
            List<Despesa> despesas = despesaDAO.listarTodas();
            System.out.println("Total de despesas listadas: " + despesas.size());
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas do mês atual.
     * @return uma lista observável de despesas do mês
     */
    public ObservableList<Despesa> listarDespesasDoMes() {
        try {
            List<Despesa> despesas = despesaDAO.listarDespesasDoMes();
            System.out.println("Despesas do mês listadas: " + despesas.size());
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas do mês: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas por categoria.
     * @param categoriaId o ID da categoria
     * @return uma lista observável de despesas da categoria
     */
    public ObservableList<Despesa> listarDespesasPorCategoria(int categoriaId) {
        try {
            List<Despesa> despesas = despesaDAO.listarPorCategoria(categoriaId);
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas por categoria: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas por responsável.
     * @param responsavelId o ID do responsável
     * @return uma lista observável de despesas do responsável
     */
    public ObservableList<Despesa> listarDespesasPorResponsavel(int responsavelId) {
        try {
            List<Despesa> despesas = despesaDAO.listarPorResponsavel(responsavelId);
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas por responsável: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas por cartão de crédito.
     * @param cartaoId o ID do cartão
     * @return uma lista observável de despesas do cartão
     */
    public ObservableList<Despesa> listarDespesasPorCartao(int cartaoId) {
        try {
            List<Despesa> despesas = despesaDAO.listarPorCartao(cartaoId);
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas por cartão: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas fixas.
     * @return uma lista observável de despesas fixas
     */
    public ObservableList<Despesa> listarDespesasFixas() {
        try {
            List<Despesa> despesas = despesaDAO.listarDespesasFixas();
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas fixas: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista as despesas parceladas.
     * @return uma lista observável de despesas parceladas
     */
    public ObservableList<Despesa> listarDespesasParceladas() {
        try {
            List<Despesa> despesas = despesaDAO.listarDespesasParceladas();
            return FXCollections.observableArrayList(despesas);
        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas parceladas: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Obtém dados para o gráfico de despesas por categoria.
     * @return uma lista de pares (categoria, valor)
     */
    public List<Object[]> obterDadosGraficoPorCategoria() {
        try {
            List<Object[]> dados = despesaDAO.calcularTotalPorCategoria();
            System.out.println("Dados obtidos para gráfico de categorias: " + dados.size() + " registros");
            return dados;
        } catch (SQLException e) {
            System.err.println("Erro ao obter dados para gráfico de categorias: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtém dados para o gráfico de despesas por responsável.
     * @return uma lista de pares (responsável, valor)
     */
    public List<Object[]> obterDadosGraficoPorResponsavel() {
        try {
            return despesaDAO.calcularTotalPorResponsavel();
        } catch (SQLException e) {
            System.err.println("Erro ao obter dados para gráfico de responsáveis: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Calcula o total de despesas do mês atual.
     * @return o valor total das despesas do mês
     */
    public double calcularTotalDespesasDoMes() {
        ObservableList<Despesa> despesas = listarDespesasDoMes();
        double total = despesas.stream().mapToDouble(Despesa::getValor).sum();
        System.out.println("Total de despesas do mês: R$ " + total);
        return total;
    }
    
    /**
     * Calcula o total de despesas pagas do mês atual.
     * @return o valor total das despesas pagas do mês
     */
    public double calcularTotalDespesasPagasDoMes() {
        ObservableList<Despesa> despesas = listarDespesasDoMes();
        double total = despesas.stream()
                .filter(Despesa::isPago)
                .mapToDouble(Despesa::getValor)
                .sum();
        System.out.println("Total de despesas pagas do mês: R$ " + total);
        return total;
    }
    
    /**
     * Calcula o total de despesas a pagar do mês atual.
     * @return o valor total das despesas a pagar do mês
     */
    public double calcularTotalDespesasAPagarDoMes() {
        ObservableList<Despesa> despesas = listarDespesasDoMes();
        double total = despesas.stream()
                .filter(d -> !d.isPago())
                .mapToDouble(Despesa::getValor)
                .sum();
        System.out.println("Total de despesas a pagar do mês: R$ " + total);
        return total;
    }
    
    /**
     * Classe interna para representar o resultado de uma operação.
     */
    public static class Resultado {
        private final boolean sucesso;
        private final String mensagem;
        
        public Resultado(boolean sucesso, String mensagem) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
        }
        
        public boolean isSucesso() {
            return sucesso;
        }
        
        public String getMensagem() {
            return mensagem;
        }
    }
}