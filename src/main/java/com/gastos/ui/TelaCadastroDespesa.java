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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Classe que representa a tela de cadastro de despesas.
 */
public class TelaCadastroDespesa {
	private final Stage janela;
	private final Despesa despesaAtual;
	private final boolean modoEdicao;

	// Controllers
	private final DespesaController despesaController = new DespesaController();
	private final CategoriaController categoriaController = new CategoriaController();
	private final ResponsavelController responsavelController = new ResponsavelController();
	private final MeioPagamentoController meioPagamentoController = new MeioPagamentoController();
	private final CartaoCreditoController cartaoController = new CartaoCreditoController();

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

	public TelaCadastroDespesa() {
		this(new Despesa(), false);
	}

	public TelaCadastroDespesa(Despesa despesa, boolean edicao) {
		this.despesaAtual = despesa;
		this.modoEdicao = edicao;

		// Configurar a janela
		this.janela = new Stage();
		janela.initModality(Modality.APPLICATION_MODAL);
		janela.setTitle(modoEdicao ? "Editar Despesa" : "Nova Despesa");
		janela.setMinWidth(600);
		janela.setMinHeight(700);

		criarInterface();
	}

	public void mostrar() {
		janela.showAndWait();
	}

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
		HBox painelBotoes = new HBox(15);
		painelBotoes.setAlignment(Pos.CENTER_RIGHT);

		Button btnCancelar = new Button("Cancelar");
		btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
		btnCancelar.setOnAction(e -> janela.close());

		Button btnSalvar = new Button("Salvar");
		btnSalvar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
		btnSalvar.setOnAction(e -> salvarDespesa());

		painelBotoes.getChildren().addAll(btnCancelar, btnSalvar);

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

		// Status de Pagamento e Data de Vencimento (lado a lado)
		Label lblStatus = new Label("Status:");
		chkPago = new CheckBox("Pago");

		// Criação do painel para status e vencimento
		painelStatusPagamento = new HBox(15);

		// Data de Vencimento (aparece somente quando não está pago)
		Label lblVencimento = new Label("Vencimento:");
		datePickerVencimento = new DatePicker();

		// Adicionar componentes no painel de status
		painelStatusPagamento.getChildren().addAll(chkPago, lblVencimento, datePickerVencimento);

		// Evento para controlar a visibilidade da data de vencimento
		chkPago.setOnAction(e -> controlarVisibilidadeVencimento());

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
		painelCartao.setSpacing(10);

		Label lblCartao = new Label("Cartão de Crédito:");
		cmbCartao = new ComboBox<>();
		cmbCartao.setPromptText("Selecione o cartão");
		cmbCartao.setPrefWidth(300);

		// Checkbox de parcelamento (agora dentro do painel de cartão)
		chkParcelado = new CheckBox("Parcelado");

		painelCartao.getChildren().addAll(lblCartao, cmbCartao, chkParcelado);

		// Painel Parcelamento
		painelParcelamento = new VBox(10);
		painelParcelamento.setVisible(false);

		Label lblParcelas = new Label("Número de Parcelas:");
		spinnerParcelas = new Spinner<>(1, 48, 1);
		spinnerParcelas.setEditable(true);
		spinnerParcelas.setPrefWidth(300);

		lblValorParcela = new Label("Valor de cada parcela: R$ 0,00");
		lblValorParcela.setStyle("-fx-font-weight: bold;");

		painelParcelamento.getChildren().addAll(lblParcelas, spinnerParcelas, lblValorParcela);

		// Adicionando os campos ao grid
		int row = 0;
		grid.add(lblDescricao, 0, row);
		grid.add(txtDescricao, 1, row++);

		grid.add(lblValor, 0, row);
		grid.add(txtValor, 1, row++);

		grid.add(lblData, 0, row);
		grid.add(datePicker, 1, row++);

		grid.add(lblStatus, 0, row);
		grid.add(painelStatusPagamento, 1, row++);

		grid.add(lblFixo, 0, row);
		grid.add(chkFixo, 1, row++);

		grid.add(lblCategoria, 0, row);
		grid.add(cmbCategoria, 1, row++);

		grid.add(lblSubCategoria, 0, row);
		grid.add(cmbSubCategoria, 1, row++);

		grid.add(lblResponsavel, 0, row);
		grid.add(cmbResponsavel, 1, row++);

		grid.add(lblMeioPagamento, 0, row);
		grid.add(cmbMeioPagamento, 1, row++);

		grid.add(painelCartao, 1, row++);

		grid.add(painelParcelamento, 1, row++);

		// Carregar dados para os comboboxes
		carregarCategorias();
		carregarResponsaveis();
		carregarMeiosPagamento();
		carregarCartoes();

		// Configurar eventos
		configurarEventos();

		// Configurar visibilidade inicial do campo de vencimento
		controlarVisibilidadeVencimento();

