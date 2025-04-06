package com.gastos.ui;

import com.gastos.controller.DespesaController;
import com.gastos.model.Despesa;
import com.gastos.service.DespesaFiltroService;
import com.gastos.ui.base.BaseTelaModal;
import com.gastos.db.ConexaoBanco;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Tela para visualizar e filtrar todas as despesas.
 * Refatorada para usar BaseTelaModal.
 */
public class TelaTodasDespesas extends BaseTelaModal {
    
    // Constante para formatação de data
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Serviços
    private final DespesaFiltroService despesaFiltroService;
    
    // Componentes da interface
    private TableView<Despesa> tabelaDespesas;
    private DatePicker dpDataInicio;
    private DatePicker dpDataFim;
    private ComboBox<String> cmbTipo;
    private ComboBox<String> cmbStatus;
    private TextField txtBusca;
    
    /**
     * Construtor da tela de todas as despesas.
     */
    public TelaTodasDespesas() {
        // Chamar o construtor da classe base
        super("Todas as Despesas", 900, 600);
        
        // Inicializar serviços
        this.despesaFiltroService = new DespesaFiltroService();
        
        // Carregar as despesas
        carregarDespesas();
    }
    
    /**
     * Cria o conteúdo principal (área central do BorderPane).
     */
    @Override
    protected Node criarConteudoPrincipal() {
        VBox painelPrincipal = new VBox(20);
        
        // Painel de filtros
        VBox painelFiltros = criarPainelFiltros();
        
        // Tabela de despesas
        VBox painelTabela = criarPainelTabela();
        
        painelPrincipal.getChildren().addAll(painelFiltros, painelTabela);
        return painelPrincipal;
    }
    
    /**
     * Cria o painel de botões (área inferior do BorderPane).
     */
    @Override
    protected Node criarPainelBotoes() {
        Button btnNova = uiFactory.criarBotaoSucesso("Nova Despesa", e -> novaDespesa());
        Button btnEditar = uiFactory.criarBotaoPrimario("Editar Despesa", e -> editarDespesa());
        Button btnExcluir = uiFactory.criarBotaoPerigo("Excluir Despesa", e -> excluirDespesa());
        Button btnAtualizar = uiFactory.criarBotaoPrimario("Atualizar", e -> carregarDespesas());
        Button btnFechar = uiFactory.criarBotaoPerigo("Fechar", e -> fechar());
        
        return uiFactory.criarPainelBotoes(btnNova, btnEditar, btnExcluir, btnAtualizar, btnFechar);
    }
    
    /**
     * Cria o painel de filtros.
     */
    private VBox criarPainelFiltros() {
        VBox painelFiltros = new VBox(10);
        painelFiltros.setPadding(new Insets(0, 0, 20, 0));
        
        // Título
        Label lblTitulo = new Label("Todas as Despesas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Linha 1 de filtros: Datas e Tipo
        HBox linha1 = criarLinhaFiltrosDatas();
        
        // Linha 2 de filtros: Status, Busca e botão Filtrar
        HBox linha2 = criarLinhaFiltrosStatus();
        
        painelFiltros.getChildren().addAll(lblTitulo, linha1, linha2);
        
        return painelFiltros;
    }
    
    /**
     * Cria a primeira linha de filtros (datas e tipo).
     */
    private HBox criarLinhaFiltrosDatas() {
        HBox linha = new HBox(15);
        linha.setAlignment(Pos.CENTER_LEFT);
        
        Label lblDataInicio = new Label("Data inicial:");
        dpDataInicio = new DatePicker(LocalDate.now().withDayOfMonth(1)); // Primeiro dia do mês
        
        Label lblDataFim = new Label("Data final:");
        dpDataFim = new DatePicker(LocalDate.now()); // Hoje
        
        Label lblTipo = new Label("Tipo:");
        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Todos", "Normal", "Fixa", "Parcelada");
        cmbTipo.setValue("Todos");
        
        linha.getChildren().addAll(lblDataInicio, dpDataInicio, lblDataFim, dpDataFim, lblTipo, cmbTipo);
        
        return linha;
    }
    
    /**
     * Cria a segunda linha de filtros (status e busca).
     */
    private HBox criarLinhaFiltrosStatus() {
        HBox linha = new HBox(15);
        linha.setAlignment(Pos.CENTER_LEFT);
        
        Label lblStatus = new Label("Status:");
        cmbStatus = new ComboBox<>();
        cmbStatus.getItems().addAll("Todos", "Pago", "A Pagar");
        cmbStatus.setValue("Todos");
        
        Label lblBusca = new Label("Buscar:");
        txtBusca = new TextField();
        txtBusca.setPromptText("Digite para buscar por descrição");
        txtBusca.setPrefWidth(300);
        
        Button btnFiltrar = uiFactory.criarBotaoPrimario("Filtrar", e -> filtrarDespesas());
        
        linha.getChildren().addAll(lblStatus, cmbStatus, lblBusca, txtBusca, btnFiltrar);
        
        return linha;
    }
    
    /**
     * Cria o painel da tabela de despesas.
     */
    private VBox criarPainelTabela() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        // Tabela de despesas
        tabelaDespesas = new TableView<>();
        tabelaDespesas.setPrefHeight(400);
        
        configurarColunas();
        configurarMenuContexto();
        configurarEventosTabela();
        
        painel.getChildren().add(tabelaDespesas);
        
        return painel;
    }
    
