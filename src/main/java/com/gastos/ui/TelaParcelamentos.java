package com.gastos.ui;

import com.gastos.db.ConexaoBanco;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Versão simplificada da tela de parcelamentos que acessa diretamente o banco de dados.
 */
public class TelaParcelamentos {
    
    private final Stage janela;
    
    // Componentes da interface
    private TableView<ParcelamentoInfo> tabelaParcelamentos;
    private TableView<ParcelaInfo> tabelaParcelas;
    
    /**
     * Construtor da tela de parcelamentos simplificada.
     */
    public TelaParcelamentos() {
        // Configurar a janela
        this.janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.setTitle("Gerenciar Parcelamentos");
        janela.setMinWidth(900);
        janela.setMinHeight(600);
        
        // Criar a interface
        criarInterface();
    }
    
    /**
     * Exibe a janela.
     */
    public void mostrar() {
        // Carregar dados
        carregarParcelamentos();
        janela.showAndWait();
    }
    
    /**
     * Cria a interface da tela de parcelamentos.
     */
    private void criarInterface() {
        // Painel principal
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label lblTitulo = new Label("Gerenciamento de Parcelamentos");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        painelPrincipal.setTop(lblTitulo);
        BorderPane.setMargin(lblTitulo, new Insets(0, 0, 15, 0));
        
        // Painel central com tabelas
        SplitPane painelCentral = new SplitPane();
        
        // Painel de parcelamentos
        VBox painelParcelamentos = criarPainelParcelamentos();
        
        // Painel de parcelas
        VBox painelParcelas = criarPainelParcelas();
        
        painelCentral.getItems().addAll(painelParcelamentos, painelParcelas);
        painelCentral.setDividerPositions(0.6);
        painelPrincipal.setCenter(painelCentral);
        
        // Botões de ação
        HBox painelBotoes = criarPainelBotoes();
        painelPrincipal.setBottom(painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal, 900, 600);
        janela.setScene(cena);
    }
    
