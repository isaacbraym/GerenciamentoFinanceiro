package com.gastos.ui;

import com.gastos.controller.DespesaController;
import com.gastos.db.ConexaoBanco;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.Despesa;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tela para visualizar e filtrar todas as despesas.
 */
public class TelaTodasDespesas {
    
    // Constantes para estilos
    private static final String STYLE_BACKGROUND = "-fx-background-color: #f5f5f5;";
    private static final String STYLE_PANEL = "-fx-background-color: white; -fx-background-radius: 5;";
    private static final String STYLE_BTN_PRIMARY = "-fx-background-color: #3498db; -fx-text-fill: white;";
    private static final String STYLE_BTN_SUCCESS = "-fx-background-color: #2ecc71; -fx-text-fill: white;";
    private static final String STYLE_BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white;";
    private static final String STYLE_BTN_NEUTRAL = "-fx-background-color: #95a5a6; -fx-text-fill: white;";
    
    // Constante para formatação de data
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Constante para SQL de busca de despesas
    private static final String SQL_DESPESAS_DIRETAS = 
            "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, d.fixo, " +
            "c.id as categoria_id, c.nome as categoria_nome " +
            "FROM despesas d " +
            "LEFT JOIN categorias c ON d.categoria_id = c.id " +
            "ORDER BY d.id DESC";
    
    private final Stage janela;
    private final DespesaController despesaController;
    
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
        this.despesaController = new DespesaController();
        
        // Configurar a janela
        this.janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.setTitle("Todas as Despesas");
        janela.setMinWidth(900);
        janela.setMinHeight(600);
        
