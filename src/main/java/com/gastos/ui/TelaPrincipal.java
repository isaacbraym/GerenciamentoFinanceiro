package com.gastos.ui;

import com.gastos.GerenciadorFinanceiroApp;
import com.gastos.controller.DespesaController;
import com.gastos.model.Despesa;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe que representa a tela principal do aplicativo.
 */
public class TelaPrincipal {

	private Scene scene;
	private final DespesaController despesaController;

	// Componentes da interface
	private Label lblTotalDespesasMes;
	private Label lblTotalPago;
	private Label lblTotalAPagar;
	private TableView<Despesa> tabelaDespesasRecentes;
	private VBox painelGraficos;

	public class Main {
		/**
		 * Método principal que inicia a aplicação.
		 * 
		 * @param args argumentos da linha de comando
		 */
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
		// Painel principal
		BorderPane painelPrincipal = new BorderPane();
		painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");

		// Cabeçalho
		HBox cabecalho = criarCabecalho();
		painelPrincipal.setTop(cabecalho);

		// Menu lateral
		VBox menuLateral = criarMenuLateral();
		painelPrincipal.setLeft(menuLateral);

		// Conteúdo principal (Dashboard)
		ScrollPane conteudoPrincipal = criarDashboard();
		painelPrincipal.setCenter(conteudoPrincipal);

		// Rodapé
		HBox rodape = criarRodape();
		painelPrincipal.setBottom(rodape);

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
		cabecalho.setStyle("-fx-background-color: #3498db;");

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
		menuLateral.setStyle("-fx-background-color: #2c3e50;");

		// Botões do menu
		Button btnDashboard = criarBotaoMenu("Dashboard", "dashboard");
		Button btnNovasDespesas = criarBotaoMenu("Nova Despesa", "nova-despesa");
		Button btnCategorias = criarBotaoMenu("Categorias", "categorias");
		Button btnDespesasFixas = criarBotaoMenu("Despesas Fixas", "despesas-fixas");
		Button btnCartoes = criarBotaoMenu("Cartões de Crédito", "cartoes");
		Button btnParcelamentos = criarBotaoMenu("Parcelamentos", "parcelamentos");
		Button btnRelatorios = criarBotaoMenu("Relatórios", "relatorios");
		Button btnConfig = criarBotaoMenu("Configurações", "config");

		// Configurar os eventos dos botões
		btnDashboard.setOnAction(e -> atualizarDashboard());
		btnNovasDespesas.setOnAction(e -> abrirTelaNovaDespesa());
		btnCategorias.setOnAction(e -> abrirTelaCategorias());
		btnDespesasFixas.setOnAction(e -> abrirTelaDespesasFixas());
		btnCartoes.setOnAction(e -> abrirTelaCartoes());
		btnParcelamentos.setOnAction(e -> abrirTelaParcelamentos());
		btnRelatorios.setOnAction(e -> abrirTelaRelatorios());
		btnConfig.setOnAction(e -> abrirTelaConfiguracoes());

		menuLateral.getChildren().addAll(btnDashboard, btnNovasDespesas, btnCategorias, btnDespesasFixas, btnCartoes,
				btnParcelamentos, btnRelatorios, btnConfig);

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
		Button botao = new Button(texto);
		botao.setId(id);
		botao.setMaxWidth(Double.MAX_VALUE);
		botao.setPrefHeight(40);
		botao.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;");

		// Efeito hover
		botao.setOnMouseEntered(e -> botao
				.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;"));
		botao.setOnMouseExited(e -> botao
				.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;"));

		return botao;
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

		// Título do dashboard
		Label tituloDashboard = new Label("Dashboard");
		tituloDashboard.setFont(Font.font("Arial", FontWeight.BOLD, 22));

		// Data atual
		LocalDate hoje = LocalDate.now();
		Label dataAtual = new Label("Data: " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		dataAtual.setFont(Font.font("Arial", 14));

		// Painel de resumo financeiro
		HBox painelResumo = criarPainelResumoFinanceiro();

		// Painel de despesas recentes e gráficos
		HBox painelDespesasGraficos = new HBox(20);
		painelDespesasGraficos.setAlignment(Pos.TOP_CENTER);

		// Tabela de despesas recentes
		VBox painelDespesasRecentes = criarPainelDespesasRecentes();
		painelDespesasRecentes.setPrefWidth(500);

		// Painel de gráficos
		painelGraficos = criarPainelGraficos();
		painelGraficos.setPrefWidth(500);

		painelDespesasGraficos.getChildren().addAll(painelDespesasRecentes, painelGraficos);

		// Adicionar todos os elementos ao painel do dashboard
		painelDashboard.getChildren().addAll(tituloDashboard, dataAtual, painelResumo, painelDespesasGraficos);

		// Criar painel de rolagem
		ScrollPane scrollPane = new ScrollPane(painelDashboard);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);

		// Carregar os dados iniciais
		atualizarDashboard();

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

		// Cartão de total de despesas do mês
		VBox cartaoTotalDespesas = criarCartaoResumo("Total Despesas do Mês", "R$ 0,00", "#3498db");
		lblTotalDespesasMes = (Label) cartaoTotalDespesas.getChildren().get(1);

		// Cartão de total pago
		VBox cartaoTotalPago = criarCartaoResumo("Total Pago", "R$ 0,00", "#2ecc71");
		lblTotalPago = (Label) cartaoTotalPago.getChildren().get(1);

		// Cartão de total a pagar
		VBox cartaoTotalAPagar = criarCartaoResumo("Total a Pagar", "R$ 0,00", "#e74c3c");
		lblTotalAPagar = (Label) cartaoTotalAPagar.getChildren().get(1);

		painelResumo.getChildren().addAll(cartaoTotalDespesas, cartaoTotalPago, cartaoTotalAPagar);

		return painelResumo;
	}

	/**
	 * Cria um cartão de resumo financeiro.
	 * 
	 * @param titulo o título do cartão
	 * @param valor  o valor inicial
	 * @param cor    a cor de fundo
	 * @return o cartão criado
	 */
	private VBox criarCartaoResumo(String titulo, String valor, String cor) {
		VBox cartao = new VBox(10);
		cartao.setPadding(new Insets(20));
		cartao.setPrefWidth(300);
		cartao.setPrefHeight(150);
		cartao.setStyle("-fx-background-color: " + cor + "; -fx-background-radius: 10;");
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
	 * Cria o painel de despesas recentes.
	 * 
	 * @return o painel de despesas recentes
	 */
	private VBox criarPainelDespesasRecentes() {
		VBox painel = new VBox(10);
		painel.setPadding(new Insets(20));
		painel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

		Label titulo = new Label("Despesas Recentes");
		titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		// Tabela de despesas recentes
		tabelaDespesasRecentes = new TableView<>();

		// Definir colunas
		TableColumn<Despesa, String> colunaDescricao = new TableColumn<>("Descrição");
		colunaDescricao.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescricao()));
		colunaDescricao.setPrefWidth(200);

		TableColumn<Despesa, String> colunaValor = new TableColumn<>("Valor");
		colunaValor.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				String.format("R$ %.2f", cellData.getValue().getValor())));
		colunaValor.setPrefWidth(100);