		return grid;
	}

	/**
	 * Controla a visibilidade do campo de data de vencimento com base no status de
	 * pagamento
	 */
	private void controlarVisibilidadeVencimento() {
		boolean estaPago = chkPago.isSelected();

		// Se estiver pago, esconde o campo de vencimento
		// Se não estiver pago, mostra o campo de vencimento
		for (int i = 1; i < painelStatusPagamento.getChildren().size(); i++) {
			painelStatusPagamento.getChildren().get(i).setVisible(!estaPago);
		}
	}

	private void configurarEventos() {
		// Atualizar valor parcela quando o valor mudar
		txtValor.textProperty().addListener((obs, oldVal, newVal) -> atualizarValorParcela());

		// Atualizar subcategorias quando categoria muda
		cmbCategoria.setOnAction(e -> atualizarSubcategorias());

		// Mostrar/ocultar cartão e parcelamento quando meio de pagamento muda
		cmbMeioPagamento.setOnAction(e -> {
			MeioPagamento meioPagamento = cmbMeioPagamento.getValue();
			boolean isCartao = meioPagamento != null && meioPagamento.isCartaoCredito();

			painelCartao.setVisible(isCartao);

			// Se não for cartão, desabilitar parcelamento
			if (!isCartao) {
				chkParcelado.setSelected(false);
				painelParcelamento.setVisible(false);
			}
		});

		// Atualizar valor parcela quando cartão muda
		cmbCartao.setOnAction(e -> {
			if (chkParcelado.isSelected()) {
				atualizarValorParcela();
			}
		});

		// Mostrar/ocultar painel parcelamento
		chkParcelado.setOnAction(e -> {
			boolean parcelado = chkParcelado.isSelected();
			painelParcelamento.setVisible(parcelado);

			if (parcelado) {
				atualizarValorParcela();
			}
		});

		// Atualizar valor parcela quando número de parcelas muda
		spinnerParcelas.valueProperty().addListener((obs, oldVal, newVal) -> atualizarValorParcela());
	}

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

	private void carregarCategorias() {
		ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
		cmbCategoria.setItems(categorias);

		// Se não houver categorias, exibir um alerta
		if (categorias.isEmpty()) {
			Alert alerta = new Alert(Alert.AlertType.WARNING);
			alerta.setTitle("Dados Faltando");
			alerta.setHeaderText("Nenhuma categoria encontrada");
			alerta.setContentText("Não foram encontradas categorias no banco de dados.");
			alerta.showAndWait();
		}
	}

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

	private void carregarResponsaveis() {
		ObservableList<Responsavel> responsaveis = responsavelController.listarTodosResponsaveis();
		cmbResponsavel.setItems(responsaveis);
	}

	private void carregarMeiosPagamento() {
		ObservableList<MeioPagamento> meiosPagamento = meioPagamentoController.listarTodosMeiosPagamento();
		cmbMeioPagamento.setItems(meiosPagamento);
	}

	private void carregarCartoes() {
		ObservableList<CartaoCredito> cartoes = cartaoController.listarTodosCartoes();
		cmbCartao.setItems(cartoes);
	}

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
			controlarVisibilidadeVencimento(); // Atualiza visibilidade do vencimento

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
	            // Apenas configura o parcelamento se for cartão de crédito
	            if (painelCartao.isVisible()) {
	                chkParcelado.setSelected(true);
	                painelParcelamento.setVisible(true);
	                spinnerParcelas.getValueFactory().setValue(despesaAtual.getParcelamento().getTotalParcelas());
	                atualizarValorParcela();
	            }
	        }
		}
	}

	private void salvarDespesa() {
		try {
			// Preencher a despesa com os dados do formulário
			despesaAtual.setDescricao(txtDescricao.getText().trim());

			try {
				despesaAtual.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));
			} catch (NumberFormatException e) {
				exibirAlerta(Alert.AlertType.ERROR, "Erro", "O valor informado não é válido.");
				return;
			}

			despesaAtual.setDataCompra(datePicker.getValue());

			// Apenas usa a data de vencimento se não estiver pago
			if (!chkPago.isSelected()) {
				despesaAtual.setDataVencimento(datePickerVencimento.getValue());
			} else {
				despesaAtual.setDataVencimento(null); // Remove data de vencimento se estiver pago
			}

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
			if (chkParcelado.isSelected()) {
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
				try {
					parcelamento.gerarParcelas(despesaAtual.getCartaoCredito());
				} catch (Exception e) {
					exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao gerar parcelas: " + e.getMessage());
					return;
				}

				despesaAtual.setParcelamento(parcelamento);
			} else {
				despesaAtual.setParcelamento(null);
			}

			// Validar campos obrigatórios
			if (txtDescricao.getText().trim().isEmpty()) {
				exibirAlerta(Alert.AlertType.ERROR, "Erro", "A descrição é obrigatória");
				return;
			}

			if (cmbCategoria.getValue() == null) {
				exibirAlerta(Alert.AlertType.ERROR, "Erro", "A categoria é obrigatória");
				return;
			}

			if (painelCartao.isVisible() && cmbCartao.getValue() == null) {
				exibirAlerta(Alert.AlertType.ERROR, "Erro", "É necessário selecionar um cartão de crédito");
				return;
			}

			// Salvar a despesa
			DespesaController.Resultado resultado = despesaController.salvarDespesa(despesaAtual);

			if (resultado.isSucesso()) {
				exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", resultado.getMensagem());
				janela.close();
			} else {
				exibirErroDetalhado("Erro ao Salvar Despesa", resultado.getMensagem());
			}

		} catch (Exception e) {
			e.printStackTrace();
			exibirAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro inesperado: " + e.getMessage());
		}
	}

	private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
		Alert alerta = new Alert(tipo);
		alerta.setTitle(titulo);
		alerta.setHeaderText(null);
		alerta.setContentText(mensagem);
		alerta.showAndWait();
	}

	private void exibirErroDetalhado(String titulo, String mensagem) {
		Alert alerta = new Alert(Alert.AlertType.ERROR);
		alerta.setTitle(titulo);
		alerta.setHeaderText("Ocorreu um erro ao salvar a despesa");

		TextArea textArea = new TextArea(mensagem);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setPrefWidth(550);
		textArea.setPrefHeight(200);

		alerta.getDialogPane().setContent(textArea);
		alerta.showAndWait();
	}
}