package com.gastos.ui;

import com.gastos.GerenciadorFinanceiroApp;
import com.gastos.controller.DespesaController;
import com.gastos.db.ConexaoBanco;
import com.gastos.model.Despesa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Paint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa a tela principal do aplicativo. Refatorada para
 * melhorar organização e reduzir duplicação de código.
 */
public class TelaPrincipal {

	// Constantes para estilo
	private static final String STYLE_BACKGROUND = "-fx-background-color: #f5f5f5;";
	private static final String STYLE_HEADER = "-fx-background-color: #3498db;";
	private static final String STYLE_SIDEBAR = "-fx-background-color: #2c3e50;";
	private static final String STYLE_FOOTER = "-fx-background-color: #ecf0f1;";
	private static final String STYLE_CARD_TOTAL = "-fx-background-color: #3498db; -fx-background-radius: 10;";
	private static final String STYLE_CARD_PAGO = "-fx-background-color: #2ecc71; -fx-background-radius: 10;";
	private static final String STYLE_CARD_A_PAGAR = "-fx-background-color: #e74c3c; -fx-background-radius: 10;";
	private static final String STYLE_PANEL = "-fx-background-color: white; -fx-background-radius: 10;";
	private static final String STYLE_BTN_PRIMARY = "-fx-background-color: #3498db; -fx-text-fill: white;";
	private static final String STYLE_BTN_SUCCESS = "-fx-background-color: #2ecc71; -fx-text-fill: white;";
	private static final String STYLE_BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white;";

	private Scene scene;
	private final DespesaController despesaController;

	// Componentes da interface
	private Label lblTotalDespesasMes;
	private Label lblTotalPago;
	private Label lblTotalAPagar;
	private TableView<Despesa> tabelaDespesasRecentes;
	private VBox painelGraficos;

	public class Main {
		public static void main(String[] args) {
			Application.launch(GerenciadorFinanceiroApp.class, args);
		}
	}

	/**
	 * Construtor da tela principal.
	 */
	public TelaPrincipal() {
		despesaController = new DespesaController();
		criarInterface();
	}

	/**
	 * Obtém a cena da tela principal.
	 * 
	 * @return a cena da tela principal
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Cria a interface da tela principal.
	 */
	private void criarInterface() {
		BorderPane painelPrincipal = new BorderPane();
		painelPrincipal.setStyle(STYLE_BACKGROUND);

		// Componentes principais
		painelPrincipal.setTop(criarCabecalho());
		painelPrincipal.setLeft(criarMenuLateral());
		painelPrincipal.setCenter(criarDashboard());
		painelPrincipal.setBottom(criarRodape());

		// Criar a cena
		scene = new Scene(painelPrincipal, 1280, 800);
	}

	/**
	 * Cria o cabeçalho da tela principal.
	 * 
	 * @return o cabeçalho
	 */
	private HBox criarCabecalho() {
		HBox cabecalho = new HBox();
		cabecalho.setAlignment(Pos.CENTER_LEFT);
		cabecalho.setPadding(new Insets(15));
		cabecalho.setStyle(STYLE_HEADER);

		Label titulo = new Label("Gerenciador Financeiro");
		titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		titulo.setTextFill(Color.WHITE);

		cabecalho.getChildren().add(titulo);
		return cabecalho;
	}

	/**
	 * Cria o menu lateral da tela principal.
	 * 
	 * @return o menu lateral
	 */
	private VBox criarMenuLateral() {
		VBox menuLateral = new VBox(10);
		menuLateral.setPadding(new Insets(15));
		menuLateral.setPrefWidth(200);
		menuLateral.setStyle(STYLE_SIDEBAR);

		// Adicionar botões de menu
		String[][] itensMenu = { { "Dashboard", "dashboard" }, { "Nova Despesa", "nova-despesa" },
				{ "Categorias", "categorias" }, { "Despesas Fixas", "despesas-fixas" },
				{ "Cartões de Crédito", "cartoes" }, { "Parcelamentos", "parcelamentos" },
				{ "Relatórios", "relatorios" }, { "Configurações", "config" } };

		for (String[] item : itensMenu) {
			Button btn = criarBotaoMenu(item[0], item[1]);
			menuLateral.getChildren().add(btn);
		}

		return menuLateral;
	}

