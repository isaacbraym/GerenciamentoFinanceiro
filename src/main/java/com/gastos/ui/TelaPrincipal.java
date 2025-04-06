package com.gastos.ui;

import com.gastos.GerenciadorFinanceiroApp;
import com.gastos.model.Despesa;
import com.gastos.model.Responsavel;
import com.gastos.service.AvatarService;
import com.gastos.service.DashboardService;
import com.gastos.service.GraficoService;
import com.gastos.service.NavegacaoService;
import com.gastos.service.UIComponentFactory;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.text.TextAlignment;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Classe que representa a tela principal do aplicativo.
 * Foco na construção da interface e delegação de ações para serviços especializados.
 */
public class TelaPrincipal {

    // Constantes para estilo
    private static final String STYLE_BACKGROUND = "-fx-background-color: #f5f5f5;";
    private static final String STYLE_HEADER = "-fx-background-color: #3498db;";
    private static final String STYLE_SIDEBAR = "-fx-background-color: #2c3e50;";
    private static final String STYLE_FOOTER = "-fx-background-color: #ecf0f1;";

    private Scene scene;
    
    // Serviços
    private final DashboardService dashboardService;
    private final GraficoService graficoService;
    private final UIComponentFactory uiFactory;
    private final NavegacaoService navegacaoService;
    private AvatarService avatarService;

    // Componentes da interface
    private Label lblTotalDespesasMes;
    private Label lblTotalPago;
    private Label lblTotalAPagar;
    private TableView<Despesa> tabelaDespesasRecentes;
    private VBox painelGraficos;
    private HBox avatarContainer;
    private Label lblFiltroAtivo;
    private Button btnLimparFiltro;
    private Integer responsavelSelecionadoId = null;
    
    // Componentes do menu
    private VBox menuLateral;
    private VBox subMenuCadastro;
    private boolean subMenuAberto = false;
    private Button btnCadastrar; // Referência ao botão de cadastro para facilitar estilização

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
        // Inicializar serviços
        this.dashboardService = new DashboardService();
        this.graficoService = new GraficoService();
        this.uiFactory = new UIComponentFactory();
        this.navegacaoService = new NavegacaoService(this::atualizarDashboard);
        
        // Criar interface
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
        menuLateral = new VBox(10);
        menuLateral.setPadding(new Insets(15));
        menuLateral.setPrefWidth(200);
        menuLateral.setStyle(STYLE_SIDEBAR);

        // Botões principais do menu
        Button btnDashboard = uiFactory.criarBotaoMenu("Dashboard", "dashboard", e -> atualizarDashboard());
        Button btnNovaDespesa = uiFactory.criarBotaoMenu("Nova Despesa", "nova-despesa", e -> navegacaoService.abrirTelaNovaDespesa());
        btnCadastrar = uiFactory.criarBotaoMenu("Cadastrar", "cadastrar", e -> toggleSubMenu());
        Button btnDespesasFixas = uiFactory.criarBotaoMenu("Despesas Fixas", "despesas-fixas", e -> navegacaoService.mostrarTelaEmDesenvolvimento());
        Button btnParcelamentos = uiFactory.criarBotaoMenu("Parcelamentos", "parcelamentos", e -> navegacaoService.abrirTelaParcelamentos());
        Button btnRelatorios = uiFactory.criarBotaoMenu("Relatórios", "relatorios", e -> navegacaoService.mostrarTelaEmDesenvolvimento());
        Button btnConfiguracoes = uiFactory.criarBotaoMenu("Configurações", "config", e -> navegacaoService.mostrarTelaEmDesenvolvimento());

        // Criar submenu de cadastro (inicialmente oculto)
        criarSubMenuCadastro();

        // Adicionar botões principais ao menu
        menuLateral.getChildren().addAll(
            btnDashboard, 
            btnNovaDespesa, 
            btnCadastrar,
            subMenuCadastro, // Agora adicionamos o submenu logo após o botão Cadastrar
            btnDespesasFixas, 
            btnParcelamentos, 
            btnRelatorios, 
            btnConfiguracoes
        );