        // Criar a interface
        criarInterface();
    }
    
    /**
     * Exibe a janela.
     */
    public void mostrar() {
        // Carregar as despesas antes de mostrar a janela
        carregarDespesas();
        janela.showAndWait();
    }
    
    /**
     * Cria a interface da tela.
     */
    private void criarInterface() {
        // Painel principal
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle(STYLE_BACKGROUND);
        
        // Painel de filtros
        VBox painelFiltros = criarPainelFiltros();
        painelPrincipal.setTop(painelFiltros);
        
        // Tabela de despesas
        VBox painelTabela = criarPainelTabela();
        painelPrincipal.setCenter(painelTabela);
        
        // Botões de ação
        HBox painelBotoes = criarPainelBotoes();
        painelPrincipal.setBottom(painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal, 900, 600);
        janela.setScene(cena);
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
        
        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.setStyle(STYLE_BTN_PRIMARY);
        btnFiltrar.setOnAction(e -> filtrarDespesas());
        
        linha.getChildren().addAll(lblStatus, cmbStatus, lblBusca, txtBusca, btnFiltrar);
        
        return linha;
    }
    
    /**
     * Cria o painel da tabela de despesas.
     */
    private VBox criarPainelTabela() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle(STYLE_PANEL);
        
        // Tabela de despesas
        tabelaDespesas = new TableView<>();
        tabelaDespesas.setPrefHeight(400);
        
        configurarColunas();
        configurarMenuContexto();
        
        painel.getChildren().add(tabelaDespesas);
        
        return painel;
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
     * Cria o painel de botões.
     */
    private HBox criarPainelBotoes() {
        HBox painel = new HBox(15);
        painel.setPadding(new Insets(15, 0, 0, 0));
        painel.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnNova = new Button("Nova Despesa");
        btnNova.setStyle(STYLE_BTN_SUCCESS);
        btnNova.setOnAction(e -> novaDespesa());
        
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle(STYLE_BTN_PRIMARY);
        btnAtualizar.setOnAction(e -> carregarDespesas());
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle(STYLE_BTN_NEUTRAL);
        btnFechar.setOnAction(e -> janela.close());
        
        painel.getChildren().addAll(btnNova, btnAtualizar, btnFechar);
        
        return painel;
    }
    
    /**
     * Carrega todas as despesas na tabela.
     */
    private void carregarDespesas() {
        try {
            // Tentar primeiro com o controller
            ObservableList<Despesa> despesas = despesaController.listarTodasDespesas();
            
            // Verificar se obteve algum resultado
            if (despesas == null || despesas.isEmpty()) {
                System.out.println("Controller não retornou despesas. Tentando acesso direto ao banco...");
                despesas = buscarDespesasDiretamente();
            }
            
            tabelaDespesas.setItems(despesas);
            
            System.out.println("Despesas carregadas: " + despesas.size());
            
            // Se ainda não tiver resultados, mostrar alerta
            if (despesas.isEmpty()) {
                Platform.runLater(() -> {
                    exibirAlerta(Alert.AlertType.INFORMATION, "Sem Dados", 
                                "Não há despesas cadastradas no sistema.");
                });
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar despesas: " + e.getMessage());
            e.printStackTrace();
            
            // Em caso de falha, tentar buscar diretamente
            ObservableList<Despesa> despesasDiretas = buscarDespesasDiretamente();
            if (despesasDiretas != null && !despesasDiretas.isEmpty()) {
                tabelaDespesas.setItems(despesasDiretas);
                System.out.println("Despesas carregadas diretamente: " + despesasDiretas.size());
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", 
                            "Não foi possível carregar as despesas: " + e.getMessage());
            }
        }
    }

    /**
     * Método alternativo para buscar despesas diretamente do banco.
     */
    private ObservableList<Despesa> buscarDespesasDiretamente() {
        System.out.println("Buscando despesas diretamente do banco...");
        List<Despesa> despesas = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_DESPESAS_DIRETAS);
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
                despesa.setFixo(rs.getBoolean("fixo"));
                
                // Categoria básica
                int categoriaId = rs.getInt("categoria_id");
                if (!rs.wasNull() && categoriaId > 0) {
                    CategoriaDespesa categoria = new CategoriaDespesa();
                    categoria.setId(categoriaId);
                    categoria.setNome(rs.getString("categoria_nome"));
                    despesa.setCategoria(categoria);
                }
                
                despesas.add(despesa);
            }
            
            System.out.println("Total despesas carregadas diretamente: " + despesas.size());
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar despesas diretamente: " + e.getMessage());
            e.printStackTrace();
        }
        
        return FXCollections.observableArrayList(despesas);
    }
    
    /**
     * Filtra as despesas conforme os critérios selecionados.
     */
    private void filtrarDespesas() {
        try {
            ObservableList<Despesa> todasDespesas = despesaController.listarTodasDespesas();
            
            // Se não houver dados pelo controller, buscar diretamente
            if (todasDespesas == null || todasDespesas.isEmpty()) {
                todasDespesas = buscarDespesasDiretamente();
            }
            
            // Criar uma lista de predicados para aplicar os filtros
            List<Predicate<Despesa>> filtros = new ArrayList<>();
            
            // Filtro de data
            if (dpDataInicio.getValue() != null && dpDataFim.getValue() != null) {
                LocalDate dataInicio = dpDataInicio.getValue();
                LocalDate dataFim = dpDataFim.getValue();
                
                filtros.add(despesa -> {
                    LocalDate dataCompra = despesa.getDataCompra();
                    LocalDate dataVencimento = despesa.getDataVencimento();
                    
                    boolean dentroDataCompra = 
                            !dataCompra.isBefore(dataInicio) && !dataCompra.isAfter(dataFim);
                            
                    boolean dentroDataVencimento = dataVencimento != null && 
                            !dataVencimento.isBefore(dataInicio) && 
                            !dataVencimento.isAfter(dataFim);
                            
                    return dentroDataCompra || dentroDataVencimento;
                });
            }
            
            // Filtro de tipo
            if (!cmbTipo.getValue().equals("Todos")) {
                switch (cmbTipo.getValue()) {
                    case "Normal":
                        filtros.add(despesa -> !despesa.isFixo() && !despesa.isParcelada());
                        break;
                    case "Fixa":
                        filtros.add(Despesa::isFixo);
                        break;
                    case "Parcelada":
                        filtros.add(Despesa::isParcelada);
                        break;
                }
            }
            
            // Filtro de status
            if (!cmbStatus.getValue().equals("Todos")) {
                boolean statusPago = cmbStatus.getValue().equals("Pago");
                filtros.add(despesa -> despesa.isPago() == statusPago);
            }
            
            // Filtro de busca
            String termoBusca = txtBusca.getText().toLowerCase().trim();
            if (!termoBusca.isEmpty()) {
                filtros.add(despesa -> {
                    boolean encontrouDescricao = despesa.getDescricao().toLowerCase().contains(termoBusca);
                    boolean encontrouCategoria = despesa.getCategoria() != null && 
                            despesa.getCategoria().getNome().toLowerCase().contains(termoBusca);
                            
                    return encontrouDescricao || encontrouCategoria;
                });
            }
            
            // Aplicar todos os filtros
            Predicate<Despesa> filtroComposto = filtros.stream()
                    .reduce(Predicate::and)
                    .orElse(d -> true); // Se não houver filtros, aceita tudo
                    
            List<Despesa> despesasFiltradas = todasDespesas.stream()
                    .filter(filtroComposto)
                    .collect(Collectors.toList());
            
            // Atualizar a tabela com as despesas filtradas
            tabelaDespesas.setItems(FXCollections.observableArrayList(despesasFiltradas));
            
            System.out.println("Despesas filtradas: " + despesasFiltradas.size());
            
        } catch (Exception e) {
            System.err.println("Erro ao filtrar despesas: " + e.getMessage());
            e.printStackTrace();
            exibirAlerta(Alert.AlertType.ERROR, "Erro", 
                        "Ocorreu um erro ao filtrar as despesas: " + e.getMessage());
        }
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
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Vazia", 
                        "Por favor, selecione uma despesa para continuar.");
        }
        
        return despesaSelecionada;
    }
    
    /**
     * Exclui a despesa selecionada.
     */
    private void excluirDespesa() {
        Despesa despesaSelecionada = obterDespesaSelecionada();
        
        if (despesaSelecionada != null) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmação");
            confirmacao.setHeaderText("Excluir Despesa");
            confirmacao.setContentText("Tem certeza que deseja excluir a despesa '" + 
                                       despesaSelecionada.getDescricao() + "'?");
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (despesaController.excluirDespesa(despesaSelecionada.getId())) {
                    carregarDespesas();
                    exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Despesa excluída com sucesso!");
                } else {
                    exibirAlerta(Alert.AlertType.ERROR, "Erro", 
                                "Erro ao excluir a despesa. Por favor, tente novamente.");
                }
            }
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
                exibirAlerta(Alert.AlertType.INFORMATION, "Informação", 
                            "A despesa já está marcada como " + (paga ? "paga." : "não paga."));
                return;
            }
            
            // Confirmar a operação
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmação");
            confirmacao.setHeaderText("Alterar Status da Despesa");
            confirmacao.setContentText("Deseja marcar a despesa '" + despesaSelecionada.getDescricao() + 
                                       "' como " + (paga ? "PAGA" : "NÃO PAGA") + "?");
            
            Optional<ButtonType> resultado = confirmacao.showAndWait();
            
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                // Alterar o status da despesa
                despesaSelecionada.setPago(paga);
                
                // Salvar a despesa atualizada
                DespesaController.Resultado resultadoSalvar = 
                    despesaController.salvarDespesa(despesaSelecionada);
                
                if (resultadoSalvar.isSucesso()) {
                    carregarDespesas();
                    exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", 
                                "Status da despesa alterado com sucesso!");
                } else {
                    exibirAlerta(Alert.AlertType.ERROR, "Erro", 
                                "Erro ao alterar o status da despesa: " + 
                                resultadoSalvar.getMensagem());
                }
            }
        }
    }
    
    /**
     * Exibe um alerta.
     */
    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}