    /**
     * Cria o painel de parcelamentos.
     * @return o painel de parcelamentos
     */
    private VBox criarPainelParcelamentos() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        // Título do painel
        Label titulo = new Label("Parcelamentos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Tabela de parcelamentos
        tabelaParcelamentos = new TableView<>();
        tabelaParcelamentos.setPrefHeight(400);
        
        // Configurar colunas
        TableColumn<ParcelamentoInfo, String> colunaDescricao = new TableColumn<>("Descrição");
        colunaDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colunaDescricao.setPrefWidth(200);
        
        TableColumn<ParcelamentoInfo, String> colunaValorTotal = new TableColumn<>("Valor Total");
        colunaValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colunaValorTotal.setPrefWidth(100);
        
        TableColumn<ParcelamentoInfo, String> colunaParcelas = new TableColumn<>("Parcelas");
        colunaParcelas.setCellValueFactory(new PropertyValueFactory<>("parcelas"));
        colunaParcelas.setPrefWidth(100);
        
        TableColumn<ParcelamentoInfo, String> colunaRestantes = new TableColumn<>("Restantes");
        colunaRestantes.setCellValueFactory(new PropertyValueFactory<>("restantes"));
        colunaRestantes.setPrefWidth(80);
        
        TableColumn<ParcelamentoInfo, String> colunaStatus = new TableColumn<>("Status");
        colunaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colunaStatus.setPrefWidth(100);
        
        // Adicionar colunas à tabela
        tabelaParcelamentos.getColumns().addAll(colunaDescricao, colunaValorTotal, 
                                               colunaParcelas, colunaRestantes, colunaStatus);
        
        // Adicionar listener para seleção
        tabelaParcelamentos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        carregarParcelas(newValue.getId());
                    } else {
                        tabelaParcelas.getItems().clear();
                    }
                });
        
        painel.getChildren().addAll(titulo, tabelaParcelamentos);
        
        return painel;
    }
    
    /**
     * Cria o painel de parcelas.
     * @return o painel de parcelas
     */
    private VBox criarPainelParcelas() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        // Título do painel
        Label titulo = new Label("Parcelas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Tabela de parcelas
        tabelaParcelas = new TableView<>();
        tabelaParcelas.setPrefHeight(400);
        
        // Configurar colunas
        TableColumn<ParcelaInfo, String> colunaNumeroParcela = new TableColumn<>("Nº");
        colunaNumeroParcela.setCellValueFactory(new PropertyValueFactory<>("numeroParcela"));
        colunaNumeroParcela.setPrefWidth(50);
        
        TableColumn<ParcelaInfo, String> colunaValor = new TableColumn<>("Valor");
        colunaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colunaValor.setPrefWidth(100);
        
        TableColumn<ParcelaInfo, String> colunaVencimento = new TableColumn<>("Vencimento");
        colunaVencimento.setCellValueFactory(new PropertyValueFactory<>("vencimento"));
        colunaVencimento.setPrefWidth(100);
        
        TableColumn<ParcelaInfo, String> colunaStatus = new TableColumn<>("Status");
        colunaStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colunaStatus.setPrefWidth(100);
        
        // Adicionar colunas à tabela
        tabelaParcelas.getColumns().addAll(colunaNumeroParcela, colunaValor, colunaVencimento, colunaStatus);
        
        // Adicionar menu de contexto para marcar parcela como paga/não paga
        ContextMenu menuContexto = new ContextMenu();
        
        MenuItem itemMarcarPaga = new MenuItem("Marcar como Paga");
        itemMarcarPaga.setOnAction(e -> marcarParcelaPaga(true));
        
        MenuItem itemMarcarNaoPaga = new MenuItem("Marcar como Não Paga");
        itemMarcarNaoPaga.setOnAction(e -> marcarParcelaPaga(false));
        
        menuContexto.getItems().addAll(itemMarcarPaga, itemMarcarNaoPaga);
        
        tabelaParcelas.setContextMenu(menuContexto);
        
        painel.getChildren().addAll(titulo, tabelaParcelas);
        
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
        
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnAtualizar.setOnAction(e -> carregarParcelamentos());
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnFechar.setOnAction(e -> janela.close());
        
        painel.getChildren().addAll(btnAtualizar, btnFechar);
        
        return painel;
    }
    
    /**
     * Carrega todos os parcelamentos diretamente do banco de dados.
     */
    private void carregarParcelamentos() {
        System.out.println("Carregando parcelamentos diretamente do banco...");
        ObservableList<ParcelamentoInfo> itens = FXCollections.observableArrayList();
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            String sql = "SELECT p.id, p.valor_total, p.total_parcelas, p.parcelas_restantes, p.data_inicio, " +
                         "d.descricao, d.id as despesa_id " +
                         "FROM parcelamentos p " +
                         "LEFT JOIN despesas d ON d.parcelamento_id = p.id " +
                         "ORDER BY p.id DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                int contador = 0;
                while (rs.next()) {
                    contador++;
                    
                    int id = rs.getInt("id");
                    double valorTotal = rs.getDouble("valor_total");
                    int totalParcelas = rs.getInt("total_parcelas");
                    int parcelasRestantes = rs.getInt("parcelas_restantes");
                    String descricao = rs.getString("descricao");
                    
                    // Se a descrição for nula (parcelamento sem despesa), usar descrição padrão
                    if (descricao == null || descricao.isEmpty()) {
                        descricao = "Parcelamento #" + id;
                    }
                    
                    ParcelamentoInfo item = new ParcelamentoInfo(
                        id,
                        descricao,
                        "R$ " + String.format("%.2f", valorTotal),
                        totalParcelas + "x",
                        String.valueOf(parcelasRestantes),
                        parcelasRestantes > 0 ? "Em Andamento" : "Quitado"
                    );
                    
                    itens.add(item);
                    System.out.println("Parcelamento #" + contador + ": " + descricao);
                }
                
                if (contador == 0) {
                    System.out.println("Nenhum parcelamento encontrado no banco de dados!");
                } else {
                    System.out.println("Total de " + contador + " parcelamentos carregados.");
                }
            }
            
            tabelaParcelamentos.setItems(itens);
            
            // Se houver itens, selecionar o primeiro
            if (!itens.isEmpty()) {
                tabelaParcelamentos.getSelectionModel().selectFirst();
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar parcelamentos: " + e.getMessage());
            e.printStackTrace();
            
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Erro ao carregar parcelamentos");
            alerta.setContentText("Ocorreu um erro ao carregar os parcelamentos: " + e.getMessage());
            alerta.showAndWait();
        }
    }
    
    /**
     * Carrega as parcelas de um parcelamento.
     * @param parcelamentoId o ID do parcelamento
     */
    private void carregarParcelas(int parcelamentoId) {
        System.out.println("Carregando parcelas do parcelamento ID " + parcelamentoId + "...");
        ObservableList<ParcelaInfo> itens = FXCollections.observableArrayList();
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            // Buscar o número total de parcelas do parcelamento
            int totalParcelas = 0;
            String sqlTotal = "SELECT total_parcelas FROM parcelamentos WHERE id = ?";
            
            try (PreparedStatement stmtTotal = conn.prepareStatement(sqlTotal)) {
                stmtTotal.setInt(1, parcelamentoId);
                
                try (ResultSet rsTotal = stmtTotal.executeQuery()) {
                    if (rsTotal.next()) {
                        totalParcelas = rsTotal.getInt("total_parcelas");
                    }
                }
            }
            
            // Buscar as parcelas
            String sql = "SELECT id, numero_parcela, valor, data_vencimento, paga " +
                         "FROM parcelas " +
                         "WHERE parcelamento_id = ? " +
                         "ORDER BY numero_parcela";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, parcelamentoId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    int contador = 0;
                    while (rs.next()) {
                        contador++;
                        
                        int id = rs.getInt("id");
                        int numeroParcela = rs.getInt("numero_parcela");
                        double valor = rs.getDouble("valor");
                        String dataVencimentoStr = rs.getString("data_vencimento");
                        boolean paga = rs.getBoolean("paga");
                        
                        LocalDate dataVencimento = null;
                        if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
                            try {
                                dataVencimento = LocalDate.parse(dataVencimentoStr);
                            } catch (Exception e) {
                                System.err.println("Erro ao converter data: " + dataVencimentoStr);
                            }
                        }
                        
                        ParcelaInfo item = new ParcelaInfo(
                            id,
                            numeroParcela + "/" + totalParcelas,
                            "R$ " + String.format("%.2f", valor),
                            dataVencimento != null ? dataVencimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A",
                            paga ? "Paga" : "A Pagar"
                        );
                        
                        itens.add(item);
                    }
                    
                    if (contador == 0) {
                        System.out.println("Nenhuma parcela encontrada para o parcelamento ID " + parcelamentoId);
                    } else {
                        System.out.println("Total de " + contador + " parcelas carregadas.");
                    }
                }
            }
            
            tabelaParcelas.setItems(itens);
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar parcelas: " + e.getMessage());
            e.printStackTrace();
            
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText("Erro ao carregar parcelas");
            alerta.setContentText("Ocorreu um erro ao carregar as parcelas: " + e.getMessage());
            alerta.showAndWait();
        }
    }
    
    /**
     * Marca uma parcela como paga ou não paga.
     * @param paga o novo status da parcela
     */
    private void marcarParcelaPaga(boolean paga) {
        ParcelaInfo parcelaSelecionada = tabelaParcelas.getSelectionModel().getSelectedItem();
        
        if (parcelaSelecionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Seleção Vazia");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, selecione uma parcela para alterar o status.");
            alerta.showAndWait();
            return;
        }
        
        // Confirmar operação
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Alterar Status da Parcela");
        confirmacao.setContentText("Deseja marcar a parcela " + parcelaSelecionada.getNumeroParcela() + 
                                  " como " + (paga ? "PAGA" : "NÃO PAGA") + "?");
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try (Connection conn = ConexaoBanco.getConexao()) {
                // 1. Atualizar o status da parcela
                String sqlParcela = "UPDATE parcelas SET paga = ? WHERE id = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sqlParcela)) {
                    stmt.setBoolean(1, paga);
                    stmt.setInt(2, parcelaSelecionada.getId());
                    int rowsUpdated = stmt.executeUpdate();
                    
                    if (rowsUpdated > 0) {
                        System.out.println("Parcela atualizada com sucesso!");
                        
                        // 2. Atualizar o número de parcelas restantes do parcelamento
                        atualizarParcelasRestantes(conn, parcelaSelecionada.getId());
                        
                        // 3. Recarregar os dados
                        ParcelamentoInfo parcelamentoSelecionado = tabelaParcelamentos.getSelectionModel().getSelectedItem();
                        if (parcelamentoSelecionado != null) {
                            carregarParcelas(parcelamentoSelecionado.getId());
                            carregarParcelamentos(); // Atualizar a lista de parcelamentos também
                        }
                        
                        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                        sucesso.setTitle("Sucesso");
                        sucesso.setHeaderText(null);
                        sucesso.setContentText("Parcela atualizada com sucesso!");
                        sucesso.showAndWait();
                    } else {
                        System.out.println("Nenhuma parcela foi atualizada!");
                        
                        Alert erro = new Alert(Alert.AlertType.ERROR);
                        erro.setTitle("Erro");
                        erro.setHeaderText(null);
                        erro.setContentText("Não foi possível atualizar a parcela. A parcela não foi encontrada.");
                        erro.showAndWait();
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao atualizar parcela: " + e.getMessage());
                e.printStackTrace();
                
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setTitle("Erro");
                erro.setHeaderText(null);
                erro.setContentText("Ocorreu um erro ao atualizar a parcela: " + e.getMessage());
                erro.showAndWait();
            }
        }
    }
    
    /**
     * Atualiza o número de parcelas restantes de um parcelamento.
     * @param conn conexão com o banco de dados
     * @param parcelaId ID da parcela
     * @throws Exception em caso de erro
     */
    private void atualizarParcelasRestantes(Connection conn, int parcelaId) throws Exception {
        // 1. Obter o ID do parcelamento
        int parcelamentoId = 0;
        
        String sqlGetParcelamento = "SELECT parcelamento_id FROM parcelas WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlGetParcelamento)) {
            stmt.setInt(1, parcelaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    parcelamentoId = rs.getInt("parcelamento_id");
                }
            }
        }
        
        if (parcelamentoId == 0) {
            throw new Exception("Não foi possível encontrar o parcelamento da parcela ID " + parcelaId);
        }
        
        // 2. Contar parcelas não pagas
        int parcelasRestantes = 0;
        
        String sqlCount = "SELECT COUNT(*) as restantes FROM parcelas WHERE parcelamento_id = ? AND paga = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sqlCount)) {
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    parcelasRestantes = rs.getInt("restantes");
                }
            }
        }
        
        // 3. Atualizar o parcelamento
        String sqlUpdate = "UPDATE parcelamentos SET parcelas_restantes = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setInt(1, parcelasRestantes);
            stmt.setInt(2, parcelamentoId);
            
            int rowsUpdated = stmt.executeUpdate();
            System.out.println("Parcelamento " + parcelamentoId + " atualizado: " + parcelasRestantes + " parcelas restantes");
        }
    }
    
    /**
     * Classe para representar um parcelamento na tabela.
     */
    public static class ParcelamentoInfo {
        private int id;
        private String descricao;
        private String valorTotal;
        private String parcelas;
        private String restantes;
        private String status;
        
        public ParcelamentoInfo(int id, String descricao, String valorTotal, String parcelas, 
                              String restantes, String status) {
            this.id = id;
            this.descricao = descricao;
            this.valorTotal = valorTotal;
            this.parcelas = parcelas;
            this.restantes = restantes;
            this.status = status;
        }
        
        public int getId() {
            return id;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        public String getValorTotal() {
            return valorTotal;
        }
        
        public String getParcelas() {
            return parcelas;
        }
        
        public String getRestantes() {
            return restantes;
        }
        
        public String getStatus() {
            return status;
        }
    }
    
    /**
     * Classe para representar uma parcela na tabela.
     */
    public static class ParcelaInfo {
        private int id;
        private String numeroParcela;
        private String valor;
        private String vencimento;
        private String status;
        
        public ParcelaInfo(int id, String numeroParcela, String valor, String vencimento, String status) {
            this.id = id;
            this.numeroParcela = numeroParcela;
            this.valor = valor;
            this.vencimento = vencimento;
            this.status = status;
        }
        
        public int getId() {
            return id;
        }
        
        public String getNumeroParcela() {
            return numeroParcela;
        }
        
        public String getValor() {
            return valor;
        }
        
        public String getVencimento() {
            return vencimento;
        }
        
        public String getStatus() {
            return status;
        }
    }
}