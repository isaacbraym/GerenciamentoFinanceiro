package com.gastos.ui;

import com.gastos.controller.*;
import com.gastos.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe que representa a tela de cadastro de despesas.
 */
public class TelaCadastroDespesa {
    
    private final Stage janela;
    private final Despesa despesaAtual;
    private final boolean modoEdicao;
    
    // Controllers
    private final DespesaController despesaController;
    private final CategoriaController categoriaController;
    private final ResponsavelController responsavelController;
    private final MeioPagamentoController meioPagamentoController;
    private final CartaoCreditoController cartaoController;
    
    // Componentes da interface
    private TextField txtDescricao;
    private TextField txtValor;
    private DatePicker datePicker;
    private DatePicker datePickerVencimento;
    private CheckBox chkPago;
    private CheckBox chkFixo;
    private ComboBox<CategoriaDespesa> cmbCategoria;
    private ComboBox<SubCategoria> cmbSubCategoria;
    private ComboBox<Responsavel> cmbResponsavel;
    private ComboBox<MeioPagamento> cmbMeioPagamento;
    private ComboBox<CartaoCredito> cmbCartao;
    private Spinner<Integer> spinnerParcelas;
    private VBox painelCartao;
    private VBox painelParcelamento;
    
    /**
     * Construtor para criação de uma nova despesa.
     */
    public TelaCadastroDespesa() {
        this(new Despesa(), false);
    }
    
    /**
     * Construtor para edição de uma despesa existente.
     * @param despesa a despesa a ser editada
     */
    public TelaCadastroDespesa(Despesa despesa, boolean edicao) {
        this.despesaAtual = despesa;
        this.modoEdicao = edicao;
        
        // Inicializar controllers
        this.despesaController = new DespesaController();
        this.categoriaController = new CategoriaController();
        this.responsavelController = new ResponsavelController();
        this.meioPagamentoController = new MeioPagamentoController();
        this.cartaoController = new CartaoCreditoController();
        
        // Configurar a janela
        this.janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.setTitle(modoEdicao ? "Editar Despesa" : "Nova Despesa");
        janela.setMinWidth(600);
        janela.setMinHeight(700);
        
        // Criar a interface
        criarInterface();
    }
    
    /**
     * Exibe a janela.
     */
    public void mostrar() {
        janela.showAndWait();
    }
    
    /**
     * Cria a interface da tela de cadastro.
     */
    private void criarInterface() {
        // Painel principal
        VBox painelPrincipal = new VBox(20);
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label lblTitulo = new Label(modoEdicao ? "Editar Despesa" : "Nova Despesa");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Formulário
        GridPane formulario = criarFormulario();
        
        // Botões de ação
        HBox painelBotoes = criarPainelBotoes();
        
        // Adicionar componentes ao painel principal
        painelPrincipal.getChildren().addAll(lblTitulo, formulario, painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal);
        janela.setScene(cena);
        
        // Carregar dados para edição
        if (modoEdicao) {
            carregarDadosParaEdicao();
        }
    }
    
