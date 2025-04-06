package com.gastos.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gastos.db.ConexaoBanco;
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
     * Salva uma nova despesa no sistema.
     * @param despesa a despesa a ser salva
     * @return um objeto Resultado contendo status e mensagem
     */
    public Resultado salvarDespesa(Despesa despesa) {
        try {
            // Validações antes de salvar
            List<String> erros = validarDespesa(despesa);
            if (!erros.isEmpty()) {
                return new Resultado(false, construirMensagemErros(erros));
            }

            // Verificar se é uma nova despesa ou uma atualização
            if (despesa.getId() == 0) {
                int id = despesaDAO.inserir(despesa);
                despesa.setId(id);
            } else {
                despesaDAO.atualizar(despesa);
            }
            return new Resultado(true, "Despesa salva com sucesso!");
        } catch (SQLException e) {
            return new Resultado(false, montarMensagemErro(e, despesa));
        }
    }

    /**
     * Constrói mensagem de erro a partir da lista de erros
     */
    private String construirMensagemErros(List<String> erros) {
        StringBuilder mensagemErro = new StringBuilder("Erros na validação da despesa:\n");
        for (String erro : erros) {
            mensagemErro.append("- ").append(erro).append("\n");
        }
        return mensagemErro.toString();
    }

    /**
     * Monta uma mensagem de erro detalhada
     */
    private String montarMensagemErro(SQLException e, Despesa despesa) {
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

        return mensagemErro;
    }

    /**
     * Valida os dados da despesa antes de salvar.
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

        validarParcelamento(despesa, erros);

        return erros;
    }
    
    /**
     * Valida dados de parcelamento
     */
    private void validarParcelamento(Despesa despesa, List<String> erros) {
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
    }

    /**
     * Exclui uma despesa do sistema.
     */
    public boolean excluirDespesa(int id) {
        try {
            // Verificar primeiro se a despesa tem parcelamento
            Despesa despesa = buscarDespesaPorId(id);
            if (despesa != null && despesa.getParcelamento() != null) {
                // Definir o ID do parcelamento como null na despesa antes de excluí-la
                // para evitar problemas com restrições de chave estrangeira
                String updateSQL = "UPDATE despesas SET parcelamento_id = NULL WHERE id = ?";
                try (Connection conn = ConexaoBanco.getConexao();
                     PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
            }
            
            // Agora podemos excluir a despesa com segurança
            despesaDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca uma despesa pelo ID.
     */
    public Despesa buscarDespesaPorId(int id) {
        try {
            return despesaDAO.buscarPorId(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lista todas as despesas do sistema.
     */
    public ObservableList<Despesa> listarTodasDespesas() {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarTodas());
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Lista as despesas do mês atual.
     */
    public ObservableList<Despesa> listarDespesasDoMes() {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarDespesasDoMes());
        } catch (SQLException e) {
            e.printStackTrace();
            return filtrarDespesasPorMesAtual(listarTodasDespesas());
        }
    }

    /**
     * Filtra despesas por mês atual como fallback
     */
    private ObservableList<Despesa> filtrarDespesasPorMesAtual(ObservableList<Despesa> todasDespesas) {
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

        return FXCollections.observableArrayList(despesasDoMes);
    }

    /**
     * Lista as despesas por categoria.
     */
    public ObservableList<Despesa> listarDespesasPorCategoria(int categoriaId) {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarPorCategoria(categoriaId));
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Lista as despesas por responsável.
     */
    public ObservableList<Despesa> listarDespesasPorResponsavel(int responsavelId) {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarPorResponsavel(responsavelId));
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Lista as despesas por cartão de crédito.
     */
    public ObservableList<Despesa> listarDespesasPorCartao(int cartaoId) {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarPorCartao(cartaoId));
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Lista as despesas fixas.
     */
    public ObservableList<Despesa> listarDespesasFixas() {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarDespesasFixas());
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Lista as despesas parceladas.
     */
    public ObservableList<Despesa> listarDespesasParceladas() {
        try {
            return FXCollections.observableArrayList(despesaDAO.listarDespesasParceladas());
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Obtém dados para o gráfico de despesas por categoria.
     */
    public List<Object[]> obterDadosGraficoPorCategoria() {
        try {
            return despesaDAO.calcularTotalPorCategoria();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtém dados para o gráfico de despesas por responsável.
     */
    public List<Object[]> obterDadosGraficoPorResponsavel() {
        try {
            return despesaDAO.calcularTotalPorResponsavel();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Calcula o total de despesas do mês atual.
     */
    public double calcularTotalDespesasDoMes() {
        return listarDespesasDoMes().stream()
                .mapToDouble(Despesa::getValor)
                .sum();
    }

    /**
     * Calcula o total de despesas pagas do mês atual.
     */
    public double calcularTotalDespesasPagasDoMes() {
        return listarDespesasDoMes().stream()
                .filter(Despesa::isPago)
                .mapToDouble(Despesa::getValor)
                .sum();
    }

    /**
     * Calcula o total de despesas a pagar do mês atual.
     */
    public double calcularTotalDespesasAPagarDoMes() {
        return listarDespesasDoMes().stream()
                .filter(d -> !d.isPago())
                .mapToDouble(Despesa::getValor)
                .sum();
    }

    /**
     * Classe para representar o resultado de uma operação.
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