	/**
	 * Cria um botão para o menu lateral.
	 * 
	 * @param texto o texto do botão
	 * @param id    o ID do botão
	 * @return o botão criado
	 */
	private Button criarBotaoMenu(String texto, String id) {
		final String STYLE_BTN_NORMAL = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";
		final String STYLE_BTN_HOVER = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";

		Button botao = new Button(texto);
		botao.setId(id);
		botao.setMaxWidth(Double.MAX_VALUE);
		botao.setPrefHeight(40);
		botao.setStyle(STYLE_BTN_NORMAL);

		// Efeito hover
		botao.setOnMouseEntered(e -> botao.setStyle(STYLE_BTN_HOVER));
		botao.setOnMouseExited(e -> botao.setStyle(STYLE_BTN_NORMAL));

		// Configurar evento de clique
		configurarEventoBotaoMenu(botao);

		return botao;
	}

	/**
	 * Configura o evento de clique para um botão do menu.
	 * 
	 * @param botao o botão a ser configurado
	 */
	private void configurarEventoBotaoMenu(Button botao) {
		String id = botao.getId();

		switch (id) {
		case "dashboard":
			botao.setOnAction(e -> atualizarDashboard());
			break;
		case "nova-despesa":
			botao.setOnAction(e -> abrirTelaNovaDespesa());
			break;
		case "categorias":
			botao.setOnAction(e -> abrirTelaCategorias());
			break;
		case "despesas-fixas":
		case "cartoes":
		case "relatorios":
		case "config":
			botao.setOnAction(e -> mostrarTelaEmDesenvolvimento());
			break;
		case "parcelamentos":
			botao.setOnAction(e -> abrirTelaParcelamentos());
			break;
		}
	}

	/**
	 * Cria o dashboard da aplicação.
	 * 
	 * @return o painel de rolagem contendo o dashboard
	 */
	private ScrollPane criarDashboard() {
		// Painel principal do dashboard
		VBox painelDashboard = new VBox(20);
		painelDashboard.setPadding(new Insets(20));

		// Título e data
		Label tituloDashboard = new Label("Dashboard");
		tituloDashboard.setFont(Font.font("Arial", FontWeight.BOLD, 22));

		LocalDate hoje = LocalDate.now();
		Label dataAtual = new Label("Data: " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		dataAtual.setFont(Font.font("Arial", 14));

		// Painéis principais
		HBox painelResumo = criarPainelResumoFinanceiro();
		HBox painelDespesasGraficos = criarPainelDespesasGraficos();

		// Adicionar todos os elementos ao painel do dashboard
		painelDashboard.getChildren().addAll(tituloDashboard, dataAtual, painelResumo, painelDespesasGraficos);

		// Criar painel de rolagem
		ScrollPane scrollPane = new ScrollPane(painelDashboard);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		// Carregar os dados iniciais após a interface estar pronta
		Platform.runLater(this::atualizarDashboard);

		return scrollPane;
	}

	/**
	 * Cria o painel de resumo financeiro.
	 * 
	 * @return o painel de resumo financeiro
	 */
	private HBox criarPainelResumoFinanceiro() {
		HBox painelResumo = new HBox(20);
		painelResumo.setAlignment(Pos.CENTER);

		// Cartões de resumo
		VBox cartaoTotalDespesas = criarCartaoResumo("Total Despesas do Mês", "R$ 0,00", STYLE_CARD_TOTAL);
		lblTotalDespesasMes = (Label) cartaoTotalDespesas.getChildren().get(1);

		VBox cartaoTotalPago = criarCartaoResumo("Total Pago", "R$ 0,00", STYLE_CARD_PAGO);
		lblTotalPago = (Label) cartaoTotalPago.getChildren().get(1);

		VBox cartaoTotalAPagar = criarCartaoResumo("Total a Pagar", "R$ 0,00", STYLE_CARD_A_PAGAR);
		lblTotalAPagar = (Label) cartaoTotalAPagar.getChildren().get(1);

		painelResumo.getChildren().addAll(cartaoTotalDespesas, cartaoTotalPago, cartaoTotalAPagar);
		return painelResumo;
	}

	/**
	 * Cria um cartão de resumo financeiro.
	 * 
	 * @param titulo o título do cartão
	 * @param valor  o valor inicial
	 * @param estilo o estilo do cartão
	 * @return o cartão criado
	 */
	private VBox criarCartaoResumo(String titulo, String valor, String estilo) {
		VBox cartao = new VBox(10);
		cartao.setPadding(new Insets(20));
		cartao.setPrefWidth(300);
		cartao.setPrefHeight(150);
		cartao.setStyle(estilo);
		cartao.setAlignment(Pos.CENTER);

		Label lblTitulo = new Label(titulo);
		lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		lblTitulo.setTextFill(Color.WHITE);

		Label lblValor = new Label(valor);
		lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		lblValor.setTextFill(Color.WHITE);

		cartao.getChildren().addAll(lblTitulo, lblValor);
		return cartao;
	}

	/**
	 * Cria o painel que contém as despesas recentes e os gráficos.
	 * 
	 * @return o painel de despesas e gráficos
	 */
	private HBox criarPainelDespesasGraficos() {
		HBox painel = new HBox(20);
		painel.setAlignment(Pos.TOP_CENTER);

		// Tabela de despesas recentes
		VBox painelDespesasRecentes = criarPainelDespesasRecentes();
		painelDespesasRecentes.setPrefWidth(500);

		// Painel de gráficos
		painelGraficos = criarPainelGraficos();
		painelGraficos.setPrefWidth(500);

		painel.getChildren().addAll(painelDespesasRecentes, painelGraficos);
		return painel;
	}

	/**
	 * Cria o painel de despesas recentes.
	 * 
	 * @return o painel de despesas recentes
	 */
	private VBox criarPainelDespesasRecentes() {
		VBox painel = new VBox(10);
		painel.setPadding(new Insets(20));
		painel.setStyle(STYLE_PANEL);

		Label titulo = new Label("Despesas Recentes");
		titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		// Tabela de despesas recentes
		tabelaDespesasRecentes = new TableView<>();
		configurarTabelaDespesas();

		Button btnVerTodas = new Button("Ver Todas as Despesas");
		btnVerTodas.setStyle(STYLE_BTN_PRIMARY);
		btnVerTodas.setOnAction(e -> abrirTelaTodasDespesas());

		painel.getChildren().addAll(titulo, tabelaDespesasRecentes, btnVerTodas);
		return painel;
	}

	/**
	 * Configura as colunas da tabela de despesas recentes.
	 */
	private void configurarTabelaDespesas() {
		// Coluna de descrição
		TableColumn<Despesa, String> colunaDescricao = new TableColumn<>("Descrição");
		colunaDescricao.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescricao()));
		colunaDescricao.setPrefWidth(200);