    /**
     * Cria o formulário de cadastro.
     * @return o painel contendo o formulário
     */
    private GridPane criarFormulario() {
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        // Descrição
        Label lblDescricao = new Label("Descrição:");
        txtDescricao = new TextField();
        txtDescricao.setPromptText("Descrição da despesa");
        txtDescricao.setPrefWidth(300);
        
        // Valor
        Label lblValor = new Label("Valor (R$):");
        txtValor = new TextField();
        txtValor.setPromptText("0,00");
        
        // Data da Compra
        Label lblData = new Label("Data da Compra:");
        datePicker = new DatePicker(LocalDate.now());
        
        // Data de Vencimento
        Label lblVencimento = new Label("Data de Vencimento:");
        datePickerVencimento = new DatePicker();
        
        // Status de Pagamento
        Label lblStatus = new Label("Status:");
        chkPago = new CheckBox("Pago");
        
        // Despesa Fixa
        Label lblFixo = new Label("Tipo:");
        chkFixo = new CheckBox("Despesa Fixa");
        
        // Categoria
        Label lblCategoria = new Label("Categoria:");
        cmbCategoria = new ComboBox<>();
        cmbCategoria.setPromptText("Selecione a categoria");
        cmbCategoria.setPrefWidth(300);
        
        // Subcategoria
        Label lblSubCategoria = new Label("Subcategoria:");
        cmbSubCategoria = new ComboBox<>();
        cmbSubCategoria.setPromptText("Selecione a subcategoria");
        cmbSubCategoria.setPrefWidth(300);
        
        // Responsável
        Label lblResponsavel = new Label("Responsável:");
        cmbResponsavel = new ComboBox<>();
        cmbResponsavel.setPromptText("Selecione o responsável");
        cmbResponsavel.setPrefWidth(300);
        
        // Meio de Pagamento
        Label lblMeioPagamento = new Label("Meio de Pagamento:");
        cmbMeioPagamento = new ComboBox<>();
        cmbMeioPagamento.setPromptText("Selecione o meio de pagamento");
        cmbMeioPagamento.setPrefWidth(300);
        
        // Painel Cartão de Crédito (exibido apenas se o meio de pagamento for cartão)
        painelCartao = new VBox(10);
        painelCartao.setVisible(false);
        
        Label lblCartao = new Label("Cartão de Crédito:");
        cmbCartao = new ComboBox<>();
        cmbCartao.setPromptText("Selecione o cartão");
        cmbCartao.setPrefWidth(300);
        
        painelCartao.getChildren().addAll(lblCartao, cmbCartao);
        
        // Painel Parcelamento
        painelParcelamento = new VBox(10);
        painelParcelamento.setVisible(false);
        
        Label lblParcelas = new Label("Número de Parcelas:");
        spinnerParcelas = new Spinner<>(1, 48, 1);
        spinnerParcelas.setEditable(true);
        spinnerParcelas.setPrefWidth(300);
        
        CheckBox chkParcelado = new CheckBox("Parcelado");
        chkParcelado.setOnAction(e -> {
            painelParcelamento.setVisible(chkParcelado.isSelected());
        });
        
        painelParcelamento.getChildren().addAll(lblParcelas, spinnerParcelas);
        
        // Carregar dados para os comboboxes
        carregarCategorias();
        carregarResponsaveis();
        carregarMeiosPagamento();
        carregarCartoes();
        
        // Adicionar eventos
        cmbCategoria.setOnAction(e -> atualizarSubcategorias());
        cmbMeioPagamento.setOnAction(e -> {
            MeioPagamento meioPagamento = cmbMeioPagamento.getValue();
            painelCartao.setVisible(meioPagamento != null && meioPagamento.isCartaoCredito());
        });
        
        // Adicionar campos ao formulário
        int row = 0;
        grid.add(lblDescricao, 0, row);
        grid.add(txtDescricao, 1, row);
        
        row++;
        grid.add(lblValor, 0, row);
        grid.add(txtValor, 1, row);
        
        row++;
        grid.add(lblData, 0, row);
        grid.add(datePicker, 1, row);
        
        row++;
        grid.add(lblVencimento, 0, row);
        grid.add(datePickerVencimento, 1, row);
        
        row++;
        grid.add(lblStatus, 0, row);
        grid.add(chkPago, 1, row);
        
        row++;
        grid.add(lblFixo, 0, row);
        grid.add(chkFixo, 1, row);
        
        row++;
        grid.add(lblCategoria, 0, row);
        grid.add(cmbCategoria, 1, row);
        
        row++;
        grid.add(lblSubCategoria, 0, row);
        grid.add(cmbSubCategoria, 1, row);
        
        row++;
        grid.add(lblResponsavel, 0, row);
        grid.add(cmbResponsavel, 1, row);
        
        row++;
        grid.add(lblMeioPagamento, 0, row);
        grid.add(cmbMeioPagamento, 1, row);
        
        row++;
        grid.add(painelCartao, 1, row);
        
        row++;
        grid.add(chkParcelado, 1, row);
        
        row++;
        grid.add(painelParcelamento, 1, row);
        
        return grid;
    }
    
    /**
     * Cria o painel de botões.
     * @return o painel de botões
     */
    private HBox criarPainelBotoes() {
        HBox painelBotoes = new HBox(15);
        painelBotoes.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnCancelar.setOnAction(e -> janela.close());
        
        Button btnSalvar = new Button("Salvar");
        btnSalvar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnSalvar.setOnAction(e -> salvarDespesa());
        
        painelBotoes.getChildren().addAll(btnCancelar, btnSalvar);
        
        return painelBotoes;
    }
    
    /**
     * Carrega as categorias para o combobox.
     */
    private void carregarCategorias() {
        System.out.println("Carregando categorias...");
        ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
        cmbCategoria.setItems(categorias);
        System.out.println("Categorias carregadas: " + categorias.size());
        
        // Se não houver categorias, exibir um alerta
        if (categorias.isEmpty()) {
            System.out.println("ALERTA: Nenhuma categoria encontrada. Verifique o banco de dados.");
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Dados Faltando");
            alerta.setHeaderText("Nenhuma categoria encontrada");
            alerta.setContentText("Não foram encontradas categorias no banco de dados. Verifique se o banco de dados foi inicializado corretamente.");
            alerta.showAndWait();
        }
    }
    
    /**
     * Atualiza as subcategorias com base na categoria selecionada.
     */
    private void atualizarSubcategorias() {
        CategoriaDespesa categoriaSelecionada = cmbCategoria.getValue();
        if (categoriaSelecionada != null) {
            ObservableList<SubCategoria> subcategorias = 
                    FXCollections.observableArrayList(categoriaSelecionada.getSubCategorias());
            cmbSubCategoria.setItems(subcategorias);
            System.out.println("Subcategorias carregadas: " + subcategorias.size() + " para categoria " + categoriaSelecionada.getNome());
        } else {
            cmbSubCategoria.getItems().clear();
        }
    }
    
