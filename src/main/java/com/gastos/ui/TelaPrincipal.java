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
 * Classe que representa a tela principal do aplicativo.
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
    private static final String STYLE_BTN_MENU = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";
    private static final String STYLE_BTN_MENU_HOVER = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";

    private Scene scene;
    private final DespesaController despesaController;

    // Componentes da interface
    private Label lblTotalDespesasMes;
    private Label lblTotalPago;
    private Label lblTotalAPagar;
    private TableView<Despesa> tabelaDespesasRecentes;
    private VBox painelGraficos;

    /**
     * Classe estática para ponto de entrada da aplicação
     */
    public static class Main {
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

        painelPrincipal.setTop(criarCabecalho());
        painelPrincipal.setLeft(criarMenuLateral());
        painelPrincipal.setCenter(criarDashboard());
        painelPrincipal.setBottom(criarRodape());

        scene = new Scene(painelPrincipal, 1280, 800);
    }

    /**
     * Cria o cabeçalho da aplicação.
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
     * Cria o menu lateral da aplicação.
     */
    private VBox criarMenuLateral() {
        VBox menuLateral = new VBox(10);
        menuLateral.setPadding(new Insets(15));
        menuLateral.setPrefWidth(200);
        menuLateral.setStyle(STYLE_SIDEBAR);

        String[][] itensMenu = { 
            { "Dashboard", "dashboard", "atualizarDashboard" },
            { "Nova Despesa", "nova-despesa", "abrirTelaNovaDespesa" },
            { "Categorias", "categorias", "abrirTelaCategorias" },
            { "Despesas Fixas", "despesas-fixas", "mostrarTelaEmDesenvolvimento" },
            { "Cartões de Crédito", "cartoes", "mostrarTelaEmDesenvolvimento" },
            { "Parcelamentos", "parcelamentos", "abrirTelaParcelamentos" },
            { "Relatórios", "relatorios", "mostrarTelaEmDesenvolvimento" },
            { "Configurações", "config", "mostrarTelaEmDesenvolvimento" }
        };

        for (String[] item : itensMenu) {
            Button btn = criarBotaoMenu(item[0], item[1], item[2]);
            menuLateral.getChildren().add(btn);
        }

        return menuLateral;
    }

    /**
     * Cria um botão para o menu lateral.
     */
    private Button criarBotaoMenu(String texto, String id, String metodo) {
        Button botao = new Button(texto);
        botao.setId(id);
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setPrefHeight(40);
        botao.setStyle(STYLE_BTN_MENU);

        // Efeito hover
        botao.setOnMouseEntered(e -> botao.setStyle(STYLE_BTN_MENU_HOVER));
        botao.setOnMouseExited(e -> botao.setStyle(STYLE_BTN_MENU));

        // Configurar evento de clique usando reflection
        configurarEventoBotaoMenu(botao, metodo);

        return botao;
    }

    /**
     * Configura o evento de clique para um botão do menu.
     */
    private void configurarEventoBotaoMenu(Button botao, String metodo) {
        switch (metodo) {
            case "atualizarDashboard":
                botao.setOnAction(e -> atualizarDashboard());
                break;
            case "abrirTelaNovaDespesa":
                botao.setOnAction(e -> abrirTelaNovaDespesa());
                break;
            case "abrirTelaCategorias":
                botao.setOnAction(e -> abrirTelaCategorias());
                break;
            case "abrirTelaParcelamentos":
                botao.setOnAction(e -> abrirTelaParcelamentos());
                break;
            case "mostrarTelaEmDesenvolvimento":
                botao.setOnAction(e -> mostrarTelaEmDesenvolvimento());
                break;
            default:
                // Nenhuma ação definida
                break;
        }
    }

    /**
     * Cria o dashboard principal.
     */
    private ScrollPane criarDashboard() {
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

        painelDashboard.getChildren().addAll(tituloDashboard, dataAtual, painelResumo, painelDespesasGraficos);

        ScrollPane scrollPane = new ScrollPane(painelDashboard);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Carregar os dados iniciais após a interface estar pronta
        Platform.runLater(this::atualizarDashboard);

        return scrollPane;
    }

    /**
     * Cria o painel de resumo financeiro com cartões.
     */
    private HBox criarPainelResumoFinanceiro() {
        HBox painelResumo = new HBox(20);
        painelResumo.setAlignment(Pos.CENTER);

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
     * Cria o painel com despesas recentes e gráficos.
     */
    private HBox criarPainelDespesasGraficos() {
        HBox painel = new HBox(20);
        painel.setAlignment(Pos.TOP_CENTER);

        VBox painelDespesasRecentes = criarPainelDespesasRecentes();
        painelDespesasRecentes.setPrefWidth(500);

        painelGraficos = criarPainelGraficos();
        painelGraficos.setPrefWidth(500);

        painel.getChildren().addAll(painelDespesasRecentes, painelGraficos);
        return painel;
    }

    /**
     * Cria o painel de despesas recentes.
     */
    private VBox criarPainelDespesasRecentes() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(20));
        painel.setStyle(STYLE_PANEL);

        Label titulo = new Label("Despesas Recentes");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        tabelaDespesasRecentes = new TableView<>();
        configurarTabelaDespesas();

        Button btnVerTodas = new Button("Ver Todas as Despesas");
        btnVerTodas.setStyle(STYLE_BTN_PRIMARY);
        btnVerTodas.setOnAction(e -> abrirTelaTodasDespesas());

        painel.getChildren().addAll(titulo, tabelaDespesasRecentes, btnVerTodas);
        return painel;
    }

    /**
     * Configura as colunas da tabela de despesas.
     */
    private void configurarTabelaDespesas() {
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

        tabelaDespesasRecentes.getColumns().addAll(colunaDescricao, colunaValor, colunaCategoria, colunaVencimento,
                colunaStatus);
    }

    /**
     * Cria o painel de gráficos.
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
     * Cria o rodapé da aplicação.
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
     * Atualiza os dados do dashboard.
     */
    public void atualizarDashboard() {
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
     * Busca despesas diretamente do banco em caso de falha.
     */
    private ObservableList<Despesa> buscarDespesasDiretamente() {
        List<Despesa> despesas = new ArrayList<>();

        try (Connection conn = ConexaoBanco.getConexao()) {
            String sql = "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, "
                    + "c.id as categoria_id, c.nome as categoria_nome " 
                    + "FROM despesas d "
                    + "LEFT JOIN categorias c ON d.categoria_id = c.id " 
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
     * Cria um gráfico de pizza com os dados fornecidos.
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

    // === Métodos para abrir telas secundárias ===

    /**
     * Abre a tela de cadastro de nova despesa.
     */
    private void abrirTelaNovaDespesa() {
        TelaCadastroDespesa telaNovaDespesa = new TelaCadastroDespesa();
        telaNovaDespesa.mostrar();
        Platform.runLater(this::atualizarDashboard);
    }

    /**
     * Abre a tela de gerenciamento de categorias.
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
     * Abre a tela com todas as despesas.
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

    // === Métodos utilitários ===

    /**
     * Mostra alerta de funcionalidade em desenvolvimento.
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