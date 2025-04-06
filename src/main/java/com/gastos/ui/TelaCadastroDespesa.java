package com.gastos.ui;

import com.gastos.controller.*;
import com.gastos.model.*;
import com.gastos.ui.base.BaseTelaModal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa a tela de cadastro de despesas.
 * Refatorada para usar BaseTelaModal.
 */
public class TelaCadastroDespesa extends BaseTelaModal {
    
    private final Despesa despesaAtual;
    private final boolean modoEdicao;

    // Controllers
    private final DespesaController despesaController;
    private final CategoriaController categoriaController;
    private final ResponsavelController responsavelController;
    private final MeioPagamentoController meioPagamentoController;
    private final CartaoCreditoController cartaoController;

    // Componentes da interface
    private TextField txtDescricao, txtValor;
    private DatePicker datePicker, datePickerVencimento;
    private CheckBox chkPago, chkFixo, chkParcelado;
    private ComboBox<CategoriaDespesa> cmbCategoria;
    private ComboBox<SubCategoria> cmbSubCategoria;
    private ComboBox<Responsavel> cmbResponsavel;
    private ComboBox<MeioPagamento> cmbMeioPagamento;
    private ComboBox<CartaoCredito> cmbCartao;
    private Spinner<Integer> spinnerParcelas;
    private VBox painelCartao, painelParcelamento;
    private Label lblValorParcela;
    private HBox painelStatusPagamento;

    /**
     * Construtor para nova despesa.
     */
    public TelaCadastroDespesa() {
        this(new Despesa(), false);
    }

    /**
     * Construtor para despesa existente.
     * @param despesa Despesa a ser editada
     * @param edicao Flag indicando modo de edição
     */
    public TelaCadastroDespesa(Despesa despesa, boolean edicao) {
        super(edicao ? "Editar Despesa" : "Nova Despesa", 600, 700);
        this.despesaAtual = despesa;
        this.modoEdicao = edicao;
        
        // Inicializar controllers
        this.despesaController = new DespesaController();
        this.categoriaController = new CategoriaController();
        this.responsavelController = new ResponsavelController();
        this.meioPagamentoController = new MeioPagamentoController();
        this.cartaoController = new CartaoCreditoController();

        // Carregar dados após a interface ser criada
        carregarDadosCombos();
        configurarEventos();
        controlarVisibilidadeVencimento();
        
        if (modoEdicao) {
            carregarDadosParaEdicao();
        }
    }

    /**
     * Cria o conteúdo principal.
     */
    @Override
    protected Node criarConteudoPrincipal() {
        // Criar o formulário de cadastro
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        int row = 0;
        
        // Descrição
        grid.add(new Label("Descrição:"), 0, row);
        txtDescricao = new TextField();
        txtDescricao.setPromptText("Descrição da despesa");
        txtDescricao.setPrefWidth(300);
        grid.add(txtDescricao, 1, row++);

        // Valor
        grid.add(new Label("Valor (R$):"), 0, row);
        txtValor = new TextField();
        txtValor.setPromptText("0,00");
        grid.add(txtValor, 1, row++);

        // Data da Compra
        grid.add(new Label("Data da Compra:"), 0, row);
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(150);
        grid.add(datePicker, 1, row++);

        // Status de Pagamento e Data de Vencimento
        grid.add(new Label("Status:"), 0, row);
        
        painelStatusPagamento = new HBox(5);
        chkPago = new CheckBox("Pago");
        Label lblVencimento = new Label("Vencimento:");
        datePickerVencimento = new DatePicker();
        datePickerVencimento.setPrefWidth(150);
        
        painelStatusPagamento.getChildren().addAll(chkPago, lblVencimento, datePickerVencimento);
        grid.add(painelStatusPagamento, 1, row++);

        // Despesa Fixa
        grid.add(new Label("Tipo:"), 0, row);
        chkFixo = new CheckBox("Despesa Fixa");
        grid.add(chkFixo, 1, row++);

        // Categoria
        grid.add(new Label("Categoria:"), 0, row);
        cmbCategoria = new ComboBox<>();
        cmbCategoria.setPromptText("Selecione a categoria");
        cmbCategoria.setPrefWidth(300);
        grid.add(cmbCategoria, 1, row++);

        // Subcategoria
        grid.add(new Label("Subcategoria:"), 0, row);
        cmbSubCategoria = new ComboBox<>();
        cmbSubCategoria.setPromptText("Selecione a subcategoria");
        cmbSubCategoria.setPrefWidth(300);
        grid.add(cmbSubCategoria, 1, row++);

        // Responsável
        grid.add(new Label("Responsável:"), 0, row);
        cmbResponsavel = new ComboBox<>();
        cmbResponsavel.setPromptText("Selecione o responsável");
        cmbResponsavel.setPrefWidth(300);
        grid.add(cmbResponsavel, 1, row++);

        // Meio de Pagamento
        grid.add(new Label("Meio de Pagamento:"), 0, row);
        cmbMeioPagamento = new ComboBox<>();
        cmbMeioPagamento.setPromptText("Selecione o meio de pagamento");
        cmbMeioPagamento.setPrefWidth(300);
        grid.add(cmbMeioPagamento, 1, row++);

        // Painel de Cartão de Crédito
        criarPainelCartaoCredito();
        grid.add(painelCartao, 1, row++);

        // Painel de Parcelamento
        criarPainelParcelamento();
        grid.add(painelParcelamento, 1, row++);
        
        return grid;
    }