    /**
     * Carrega os responsáveis para o combobox.
     */
    private void carregarResponsaveis() {
        ObservableList<Responsavel> responsaveis = responsavelController.listarTodosResponsaveis();
        cmbResponsavel.setItems(responsaveis);
        System.out.println("Responsáveis carregados: " + responsaveis.size());
    }
    
    /**
     * Carrega os meios de pagamento para o combobox.
     */
    private void carregarMeiosPagamento() {
        ObservableList<MeioPagamento> meiosPagamento = meioPagamentoController.listarTodosMeiosPagamento();
        cmbMeioPagamento.setItems(meiosPagamento);
        System.out.println("Meios de pagamento carregados: " + meiosPagamento.size());
    }
    
    /**
     * Carrega os cartões de crédito para o combobox.
     */
    private void carregarCartoes() {
        ObservableList<CartaoCredito> cartoes = cartaoController.listarTodosCartoes();
        cmbCartao.setItems(cartoes);
        System.out.println("Cartões carregados: " + cartoes.size());
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
                painelParcelamento.setVisible(true);
                spinnerParcelas.getValueFactory().setValue(despesaAtual.getParcelamento().getTotalParcelas());
            }
        }
    }
    
    /**
     * Salva a despesa no banco de dados.
     */
    private void salvarDespesa() {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }
            
            // Preencher a despesa com os dados do formulário
            despesaAtual.setDescricao(txtDescricao.getText().trim());
            despesaAtual.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));
            despesaAtual.setDataCompra(datePicker.getValue());
            despesaAtual.setDataVencimento(datePickerVencimento.getValue());
            despesaAtual.setPago(chkPago.isSelected());
            despesaAtual.setFixo(chkFixo.isSelected());
            despesaAtual.setCategoria(cmbCategoria.getValue());
            despesaAtual.setSubCategoria(cmbSubCategoria.getValue());
            despesaAtual.setResponsavel(cmbResponsavel.getValue());
            despesaAtual.setMeioPagamento(cmbMeioPagamento.getValue());
            
            // Cartão de crédito (se aplicável)
            if (painelCartao.isVisible()) {
                despesaAtual.setCartaoCredito(cmbCartao.getValue());
            } else {
                despesaAtual.setCartaoCredito(null);
            }
            
            // Parcelamento (se aplicável)
            if (painelParcelamento.isVisible()) {
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
                parcelamento.gerarParcelas(despesaAtual.getCartaoCredito());
                
                despesaAtual.setParcelamento(parcelamento);
            } else {
                despesaAtual.setParcelamento(null);
            }
            
            // Salvar a despesa
            boolean sucesso = despesaController.salvarDespesa(despesaAtual);
            
            if (sucesso) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Sucesso");
                alerta.setHeaderText(null);
                alerta.setContentText("Despesa salva com sucesso!");
                alerta.showAndWait();
                
                janela.close();
            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro");
                alerta.setHeaderText(null);
                alerta.setContentText("Erro ao salvar a despesa. Por favor, tente novamente.");
                alerta.showAndWait();
            }
            
        } catch (NumberFormatException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText(null);
            alerta.setContentText("O valor digitado não é válido. Use apenas números e separador decimal.");
            alerta.showAndWait();
        }
    }
    
    /**
     * Valida os campos do formulário.
     * @return true se todos os campos forem válidos
     */
    private boolean validarCampos() {
        StringBuilder erros = new StringBuilder();
        
        if (txtDescricao.getText().trim().isEmpty()) {
            erros.append("- A descrição é obrigatória.\n");
        }
        
        if (txtValor.getText().trim().isEmpty()) {
            erros.append("- O valor é obrigatório.\n");
        } else {
            try {
                double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
                if (valor <= 0) {
                    erros.append("- O valor deve ser maior que zero.\n");
                }
            } catch (NumberFormatException e) {
                erros.append("- O valor não é válido.\n");
            }
        }
        
        if (datePicker.getValue() == null) {
            erros.append("- A data da compra é obrigatória.\n");
        }
        
        if (cmbCategoria.getValue() == null) {
            erros.append("- A categoria é obrigatória.\n");
        }
        
        if (cmbMeioPagamento.getValue() != null && cmbMeioPagamento.getValue().isCartaoCredito() && cmbCartao.getValue() == null) {
            erros.append("- Selecione um cartão de crédito.\n");
        }
        
        if (erros.length() > 0) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Corrija os seguintes erros:");
            alerta.setContentText(erros.toString());
            alerta.showAndWait();
            return false;
        }
        
        return true;
    }
}