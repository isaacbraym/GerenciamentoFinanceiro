package com.gastos.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gastos.db.DespesaDAO;
import com.gastos.model.Despesa;
import com.gastos.model.Parcelamento.Parcela;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
                    Parcela parcela = despesa.getParcelamento().getParcelas().get(i);
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
        return executarOperacao(() -> {
            despesaDAO.excluir(id);
            System.out.println("Despesa excluída. ID: " + id);
            return true;
        }, false);
    }

    /**
     * Busca uma despesa pelo ID.
     * @param id o ID da despesa
     * @return a despesa encontrada ou null se não encontrar
     */
    public Despesa buscarDespesaPorId(int id) {
        return executarOperacao(() -> despesaDAO.buscarPorId(id), null);
    }

    /**
     * Lista todas as despesas do sistema.
     * @return uma lista observável de despesas
     */
    public ObservableList<Despesa> listarTodasDespesas() {
        return executarOperacaoLista(despesaDAO::listarTodas);
    }

    /**
     * Lista as despesas do mês atual, com fallback manual em caso de erro no DAO.
     * @return uma lista observável de despesas do mês
     */
    public ObservableList<Despesa> listarDespesasDoMes() {
        try {
            System.out.println("Solicitando lista de despesas do mês ao DAO");
            List<Despesa> despesas = despesaDAO.listarDespesasDoMes();
            System.out.println("Despesas do mês listadas: " + despesas.size());

            if (despesas.isEmpty()) {
                System.out.println("ATENÇÃO: Nenhuma despesa encontrada para o mês atual!");
            } else {
                for (int i = 0; i < Math.min(5, despesas.size()); i++) {
                    Despesa d = despesas.get(i);
                    System.out.println("Despesa " + (i+1) + ": " + d.getDescricao() +
                            " - R$ " + d.getValor() +
                            " - Data: " + d.getDataCompra());
                }
                if (despesas.size() > 5) {
                    System.out.println("... e mais " + (despesas.size() - 5) + " despesas.");
                }
            }

            return FXCollections.observableArrayList(despesas);

        } catch (SQLException e) {
            System.err.println("Erro ao listar despesas do mês: " + e.getMessage());
            e.printStackTrace();
            // Fallback manual
            return fallbackListarDespesasDoMes();
        }
    }

    /**
     * Fallback: caso ocorra erro no método listarDespesasDoMes do DAO,
     * faz a busca de todas as despesas e filtra manualmente as do mês atual.
     */
    private ObservableList<Despesa> fallbackListarDespesasDoMes() {
        try {
            System.out.println("Tentando abordagem alternativa: listar todas as despesas...");
            List<Despesa> todasDespesas = despesaDAO.listarTodas();

            LocalDate inicio = LocalDate.now().withDayOfMonth(1);
            LocalDate fim = inicio.plusMonths(1).minusDays(1);

            List<Despesa> despesasDoMes = todasDespesas.stream()
                    .filter(d -> {
                        boolean dentroDataCompra =
                                !d.getDataCompra().isBefore(inicio) && !d.getDataCompra().isAfter(fim);

                        boolean dentroDataVencimento = d.getDataVencimento() != null &&
                                !d.getDataVencimento().isBefore(inicio) &&
                                !d.getDataVencimento().isAfter(fim);

                        return dentroDataCompra || dentroDataVencimento;
                    })
                    .collect(Collectors.toList());

            System.out.println("Abordagem alternativa encontrou " + despesasDoMes.size() + " despesas do mês.");
            return FXCollections.observableArrayList(despesasDoMes);
        } catch (Exception ex) {
            System.err.println("Erro também na abordagem alternativa: " + ex.getMessage());
            ex.printStackTrace();
        }
        return FXCollections.observableArrayList();
    }

    /**
     * Lista as despesas por categoria.
     */
    public ObservableList<Despesa> listarDespesasPorCategoria(int categoriaId) {
        return executarOperacaoLista(() -> despesaDAO.listarPorCategoria(categoriaId));
    }

    /**
     * Lista as despesas por responsável.
     */
    public ObservableList<Despesa> listarDespesasPorResponsavel(int responsavelId) {
        return executarOperacaoLista(() -> despesaDAO.listarPorResponsavel(responsavelId));
    }

    /**
     * Lista as despesas por cartão de crédito.
     */
    public ObservableList<Despesa> listarDespesasPorCartao(int cartaoId) {
        return executarOperacaoLista(() -> despesaDAO.listarPorCartao(cartaoId));
    }

    /**
     * Lista as despesas fixas.
     */
    public ObservableList<Despesa> listarDespesasFixas() {
        return executarOperacaoLista(despesaDAO::listarDespesasFixas);
    }

    /**
     * Lista as despesas parceladas.
     */
    public ObservableList<Despesa> listarDespesasParceladas() {
        return executarOperacaoLista(despesaDAO::listarDespesasParceladas);
    }

    /**
     * Obtém dados para o gráfico de despesas por categoria.
     * Retorna uma lista de pares (categoria, valor).
     */
    public List<Object[]> obterDadosGraficoPorCategoria() {
        return executarOperacao(despesaDAO::calcularTotalPorCategoria, new ArrayList<>());
    }

    /**
     * Obtém dados para o gráfico de despesas por responsável.
     * Retorna uma lista de pares (responsável, valor).
     */
    public List<Object[]> obterDadosGraficoPorResponsavel() {
        return executarOperacao(despesaDAO::calcularTotalPorResponsavel, new ArrayList<>());
    }

    /**
     * Calcula o total de despesas do mês atual.
     */
    public double calcularTotalDespesasDoMes() {
        ObservableList<Despesa> despesas = listarDespesasDoMes();
        double total = despesas.stream().mapToDouble(Despesa::getValor).sum();
        System.out.println("Total de despesas do mês: R$ " + total);
        return total;
    }

    /**
     * Calcula o total de despesas pagas do mês atual.
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

    /* ------------------------------------------------------------
       Métodos auxiliares para reduzir try-catch repetitivos
       ------------------------------------------------------------ */
    /**
     * Executa uma operação que retorna uma lista de objetos e a converte em ObservableList.
     */
    private <T> ObservableList<T> executarOperacaoLista(DAOListOperation<T> operacao) {
        try {
            return FXCollections.observableArrayList(operacao.executar());
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Executa uma operação genérica que retorna um objeto (ou valor),
     * usando um valor padrão em caso de exceção.
     */
    private <R> R executarOperacao(DAOOperation<R> operacao, R defaultValue) {
        try {
            return operacao.executar();
        } catch (SQLException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    @FunctionalInterface
    private interface DAOListOperation<T> {
        List<T> executar() throws SQLException;
    }

    @FunctionalInterface
    private interface DAOOperation<R> {
        R executar() throws SQLException;
    }

    /**
     * Classe interna (ou pode ficar externa) para representar o resultado de uma operação.
     * Mantive aqui, mas se preferir pode deixá-la fora.
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
