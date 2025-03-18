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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

/**
 * Tela para visualizar e filtrar todas as despesas.
 */
public class TelaTodasDespesas {
    
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
     * Cria a interface da tela de todas as despesas.
     */
    private void criarInterface() {
        // Painel principal
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label lblTitulo = new Label("Gerenciamento de Despesas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Painel de filtros
        VBox painelFiltros = criarPainelFiltros();  // Alterado de HBox para VBox
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
     * @return o painel de filtros
     */
    private VBox criarPainelFiltros() { 
        VBox painelFiltros = new VBox(10);
        painelFiltros.setPadding(new Insets(0, 0, 20, 0));
        
        // Título
        Label lblTitulo = new Label("Todas as Despesas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Linha 1 de filtros
        HBox linha1 = new HBox(15);
        linha1.setAlignment(Pos.CENTER_LEFT);
        
        Label lblDataInicio = new Label("Data inicial:");
        dpDataInicio = new DatePicker(LocalDate.now().withDayOfMonth(1)); // Primeiro dia do mês
        
        Label lblDataFim = new Label("Data final:");
        dpDataFim = new DatePicker(LocalDate.now()); // Hoje
        
        Label lblTipo = new Label("Tipo:");
        cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("Todos", "Normal", "Fixa", "Parcelada");
        cmbTipo.setValue("Todos");
        
        linha1.getChildren().addAll(lblDataInicio, dpDataInicio, lblDataFim, dpDataFim, lblTipo, cmbTipo);
        
        // Linha 2 de filtros
        HBox linha2 = new HBox(15);
        linha2.setAlignment(Pos.CENTER_LEFT);
        
        Label lblStatus = new Label("Status:");
        cmbStatus = new ComboBox<>();
        cmbStatus.getItems().addAll("Todos", "Pago", "A Pagar");
        cmbStatus.setValue("Todos");
        
        Label lblBusca = new Label("Buscar:");
        txtBusca = new TextField();
        txtBusca.setPromptText("Digite para buscar por descrição");
        txtBusca.setPrefWidth(300);
        
        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnFiltrar.setOnAction(e -> filtrarDespesas());
        
        linha2.getChildren().addAll(lblStatus, cmbStatus, lblBusca, txtBusca, btnFiltrar);
        
        painelFiltros.getChildren().addAll(lblTitulo, linha1, linha2);
        
        return painelFiltros;
    }
    
    /**
     * Cria o painel da tabela de despesas.
     * @return o painel da tabela
     */
    private VBox criarPainelTabela() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        // Tabela de despesas
        tabelaDespesas = new TableView<>();
        tabelaDespesas.setPrefHeight(400);
        
        // Configurar colunas
        TableColumn<Despesa, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colunaDescricao.setPrefWidth(200);
        
        TableColumn<Despesa, String> colunaValor = new TableColumn<>("Valor");
        colunaValor.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.format("R$ %.2f", cellData.getValue().getValor())));
        colunaValor.setPrefWidth(100);
        
        TableColumn<Despesa, String> colunaCategoria = new TableColumn<>("Categoria");
        colunaCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategoria() != null ? cellData.getValue().getCategoria().getNome() : ""));
        colunaCategoria.setPrefWidth(150);
        
        TableColumn<Despesa, String> colunaDataCompra = new TableColumn<>("Data Compra");
        colunaDataCompra.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDataCompra().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        colunaDataCompra.setPrefWidth(100);
        
        TableColumn<Despesa, String> colunaVencimento = new TableColumn<>("Vencimento");
        colunaVencimento.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDataVencimento() != null
                        ? cellData.getValue().getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : ""));
        colunaVencimento.setPrefWidth(100);
        
        TableColumn<Despesa, String> colunaStatus = new TableColumn<>("Status");
        colunaStatus.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isPago() ? "Pago" : "A Pagar"));
        colunaStatus.setPrefWidth(80);
        
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
        
        // Adicionar menu de contexto
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
        
        painel.getChildren().add(tabelaDespesas);
        
        return painel;
    }
    
    /**
     * Cria o painel de botões.
     * @return o painel de botões
     */
    private HBox criarPainelBotoes() {
        HBox painel = new HBox(15);
        painel.setPadding(new Insets(15, 0, 0, 0));
        painel.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnNova = new Button("Nova Despesa");
        btnNova.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnNova.setOnAction(e -> novaDespesa());
        
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnAtualizar.setOnAction(e -> carregarDespesas());
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        btnFechar.setOnAction(e -> janela.close());
        
        painel.getChildren().addAll(btnNova, btnAtualizar, btnFechar);
        
        return painel;
    }
    
    /**
     * Carrega todas as despesas na tabela.
     * Versão melhorada com backup direto do banco de dados.
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
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Sem Dados");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Não há despesas cadastradas no sistema.");
                    alerta.showAndWait();
                });
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar despesas através do controller: " + e.getMessage());
            e.printStackTrace();
            
            // Em caso de falha, tentar buscar diretamente
            ObservableList<Despesa> despesasDiretas = buscarDespesasDiretamente();
            if (despesasDiretas != null && !despesasDiretas.isEmpty()) {
                tabelaDespesas.setItems(despesasDiretas);
                System.out.println("Despesas carregadas diretamente: " + despesasDiretas.size());
            } else {
                Platform.runLater(() -> {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Erro");
                    alerta.setHeaderText("Erro ao carregar despesas");
                    alerta.setContentText("Não foi possível carregar as despesas: " + e.getMessage());
                    alerta.showAndWait();
                });
            }
        }
    }

    /**
     * Método alternativo para buscar despesas diretamente do banco de dados
     * quando o método normal falha.
     * 
     * @return lista de despesas recuperadas diretamente do banco
     */
    private ObservableList<Despesa> buscarDespesasDiretamente() {
        System.out.println("Tentando buscar despesas diretamente do banco...");
        List<Despesa> despesas = new ArrayList<>();
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            // Criar uma consulta SQL simplificada
            String sql = "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, d.fixo, " +
                         "c.id as categoria_id, c.nome as categoria_nome " +
                         "FROM despesas d " +
                         "LEFT JOIN categorias c ON d.categoria_id = c.id " +
                         "ORDER BY d.id DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Despesa despesa = new Despesa();
                        despesa.setId(rs.getInt("id"));
                        despesa.setDescricao(rs.getString("descricao"));
                        despesa.setValor(rs.getDouble("valor"));
                        
                        String dataCompraStr = rs.getString("data_compra");
                        if (dataCompraStr != null && !dataCompraStr.isEmpty()) {
                            despesa.setDataCompra(LocalDate.parse(dataCompraStr));
                        } else {
                            despesa.setDataCompra(LocalDate.now());
                        }
                        
                        String dataVencimentoStr = rs.getString("data_vencimento");
                        if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
                            despesa.setDataVencimento(LocalDate.parse(dataVencimentoStr));
                        }
                        
                        despesa.setPago(rs.getBoolean("pago"));
                        despesa.setFixo(rs.getBoolean("fixo"));
                        
                        // Criar categoria básica
                        int categoriaId = rs.getInt("categoria_id");
                        if (!rs.wasNull() && categoriaId > 0) {
                            CategoriaDespesa categoria = new CategoriaDespesa();
                            categoria.setId(categoriaId);
                            categoria.setNome(rs.getString("categoria_nome"));
                            despesa.setCategoria(categoria);
                        }
                        
                        despesas.add(despesa);
                        System.out.println("Despesa carregada diretamente: ID " + despesa.getId() + 
                                          " - " + despesa.getDescricao() + 
                                          " - R$ " + despesa.getValor());
                    }
                }
            }
            
            System.out.println("Total de despesas carregadas diretamente: " + despesas.size());
        } catch (Exception e) {
            System.err.println("Erro ao buscar despesas diretamente: " + e.getMessage());
            e.printStackTrace();
        }
        
        return FXCollections.observableArrayList(despesas);
    }
    
    /**
     * Filtra as despesas de acordo com os critérios selecionados.
     */
    private void filtrarDespesas() {
        try {
            // Obter despesas base
            ObservableList<Despesa> todasDespesas = despesaController.listarTodasDespesas();
            
            // Aplicar filtros
            ObservableList<Despesa> despesasFiltradas = FXCollections.observableArrayList();
            
            LocalDate dataInicio = dpDataInicio.getValue();
            LocalDate dataFim = dpDataFim.getValue();
            String tipo = cmbTipo.getValue();
            String status = cmbStatus.getValue();
            String busca = txtBusca.getText().toLowerCase().trim();
            
            for (Despesa despesa : todasDespesas) {
                boolean passaFiltro = true;
                
                // Filtro de data
                if (dataInicio != null && dataFim != null) {
                    LocalDate dataCompra = despesa.getDataCompra();
                    LocalDate dataVencimento = despesa.getDataVencimento();
                    
                    boolean dentroDataCompra = !dataCompra.isBefore(dataInicio) && !dataCompra.isAfter(dataFim);
                    boolean dentroDataVencimento = dataVencimento != null && 
                                                  !dataVencimento.isBefore(dataInicio) && 
                                                  !dataVencimento.isAfter(dataFim);
                    
                    if (!dentroDataCompra && !dentroDataVencimento) {
                        passaFiltro = false;
                    }
                }
                
                // Filtro de tipo
                if (!tipo.equals("Todos")) {
                    if (tipo.equals("Normal") && (despesa.isFixo() || despesa.isParcelada())) {
                        passaFiltro = false;
                    } else if (tipo.equals("Fixa") && !despesa.isFixo()) {
                        passaFiltro = false;
                    } else if (tipo.equals("Parcelada") && !despesa.isParcelada()) {
                        passaFiltro = false;
                    }
                }
                
                // Filtro de status
                if (!status.equals("Todos")) {
                    if (status.equals("Pago") && !despesa.isPago()) {
                        passaFiltro = false;
                    } else if (status.equals("A Pagar") && despesa.isPago()) {
                        passaFiltro = false;
                    }
                }
                
                // Filtro de busca
                if (!busca.isEmpty()) {
                    boolean encontrado = despesa.getDescricao().toLowerCase().contains(busca);
                    
                    if (despesa.getCategoria() != null) {
                        encontrado = encontrado || despesa.getCategoria().getNome().toLowerCase().contains(busca);
                    }
                    
                    if (!encontrado) {
                        passaFiltro = false;
                    }
                }
                
                // Adicionar à lista filtrada se passar em todos os filtros
                if (passaFiltro) {
                    despesasFiltradas.add(despesa);
                }
            }
            
            tabelaDespesas.setItems(despesasFiltradas);
            System.out.println("Despesas filtradas: " + despesasFiltradas.size());
        } catch (Exception e) {
            System.err.println("Erro ao filtrar despesas: " + e.getMessage());
            e.printStackTrace();
            
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Erro ao filtrar despesas");
            alerta.setContentText("Ocorreu um erro ao filtrar as despesas: " + e.getMessage());
            alerta.showAndWait();
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
        Despesa despesaSelecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
        
        if (despesaSelecionada != null) {
            TelaCadastroDespesa telaCadastroDespesa = new TelaCadastroDespesa(despesaSelecionada, true);
            telaCadastroDespesa.mostrar();
            
            // Atualizar a tabela após a edição
            carregarDespesas();
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Seleção Vazia");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, selecione uma despesa para editar.");
            alerta.showAndWait();
        }
    }
    
    /**
     * Exclui a despesa selecionada.
     */
    private void excluirDespesa() {
        Despesa despesaSelecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
        
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
                    
                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Despesa excluída com sucesso!");
                    sucesso.showAndWait();
                } else {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setTitle("Erro");
                    erro.setHeaderText(null);
                    erro.setContentText("Erro ao excluir a despesa. Por favor, tente novamente.");
                    erro.showAndWait();
                }
            }
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Seleção Vazia");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, selecione uma despesa para excluir.");
            alerta.showAndWait();
        }
    }
    
    /**
     * Marca a despesa selecionada como paga ou não paga.
     * @param paga o novo status da despesa
     */
    private void marcarDespesaPaga(boolean paga) {
        Despesa despesaSelecionada = tabelaDespesas.getSelectionModel().getSelectedItem();
        
        if (despesaSelecionada != null) {
            // Se já está no estado desejado, não fazer nada
            if (despesaSelecionada.isPago() == paga) {
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Informação");
                alerta.setHeaderText(null);
                alerta.setContentText("A despesa já está marcada como " + 
                                     (paga ? "paga." : "não paga."));
                alerta.showAndWait();
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
                    
                    Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                    sucesso.setTitle("Sucesso");
                    sucesso.setHeaderText(null);
                    sucesso.setContentText("Status da despesa alterado com sucesso!");
                    sucesso.showAndWait();
                } else {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setTitle("Erro");
                    erro.setHeaderText(null);
                    erro.setContentText("Erro ao alterar o status da despesa: " + 
                                       resultadoSalvar.getMensagem());
                    erro.showAndWait();
                }
            }
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Seleção Vazia");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, selecione uma despesa para alterar o status.");
            alerta.showAndWait();
        }
    }
}