		// Coluna de valor
		TableColumn<Despesa, String> colunaValor = new TableColumn<>("Valor");
		colunaValor.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				String.format("R$ %.2f", cellData.getValue().getValor())));
		colunaValor.setPrefWidth(100);

		// Coluna de categoria
		TableColumn<Despesa, String> colunaCategoria = new TableColumn<>("Categoria");
		colunaCategoria.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getCategoria() != null ? cellData.getValue().getCategoria().getNome() : ""));
		colunaCategoria.setPrefWidth(100);

		// Coluna de vencimento
		TableColumn<Despesa, String> colunaVencimento = new TableColumn<>("Vencimento");
		colunaVencimento.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getDataVencimento() != null
						? cellData.getValue().getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
						: ""));
		colunaVencimento.setPrefWidth(100);

		// Coluna de status
		TableColumn<Despesa, String> colunaStatus = new TableColumn<>("Status");
		colunaStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().isPago() ? "Pago" : "A Pagar"));
		colunaStatus.setPrefWidth(80);

		// Adicionar colunas à tabela
		tabelaDespesasRecentes.getColumns().addAll(colunaDescricao, colunaValor, colunaCategoria, colunaVencimento,
				colunaStatus);
	}

	/**
	 * Cria o painel de gráficos.
	 * 
	 * @return o painel de gráficos
	 */
	private VBox criarPainelGraficos() {
		VBox painel = new VBox(20);
		painel.setPadding(new Insets(20));
		painel.setStyle(STYLE_PANEL);

		Label titulo = new Label("Análise de Gastos");
		titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		painel.getChildren().add(titulo);
		return painel;
	}

	/**
	 * Cria o rodapé da tela principal.
	 * 
	 * @return o rodapé
	 */
	private HBox criarRodape() {
		HBox rodape = new HBox();
		rodape.setAlignment(Pos.CENTER_RIGHT);
		rodape.setPadding(new Insets(10, 15, 10, 15));
		rodape.setStyle(STYLE_FOOTER);

		Label lblVersao = new Label("Gerenciador Financeiro v1.0");
		lblVersao.setFont(Font.font("Arial", 12));
		lblVersao.setTextFill(Color.GRAY);

		rodape.getChildren().add(lblVersao);
		return rodape;
	}

	/**
	 * Atualiza o dashboard com os dados mais recentes.
	 */
	private void atualizarDashboard() {
		try {
			// Atualizar os totais financeiros
			double totalDespesas = despesaController.calcularTotalDespesasDoMes();
			double totalPago = despesaController.calcularTotalDespesasPagasDoMes();
			double totalAPagar = despesaController.calcularTotalDespesasAPagarDoMes();

			lblTotalDespesasMes.setText(String.format("R$ %.2f", totalDespesas));
			lblTotalPago.setText(String.format("R$ %.2f", totalPago));
			lblTotalAPagar.setText(String.format("R$ %.2f", totalAPagar));

			// Obter despesas recentes
			ObservableList<Despesa> despesas = despesaController.listarDespesasDoMes();

			// Se não conseguiu, buscar diretamente
			if (despesas == null || despesas.isEmpty()) {
				despesas = buscarDespesasDiretamente();
			}

			// Atualizar a tabela
			tabelaDespesasRecentes.setItems(despesas);

			// Atualizar os gráficos
			atualizarGraficos();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Busca despesas diretamente do banco de dados como fallback.
	 */
	private ObservableList<Despesa> buscarDespesasDiretamente() {
		List<Despesa> despesas = new ArrayList<>();

		try (Connection conn = ConexaoBanco.getConexao()) {
			String sql = "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, "
					+ "c.id as categoria_id, c.nome as categoria_nome " + "FROM despesas d "
					+ "LEFT JOIN categorias c ON d.categoria_id = c.id " + "ORDER BY d.id DESC LIMIT 20";

			try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

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

					// Categoria básica
					int categoriaId = rs.getInt("categoria_id");
					if (!rs.wasNull()) {
						com.gastos.model.CategoriaDespesa categoria = new com.gastos.model.CategoriaDespesa();
						categoria.setId(categoriaId);
						categoria.setNome(rs.getString("categoria_nome"));
						despesa.setCategoria(categoria);
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
	 * Atualiza os gráficos do dashboard.
	 */
	private void atualizarGraficos() {
		// Limpar gráficos antigos
		if (painelGraficos.getChildren().size() > 1) {
			painelGraficos.getChildren().remove(1, painelGraficos.getChildren().size());
		}

		// Criar gráfico de despesas por categoria
		JFreeChart graficoCategoria = criarGraficoPizza("Despesas por Categoria",
				despesaController.obterDadosGraficoPorCategoria());
		ChartViewer viewerCategoria = new ChartViewer(graficoCategoria);
		viewerCategoria.setPrefSize(450, 300);

		// Adicionar os gráficos ao painel
		painelGraficos.getChildren().add(viewerCategoria);
	}

	/**
	 * Cria um gráfico de pizza.
	 * 
	 * @param titulo o título do gráfico
	 * @param dados  os dados para o gráfico
	 * @return o gráfico criado
	 */
	@SuppressWarnings("unchecked")
	private JFreeChart criarGraficoPizza(String titulo, List<Object[]> dados) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

		for (Object[] dado : dados) {
			String categoria = (String) dado[0];
			double valor = (double) dado[1];
			dataset.setValue(categoria, valor);
		}

		JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);

		// Personalizar o gráfico
		PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
		plot.setBackgroundPaint((Paint) java.awt.Color.WHITE);
		plot.setOutlinePaint(null);

		return chart;
	}

	/**
	 * Abre a tela de cadastro de despesas.
	 */
	private void abrirTelaNovaDespesa() {
		TelaCadastroDespesa telaNovaDespesa = new TelaCadastroDespesa();
		telaNovaDespesa.mostrar();
		Platform.runLater(this::atualizarDashboard);
	}

	/**
	 * Abre a tela de categorias.
	 */
	private void abrirTelaCategorias() {
		TelaCategorias telaCategorias = new TelaCategorias();
		telaCategorias.mostrar();
		Platform.runLater(this::atualizarDashboard);
	}

	/**
	 * Abre a tela de gerenciamento de parcelamentos.
	 */
	private void abrirTelaParcelamentos() {
		try {
			TelaParcelamentos telaParcelamentos = new TelaParcelamentos();
			telaParcelamentos.mostrar();
			Platform.runLater(this::atualizarDashboard);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarErro("Erro ao abrir tela de parcelamentos", e.getMessage());
		}
	}

	/**
	 * Abre a tela de todas as despesas.
	 */
	private void abrirTelaTodasDespesas() {
		try {
			TelaTodasDespesas telaTodasDespesas = new TelaTodasDespesas();
			telaTodasDespesas.mostrar();
			Platform.runLater(this::atualizarDashboard);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarErro("Erro ao abrir tela de todas as despesas", e.getMessage());
		}
	}

	/**
	 * Mostra um alerta de funcionalidade em desenvolvimento.
	 */
	private void mostrarTelaEmDesenvolvimento() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Em desenvolvimento");
		alert.setHeaderText(null);
		alert.setContentText("Esta funcionalidade está em desenvolvimento.");
		alert.showAndWait();
	}

	/**
	 * Mostra uma mensagem de erro.
	 */
	private void mostrarErro(String titulo, String mensagem) {
		Alert alerta = new Alert(Alert.AlertType.ERROR);
		alerta.setTitle("Erro");
		alerta.setHeaderText(titulo);
		alerta.setContentText(mensagem);
		alerta.showAndWait();
	}
}