    /**
     * Configura eventos adicionais para a tabela.
     */
    private void configurarEventosTabela() {
        // Adicionar evento de duplo clique para editar
        tabelaDespesas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editarDespesa();
            }
        });
    }
    
    /**
     * Configura as colunas da tabela.
     */
    private void configurarColunas() {
        // Coluna de descrição
        TableColumn<Despesa, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDescricao()));
        colunaDescricao.setPrefWidth(200);
        
        // Coluna de valor
        TableColumn<Despesa, String> colunaValor = new TableColumn<>("Valor");
        colunaValor.setCellValueFactory(cellData -> 
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));
        colunaValor.setPrefWidth(100);
        
        // Coluna de categoria
        TableColumn<Despesa, String> colunaCategoria = new TableColumn<>("Categoria");
        colunaCategoria.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getCategoria() != null 
                        ? cellData.getValue().getCategoria().getNome() : ""));
        colunaCategoria.setPrefWidth(150);
        
        // Coluna de data compra
        TableColumn<Despesa, String> colunaDataCompra = new TableColumn<>("Data Compra");
        colunaDataCompra.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDataCompra().format(DATE_FORMATTER)));
        colunaDataCompra.setPrefWidth(100);
        
        // Coluna de vencimento
        TableColumn<Despesa, String> colunaVencimento = new TableColumn<>("Vencimento");
        colunaVencimento.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getDataVencimento() != null
                        ? cellData.getValue().getDataVencimento().format(DATE_FORMATTER) : ""));
        colunaVencimento.setPrefWidth(100);
        
        // Coluna de status
        TableColumn<Despesa, String> colunaStatus = new TableColumn<>("Status");
        colunaStatus.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isPago() ? "Pago" : "A Pagar"));
        colunaStatus.setPrefWidth(80);
        
        // Coluna de tipo
        TableColumn<Despesa, String> colunaTipo = new TableColumn<>("Tipo");
        colunaTipo.setCellValueFactory(cellData -> {
            Despesa despesa = cellData.getValue();
            if (despesa.isParcelada()) {
                return new SimpleStringProperty("Parcelada");
            } else if (despesa.isFixo()) {
                return new SimpleStringProperty("Fixa");
            } else {
                return new SimpleStringProperty("Normal");
            }
        });
        colunaTipo.setPrefWidth(80);
        
        // Adicionar colunas à tabela
        tabelaDespesas.getColumns().addAll(colunaDescricao, colunaValor, colunaCategoria, 
                                          colunaDataCompra, colunaVencimento, colunaStatus, colunaTipo);
    }
    
    /**
     * Configura o menu de contexto da tabela.
     */
    private void configurarMenuContexto() {
        ContextMenu menuContexto = new ContextMenu();
        
        MenuItem itemEditar = new MenuItem("Editar");
        itemEditar.setOnAction(e -> editarDespesa());
        
        MenuItem itemExcluir = new MenuItem("Excluir");
        itemExcluir.setOnAction(e -> excluirDespesa());
        
        MenuItem itemMarcarPaga = new MenuItem("Marcar como Paga");
        itemMarcarPaga.setOnAction(e -> marcarDespesaPaga(true));
        
        MenuItem itemMarcarNaoPaga = new MenuItem("Marcar como Não Paga");
        itemMarcarNaoPaga.setOnAction(e -> marcarDespesaPaga(false));
        
        menuContexto.getItems().addAll(itemEditar, itemExcluir, new SeparatorMenuItem(), 
                                      itemMarcarPaga, itemMarcarNaoPaga);
        
        tabelaDespesas.setContextMenu(menuContexto);
    }
    
    /**
     * Carrega todas as despesas na tabela.
     */
    private void carregarDespesas() {
        ObservableList<Despesa> despesas = despesaFiltroService.carregarTodasDespesas();
        tabelaDespesas.setItems(despesas);
        
        // Se não tiver resultados, mostrar alerta
        if (despesas.isEmpty()) {
            Platform.runLater(() -> {
                exibirInformacao("Sem Dados", "Não há despesas cadastradas no sistema.");
            });
        }
    }
    
    /**
     * Filtra as despesas conforme os critérios selecionados.
     */
    private void filtrarDespesas() {
        ObservableList<Despesa> despesasFiltradas = despesaFiltroService.filtrarDespesas(
            dpDataInicio.getValue(),
            dpDataFim.getValue(),
            cmbTipo.getValue(),
            cmbStatus.getValue(),
            txtBusca.getText()
        );
        
        tabelaDespesas.setItems(despesasFiltradas);
    }
    
    /**
     * Abre a tela de cadastro de nova despesa.
     */
    private void novaDespesa() {
        TelaCadastroDespesa telaCadastroDespesa = new TelaCadastroDespesa();
        telaCadastroDespesa.mostrar();
        
        // Atualizar a tabela após o cadastro
        carregarDespesas();
    }
    
    /**
     * Abre a tela de edição da despesa selecionada.
     */
    private void editarDespesa() {
        Despesa despesaSelecionada = obterDespesaSelecionada();
        
        if (despesaSelecionada != null) {
            TelaCadastroDespesa telaCadastroDespesa = new TelaCadastroDespesa(despesaSelecionada, true);
            telaCadastroDespesa.mostrar();
            
            // Atualizar a tabela após a edição
            carregarDespesas();
        }
    }
    
    /**
     * Obtém a despesa selecionada na tabela ou exibe um alerta se não houver seleção.
     */
    private Despesa obterDespesaSelecionada() {
        Despesa despesaSelecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
        
        if (despesaSelecionada == null) {
            exibirAviso("Seleção Vazia", "Por favor, selecione uma despesa para continuar.");
        }
        
        return despesaSelecionada;
    }
    
    private void excluirDespesa() {
        Despesa despesaSelecionada = obterDespesaSelecionada();
        
        if (despesaSelecionada != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmação");
            confirmacao.setHeaderText("Excluir Despesa");
            confirmacao.setContentText("Tem certeza que deseja excluir a despesa '" + 
                                      despesaSelecionada.getDescricao() + "'?");
            
            confirmacao.initOwner(stage);
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                try {
                    // Removemos a chamada ao método excluirParcelamentoDaDespesa
                    // Deixamos que o DAO cuide de tudo em uma única transação
                    
                    boolean sucesso = despesaFiltroService.excluirDespesa(despesaSelecionada.getId());
                    
                    if (sucesso) {
                        carregarDespesas();
                        exibirInformacao("Sucesso", "Despesa excluída com sucesso!");
                    } else {
                        exibirErro("Erro", "Erro ao excluir a despesa. Por favor, tente novamente.");
                    }
                } catch (Exception e) {
                    exibirErro("Erro", "Erro ao excluir a despesa: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Exclui o parcelamento associado a uma despesa.
     * Este método deve ser chamado antes de excluir a despesa.
     */
    private void excluirParcelamentoDaDespesa(Despesa despesa) {
        try {
            if (despesa.getParcelamento() != null && despesa.getParcelamento().getId() > 0) {
                // Executar SQL para excluir as parcelas primeiro
                try (Connection conn = ConexaoBanco.getConexao()) {
                    // 1. Excluir parcelas
                    String sqlDeleteParcelas = "DELETE FROM parcelas WHERE parcelamento_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteParcelas)) {
                        stmt.setInt(1, despesa.getParcelamento().getId());
                        stmt.executeUpdate();
                    }
                    
                    // 2. Excluir o parcelamento
                    String sqlDeleteParcelamento = "DELETE FROM parcelamentos WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteParcelamento)) {
                        stmt.setInt(1, despesa.getParcelamento().getId());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir parcelamento: " + e.getMessage());
            throw new RuntimeException("Falha ao excluir o parcelamento associado à despesa", e);
        }
    }
    
    /**
     * Marca a despesa selecionada como paga ou não paga.
     */
    private void marcarDespesaPaga(boolean paga) {
        Despesa despesaSelecionada = obterDespesaSelecionada();
        
        if (despesaSelecionada != null) {
            // Se já está no estado desejado, não fazer nada
            if (despesaSelecionada.isPago() == paga) {
                exibirInformacao("Informação", 
                               "A despesa já está marcada como " + (paga ? "paga." : "não paga."));
                return;
            }
            
            // Confirmar a operação
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmação");
            confirmacao.setHeaderText("Alterar Status da Despesa");
            confirmacao.setContentText("Deseja marcar a despesa '" + despesaSelecionada.getDescricao() + 
                                       "' como " + (paga ? "PAGA" : "NÃO PAGA") + "?");
            confirmacao.initOwner(stage);
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Alterar o status da despesa usando o serviço
                DespesaController.Resultado resultadoSalvar = 
                    despesaFiltroService.marcarStatusPagamento(despesaSelecionada, paga);
                
                if (resultadoSalvar.isSucesso()) {
                    carregarDespesas();
                    exibirInformacao("Sucesso", "Status da despesa alterado com sucesso!");
                } else {
                    exibirErro("Erro", "Erro ao alterar o status da despesa: " + 
                              resultadoSalvar.getMensagem());
                }
            }
        }
    }
}