		TableColumn<Despesa, String> colunaCategoria = new TableColumn<>("Categoria");
		colunaCategoria.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getCategoria() != null ? cellData.getValue().getCategoria().getNome() : ""));
		colunaCategoria.setPrefWidth(100);

		TableColumn<Despesa, String> colunaVencimento = new TableColumn<>("Vencimento");
		colunaVencimento.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getDataVencimento() != null
						? cellData.getValue().getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
						: ""));
		colunaVencimento.setPrefWidth(100);

		TableColumn<Despesa, String> colunaStatus = new TableColumn<>("Status");
		colunaStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().isPago() ? "Pago" : "A Pagar"));
		colunaStatus.setPrefWidth(80);

		// Adicionando as colunas à tabela
		tabelaDespesasRecentes.getColumns().addAll(colunaDescricao, colunaValor, colunaCategoria, colunaVencimento,
				colunaStatus);

		Button btnVerTodas = new Button("Ver Todas as Despesas");
		btnVerTodas.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
		btnVerTodas.setOnAction(e -> abrirTelaTodasDespesas());

		painel.getChildren().addAll(titulo, tabelaDespesasRecentes, btnVerTodas);

		return painel;
	}

	/**
	 * Cria o painel de gráficos.
	 * 
	 * @return o painel de gráficos
	 */
	private VBox criarPainelGraficos() {
		VBox painel = new VBox(20);
		painel.setPadding(new Insets(20));
		painel.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

		Label titulo = new Label("Análise de Gastos");
		titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		// Aqui serão adicionados os gráficos

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
		rodape.setStyle("-fx-background-color: #ecf0f1;");

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
        System.out.println("Atualizando dashboard...");
        
        try {
            // Atualizar os totais financeiros
            double totalDespesas = despesaController.calcularTotalDespesasDoMes();
            double totalPago = despesaController.calcularTotalDespesasPagasDoMes();
            double totalAPagar = despesaController.calcularTotalDespesasAPagarDoMes();

            lblTotalDespesasMes.setText(String.format("R$ %.2f", totalDespesas));
            lblTotalPago.setText(String.format("R$ %.2f", totalPago));
            lblTotalAPagar.setText(String.format("R$ %.2f", totalAPagar));

            // Atualizar a tabela de despesas recentes
            ObservableList<Despesa> despesas = despesaController.listarDespesasDoMes();
            System.out.println("Carregando " + despesas.size() + " despesas para a tabela...");
            
            // Debug - imprimir detalhes das despesas
            for (Despesa d : despesas) {
                System.out.println("Despesa: " + d.getId() + " - " + d.getDescricao() + 
                                  " - R$ " + d.getValor() + 
                                  " - Data: " + d.getDataCompra() +
                                  (d.getDataVencimento() != null ? " - Venc.: " + d.getDataVencimento() : ""));
            }
            
            // Atualizar a tabela com as despesas
            tabelaDespesasRecentes.setItems(despesas);
            
            // Forçar refresh da tabela
            tabelaDespesasRecentes.refresh();

            // Atualizar os gráficos
            atualizarGraficos();
            
            System.out.println("Dashboard atualizado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dashboard: " + e.getMessage());
            e.printStackTrace();
        }
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

		// Atualizar o dashboard após o cadastro de uma nova despesa
		atualizarDashboard();
	}

	// Métodos para abrir outras telas
	private void abrirTelaCategorias() {
		TelaCategorias telaCategorias = new TelaCategorias();
		telaCategorias.mostrar();

		atualizarDashboard();
	}

	private void abrirTelaDespesasFixas() {
		// Implementar
	}

	private void abrirTelaCartoes() {
		// Implementar
	}

	private void abrirTelaParcelamentos() {
		// Implementar
	}

	private void abrirTelaRelatorios() {
		// Implementar
	}

	private void abrirTelaConfiguracoes() {
		// Implementar
	}

	private void abrirTelaTodasDespesas() {
		// Implementar
	}
}