        return menuLateral;
    }

    /**
     * Cria o submenu de cadastro.
     */
    private void criarSubMenuCadastro() {
        subMenuCadastro = new VBox(5);
        subMenuCadastro.setVisible(false);
        subMenuCadastro.setManaged(false);
        
        Button btnCartoes = uiFactory.criarBotaoSubMenu("Cartões", "cartoes", e -> navegacaoService.mostrarTelaEmDesenvolvimento());
        Button btnCategorias = uiFactory.criarBotaoSubMenu("Categorias", "categorias", e -> navegacaoService.abrirTelaCategorias());
        Button btnPessoas = uiFactory.criarBotaoSubMenu("Pessoas", "pessoas", e -> {
            try {
                navegacaoService.abrirTelaCadastroPessoa();
            } catch (Exception ex) {
                System.err.println("Erro ao abrir a tela de cadastro de pessoas: " + ex.getMessage());
                ex.printStackTrace();
                
                // Exibir alerta para o usuário
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Erro");
                alerta.setHeaderText("Erro ao abrir a tela de cadastro de pessoas");
                alerta.setContentText("Ocorreu um erro ao tentar abrir a tela: " + ex.getMessage());
                alerta.showAndWait();
            }
        });
        
        subMenuCadastro.getChildren().addAll(btnCartoes, btnCategorias, btnPessoas);
    }
    
    /**
     * Alterna a visibilidade do submenu.
     */
    private void toggleSubMenu() {
        try {
            subMenuAberto = !subMenuAberto;
            subMenuCadastro.setVisible(subMenuAberto);
            subMenuCadastro.setManaged(subMenuAberto);
            
            // Destacar o botão quando o submenu estiver aberto
            if (subMenuAberto) {
                btnCadastrar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;");
            } else {
                btnCadastrar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;");
            }
        } catch (Exception e) {
            System.err.println("Erro ao alternar visibilidade do submenu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cria o dashboard principal.
     */
    private ScrollPane criarDashboard() {
        VBox painelDashboard = new VBox(20);
        painelDashboard.setPadding(new Insets(20));

        // Container para os avatares e informações de filtro
        HBox cabecalhoDashboard = new HBox();
        cabecalhoDashboard.setAlignment(Pos.CENTER_LEFT);
        cabecalhoDashboard.setSpacing(20);
        
        // Container para avatares - principal destaque
        avatarContainer = new HBox(15);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);
        avatarContainer.setPrefHeight(60);  // Aumentando um pouco a altura para dar mais destaque
        avatarContainer.setStyle("-fx-padding: 5px 10px; -fx-background-color: #f8f9fa; -fx-background-radius: 10px;");
        
        // Inicializa o serviço de avatares
        avatarService = new AvatarService(avatarContainer, this::selecionarResponsavel);
        
        // Container para informações de filtro (à direita dos avatares)
        VBox filtroContainer = new VBox(5);
        filtroContainer.setAlignment(Pos.CENTER_LEFT);
        
        lblFiltroAtivo = new Label("Mostrando despesas de: ");
        lblFiltroAtivo.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblFiltroAtivo.setTextFill(Color.GRAY);
        lblFiltroAtivo.setVisible(false);
        
        btnLimparFiltro = uiFactory.criarBotaoPerigo("Limpar Filtro", e -> limparFiltro());
        btnLimparFiltro.setVisible(false);
        
        filtroContainer.getChildren().addAll(lblFiltroAtivo, btnLimparFiltro);
        
        // Adicionar os containers ao cabeçalho
        cabecalhoDashboard.getChildren().addAll(avatarContainer, filtroContainer);
        
        // Container para data atual
        Label dataAtual = new Label("Data: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dataAtual.setFont(Font.font("Arial", 14));
        
        // Painéis principais
        HBox painelResumo = criarPainelResumoFinanceiro();
        HBox painelDespesasGraficos = criarPainelDespesasGraficos();

        painelDashboard.getChildren().addAll(
            cabecalhoDashboard,
            dataAtual,
            painelResumo, 
            painelDespesasGraficos
        );

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

        VBox cartaoTotalDespesas = uiFactory.criarCartaoTotalDespesas("R$ 0,00");
        lblTotalDespesasMes = (Label) cartaoTotalDespesas.getChildren().get(1);

        VBox cartaoTotalPago = uiFactory.criarCartaoTotalPago("R$ 0,00");
        lblTotalPago = (Label) cartaoTotalPago.getChildren().get(1);

        VBox cartaoTotalAPagar = uiFactory.criarCartaoTotalAPagar("R$ 0,00");
        lblTotalAPagar = (Label) cartaoTotalAPagar.getChildren().get(1);

        painelResumo.getChildren().addAll(cartaoTotalDespesas, cartaoTotalPago, cartaoTotalAPagar);
        return painelResumo;
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
        VBox painel = uiFactory.criarPainelComTitulo("Despesas Recentes");

        tabelaDespesasRecentes = new TableView<>();
        configurarTabelaDespesas();

        Button btnVerTodas = uiFactory.criarBotaoPrimario("Ver Todas as Despesas", e -> navegacaoService.abrirTelaTodasDespesas());

        painel.getChildren().addAll(tabelaDespesasRecentes, btnVerTodas);
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
        
        // Nova coluna para mostrar o responsável
        TableColumn<Despesa, String> colunaResponsavel = new TableColumn<>("Responsável");
        colunaResponsavel.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getResponsavel() != null ? cellData.getValue().getResponsavel().getNome() : ""));
        colunaResponsavel.setPrefWidth(100);

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

        tabelaDespesasRecentes.getColumns().addAll(colunaDescricao, colunaValor, colunaCategoria, 
                colunaResponsavel, colunaVencimento, colunaStatus);
    }

    /**
     * Cria o painel de gráficos.
     */
    private VBox criarPainelGraficos() {
        VBox painel = uiFactory.criarPainelComTitulo("Análise de Gastos");
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
            // Atualizar avatares
            avatarService.carregarAvatares();
            
            // Atualizar os totais financeiros
            double totalDespesas = dashboardService.calcularTotalDespesasDoMes(responsavelSelecionadoId);
            double totalPago = dashboardService.calcularTotalDespesasPagasDoMes(responsavelSelecionadoId);
            double totalAPagar = dashboardService.calcularTotalDespesasAPagarDoMes(responsavelSelecionadoId);

            lblTotalDespesasMes.setText(String.format("R$ %.2f", totalDespesas));
            lblTotalPago.setText(String.format("R$ %.2f", totalPago));
            lblTotalAPagar.setText(String.format("R$ %.2f", totalAPagar));

            // Atualizar a tabela
            ObservableList<Despesa> despesas = dashboardService.obterDespesasDoMes(responsavelSelecionadoId);
            tabelaDespesasRecentes.setItems(despesas);

            // Atualizar os gráficos
            atualizarGraficos();
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

        // Obter dados para o gráfico
        List<Object[]> dadosGrafico = dashboardService.obterDadosGrafico(responsavelSelecionadoId);
        
        // Criar gráfico de despesas por categoria
        String tituloGrafico = responsavelSelecionadoId != null ? 
                "Despesas por Categoria (Filtrado)" : "Despesas por Categoria";
        
        ChartViewer viewerCategoria = graficoService.criarGraficoPizzaComVisualizador(
            tituloGrafico,
            dadosGrafico,
            450, 300
        );

        // Adicionar os gráficos ao painel
        painelGraficos.getChildren().add(viewerCategoria);
    }
    
    /**
     * Seleciona um responsável e filtra os dados.
     * 
     * @param responsavel O responsável selecionado ou null para limpar
     */
    private void selecionarResponsavel(Responsavel responsavel) {
        if (responsavel == null) {
            limparFiltro();
            return;
        }
        
        // Atualizar filtro ativo
        responsavelSelecionadoId = responsavel.getId();
        lblFiltroAtivo.setText("Mostrando despesas de: " + responsavel.getNome());
        lblFiltroAtivo.setVisible(true);
        btnLimparFiltro.setVisible(true);
        
        // Atualizar o dashboard com o filtro
        atualizarDashboard();
    }
    
    /**
     * Limpa o filtro de responsável.
     */
    private void limparFiltro() {
        responsavelSelecionadoId = null;
        avatarService.setResponsavelSelecionadoId(null);
        lblFiltroAtivo.setVisible(false);
        btnLimparFiltro.setVisible(false);
        atualizarDashboard();
    }
}