    /**
     * Cria o painel de botões.
     */
    @Override
    protected Node criarPainelBotoes() {
        HBox painelBotoes = new HBox(15);
        painelBotoes.setAlignment(Pos.CENTER_RIGHT);
        painelBotoes.setPadding(new Insets(15, 0, 0, 0));

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> fechar());

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnSalvar.setOnAction(e -> salvarDespesa());

        painelBotoes.getChildren().addAll(btnCancelar, btnSalvar);
        return painelBotoes;
    }
    
    /**
     * Cria o painel de cartão de crédito.
     */
    private void criarPainelCartaoCredito() {
        painelCartao = new VBox(10);
        painelCartao.setVisible(false);
        
        Label lblCartao = new Label("Cartão de Crédito:");
        cmbCartao = new ComboBox<>();
        cmbCartao.setPromptText("Selecione o cartão");
        cmbCartao.setPrefWidth(300);
        
        chkParcelado = new CheckBox("Parcelado");
        
        painelCartao.getChildren().addAll(lblCartao, cmbCartao, chkParcelado);
    }
    
    /**
     * Cria o painel de parcelamento.
     */
    private void criarPainelParcelamento() {
        painelParcelamento = new VBox(10);
        painelParcelamento.setVisible(false);
        
        Label lblParcelas = new Label("Número de Parcelas:");
        spinnerParcelas = new Spinner<>(1, 48, 1);
        spinnerParcelas.setEditable(true);
        spinnerParcelas.setPrefWidth(300);
        
        lblValorParcela = new Label("Valor de cada parcela: R$ 0,00");
        lblValorParcela.setStyle("-fx-font-weight: bold;");
        
        painelParcelamento.getChildren().addAll(lblParcelas, spinnerParcelas, lblValorParcela);
    }

    /**
     * Controla a visibilidade do campo de vencimento.
     */
    private void controlarVisibilidadeVencimento() {
        boolean estaPago = chkPago.isSelected();
        for (int i = 1; i < painelStatusPagamento.getChildren().size(); i++) {
            painelStatusPagamento.getChildren().get(i).setVisible(!estaPago);
        }
    }

    /**
     * Configura os eventos dos componentes.
     */
    private void configurarEventos() {
        // Atualizar valor da parcela quando o valor mudar
        txtValor.textProperty().addListener((obs, oldVal, newVal) -> atualizarValorParcela());

        // Atualizar subcategorias quando categoria muda
        cmbCategoria.setOnAction(e -> atualizarSubcategorias());

        // Mostrar/ocultar cartão e parcelamento
        cmbMeioPagamento.setOnAction(e -> {
            MeioPagamento meioPagamento = cmbMeioPagamento.getValue();
            boolean isCartao = meioPagamento != null && meioPagamento.isCartaoCredito();
            painelCartao.setVisible(isCartao);
            
            if (!isCartao) {
                chkParcelado.setSelected(false);
                painelParcelamento.setVisible(false);
            }
        });

        // Evento para controlar a visibilidade da data de vencimento
        chkPago.setOnAction(e -> controlarVisibilidadeVencimento());
        
        // Atualizar valor da parcela quando cartão muda
        cmbCartao.setOnAction(e -> {
            if (chkParcelado.isSelected()) {
                atualizarValorParcela();
            }
        });

        // Mostrar/ocultar painel de parcelamento
        chkParcelado.setOnAction(e -> {
            painelParcelamento.setVisible(chkParcelado.isSelected());
            if (chkParcelado.isSelected()) {
                atualizarValorParcela();
            }
        });

        // Atualizar valor parcela quando parcelas mudam
        spinnerParcelas.valueProperty().addListener((obs, oldVal, newVal) -> atualizarValorParcela());
    }
    
    /**
     * Atualiza o valor de cada parcela.
     */
    private void atualizarValorParcela() {
        try {
            double valorTotal = Double.parseDouble(txtValor.getText().replace(",", "."));
            int numeroParcelas = spinnerParcelas.getValue();

            if (valorTotal > 0 && numeroParcelas > 0) {
                double valorParcela = Math.round((valorTotal / numeroParcelas) * 100.0) / 100.0;
                lblValorParcela.setText("Valor de cada parcela: R$ " + String.format("%.2f", valorParcela));
            } else {
                lblValorParcela.setText("Valor de cada parcela: R$ 0,00");
            }
        } catch (NumberFormatException e) {
            lblValorParcela.setText("Valor de cada parcela: R$ 0,00");
        }
    }

    /**
     * Carrega os dados nos comboboxes.
     */
    private void carregarDadosCombos() {
        carregarCategorias();
        carregarResponsaveis();
        carregarMeiosPagamento();
        carregarCartoes();
    }
    
    /**
     * Carrega as categorias no combobox.
     */
    private void carregarCategorias() {
        ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
        cmbCategoria.setItems(categorias);

        if (categorias.isEmpty()) {
            exibirAviso("Dados Faltando", "Nenhuma categoria encontrada no banco de dados.");
        }
    }

    /**
     * Atualiza as subcategorias com base na categoria selecionada.
     */
    private void atualizarSubcategorias() {
        CategoriaDespesa categoriaSelecionada = cmbCategoria.getValue();
        if (categoriaSelecionada != null) {
            ObservableList<SubCategoria> subcategorias = FXCollections
                    .observableArrayList(categoriaSelecionada.getSubCategorias());
            cmbSubCategoria.setItems(subcategorias);
        } else {
            cmbSubCategoria.getItems().clear();
        }
    }

    /**
     * Carrega os responsáveis no combobox.
     */
    private void carregarResponsaveis() {
        ObservableList<Responsavel> responsaveis = responsavelController.listarTodosResponsaveis();
        cmbResponsavel.setItems(responsaveis);
    }

    /**
     * Carrega os meios de pagamento no combobox.
     */
    private void carregarMeiosPagamento() {
        ObservableList<MeioPagamento> meiosPagamento = meioPagamentoController.listarTodosMeiosPagamento();
        cmbMeioPagamento.setItems(meiosPagamento);
    }

    /**
     * Carrega os cartões de crédito no combobox.
     */
    private void carregarCartoes() {
        ObservableList<CartaoCredito> cartoes = cartaoController.listarTodosCartoes();
        cmbCartao.setItems(cartoes);
    }

    /**
     * Carrega os dados da despesa para edição.
     */
    private void carregarDadosParaEdicao() {
        if (despesaAtual != null) {
            txtDescricao.setText(despesaAtual.getDescricao());
            txtValor.setText(String.format("%.2f", despesaAtual.getValor()));

            if (despesaAtual.getDataCompra() != null) {
                datePicker.setValue(despesaAtual.getDataCompra());
            }

            if (despesaAtual.getDataVencimento() != null) {
                datePickerVencimento.setValue(despesaAtual.getDataVencimento());
            }

            chkPago.setSelected(despesaAtual.isPago());
            controlarVisibilidadeVencimento();

            chkFixo.setSelected(despesaAtual.isFixo());

            if (despesaAtual.getCategoria() != null) {
                cmbCategoria.setValue(despesaAtual.getCategoria());
                atualizarSubcategorias();

                if (despesaAtual.getSubCategoria() != null) {
                    cmbSubCategoria.setValue(despesaAtual.getSubCategoria());
                }
            }

            if (despesaAtual.getResponsavel() != null) {
                cmbResponsavel.setValue(despesaAtual.getResponsavel());
            }

            if (despesaAtual.getMeioPagamento() != null) {
                cmbMeioPagamento.setValue(despesaAtual.getMeioPagamento());

                if (despesaAtual.getMeioPagamento().isCartaoCredito() && despesaAtual.getCartaoCredito() != null) {
                    painelCartao.setVisible(true);
                    cmbCartao.setValue(despesaAtual.getCartaoCredito());
                }
            }

            if (despesaAtual.getParcelamento() != null) {
                // Configurar parcelamento se for cartão de crédito
                if (painelCartao.isVisible()) {
                    chkParcelado.setSelected(true);
                    painelParcelamento.setVisible(true);
                    spinnerParcelas.getValueFactory().setValue(despesaAtual.getParcelamento().getTotalParcelas());
                    atualizarValorParcela();
                }
            }
        }
    }

    /**
     * Salva a despesa no banco de dados.
     */
    private void salvarDespesa() {
        try {
            // Preencher a despesa com os dados do formulário
            preencherDespesa();
            
            // Validar a despesa
            List<String> erros = validarDespesa();
            if (!erros.isEmpty()) {
                exibirErro("Erro de Validação", construirMensagemErros(erros));
                return;
            }
            
            // Processar parcelamento se necessário
            if (chkParcelado.isSelected() && painelCartao.isVisible()) {
                criarParcelamento();
            } else {
                despesaAtual.setParcelamento(null);
            }

            // Salvar a despesa
            DespesaController.Resultado resultado = despesaController.salvarDespesa(despesaAtual);

            if (resultado.isSucesso()) {
                exibirInformacao("Sucesso", resultado.getMensagem());
                fechar();
            } else {
                exibirErroDetalhado("Erro ao Salvar Despesa", resultado.getMensagem());
            }
        } catch (Exception e) {
            e.printStackTrace();
            exibirErro("Erro", "Ocorreu um erro inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Preenche o objeto despesa com os dados do formulário.
     */
    private void preencherDespesa() {
        despesaAtual.setDescricao(txtDescricao.getText().trim());
        
        try {
            despesaAtual.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O valor informado não é válido.");
        }
        
        despesaAtual.setDataCompra(datePicker.getValue());
        
        // Apenas usa a data de vencimento se não estiver pago
        if (!chkPago.isSelected()) {
            despesaAtual.setDataVencimento(datePickerVencimento.getValue());
        } else {
            despesaAtual.setDataVencimento(null);
        }
        
        despesaAtual.setPago(chkPago.isSelected());
        despesaAtual.setFixo(chkFixo.isSelected());
        despesaAtual.setCategoria(cmbCategoria.getValue());
        despesaAtual.setSubCategoria(cmbSubCategoria.getValue());
        despesaAtual.setResponsavel(cmbResponsavel.getValue());
        despesaAtual.setMeioPagamento(cmbMeioPagamento.getValue());
        
        // Cartão de crédito
        if (painelCartao.isVisible()) {
            despesaAtual.setCartaoCredito(cmbCartao.getValue());
        } else {
            despesaAtual.setCartaoCredito(null);
        }
    }
    
    /**
     * Valida os dados da despesa.
     * @return Lista de erros encontrados
     */
    private List<String> validarDespesa() {
        List<String> erros = new ArrayList<>();
        
        if (txtDescricao.getText().trim().isEmpty()) {
            erros.add("A descrição é obrigatória");
        }
        
        if (cmbCategoria.getValue() == null) {
            erros.add("A categoria é obrigatória");
        }
        
        if (painelCartao.isVisible() && cmbCartao.getValue() == null) {
            erros.add("É necessário selecionar um cartão de crédito");
        }
        
        return erros;
    }
    
    /**
     * Cria ou atualiza o parcelamento da despesa.
     */
    private void criarParcelamento() {
        Parcelamento parcelamento;
        
        if (despesaAtual.getParcelamento() == null) {
            parcelamento = new Parcelamento();
            parcelamento.setDataInicio(despesaAtual.getDataCompra());
        } else {
            parcelamento = despesaAtual.getParcelamento();
        }
        
        parcelamento.setValorTotal(despesaAtual.getValor());
        parcelamento.setTotalParcelas(spinnerParcelas.getValue());
        parcelamento.setParcelasRestantes(spinnerParcelas.getValue());
        
        // Gerar as parcelas
        parcelamento.gerarParcelas(despesaAtual.getCartaoCredito());
        despesaAtual.setParcelamento(parcelamento);
    }
    
    /**
     * Constrói uma mensagem de erro a partir da lista de erros.
     */
    private String construirMensagemErros(List<String> erros) {
        StringBuilder mensagem = new StringBuilder("Erros na validação da despesa:\n");
        for (String erro : erros) {
            mensagem.append("- ").append(erro).append("\n");
        }
        return mensagem.toString();
    }

    /**
     * Exibe um erro detalhado com área de texto.
     */
    private void exibirErroDetalhado(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText("Ocorreu um erro ao salvar a despesa");
        alerta.initOwner(stage);

        TextArea textArea = new TextArea(mensagem);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(550);
        textArea.setPrefHeight(200);

        alerta.getDialogPane().setContent(textArea);
        alerta.showAndWait();
    }
}