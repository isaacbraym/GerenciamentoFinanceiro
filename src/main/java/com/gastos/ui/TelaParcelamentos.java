package com.gastos.ui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.gastos.db.ConexaoBanco;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Tela para gerenciar parcelamentos.
 */
public class TelaParcelamentos {
    
    // Constantes para estilos
    private static final String STYLE_BACKGROUND = "-fx-background-color: #f5f5f5;";
    private static final String STYLE_PANEL = "-fx-background-color: white; -fx-background-radius: 5;";
    private static final String STYLE_BTN_SUCCESS = "-fx-background-color: #2ecc71; -fx-text-fill: white;";
    private static final String STYLE_BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white;";
    
    // Constantes para SQL
    private static final String SQL_LOAD_PARCELAMENTOS = 
            "SELECT p.id, p.valor_total, p.total_parcelas, p.parcelas_restantes, p.data_inicio, " +
            "d.descricao, d.id as despesa_id " +
            "FROM parcelamentos p " +
            "LEFT JOIN despesas d ON d.parcelamento_id = p.id " +
            "ORDER BY p.id DESC";
    
    private static final String SQL_LOAD_PARCELAS = 
            "SELECT id, numero_parcela, valor, data_vencimento, paga " +
            "FROM parcelas " +
            "WHERE parcelamento_id = ? " +
            "ORDER BY numero_parcela";
    
    private static final String SQL_UPDATE_PARCELA_STATUS = 
            "UPDATE parcelas SET paga = ? WHERE id = ?";
    
    private static final String SQL_GET_PARCELAMENTO_ID = 
            "SELECT parcelamento_id FROM parcelas WHERE id = ?";
    
    private static final String SQL_COUNT_UNPAID_PARCELAS = 
            "SELECT COUNT(*) as restantes FROM parcelas WHERE parcelamento_id = ? AND paga = 0";
    
    private static final String SQL_UPDATE_PARCELAS_RESTANTES = 
            "UPDATE parcelamentos SET parcelas_restantes = ? WHERE id = ?";
    
    private final Stage janela;
    
    // Componentes da interface
    private TableView<ParcelamentoInfo> tabelaParcelamentos;
    private TableView<ParcelaInfo> tabelaParcelas;
    
    /**
     * Construtor da tela de parcelamentos.
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
     * Cria a interface da tela.
     */
    private void criarInterface() {
        // Painel principal
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle(STYLE_BACKGROUND);
        
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
     */
    private VBox criarPainelParcelamentos() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle(STYLE_PANEL);
        
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
     */
    private VBox criarPainelParcelas() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle(STYLE_PANEL);
        
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
        
        // Adicionar menu de contexto
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
     */
    private HBox criarPainelBotoes() {
        HBox painel = new HBox(15);
        painel.setPadding(new Insets(15, 0, 0, 0));
        painel.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnAtualizar = new Button("Atualizar");
        btnAtualizar.setStyle(STYLE_BTN_SUCCESS);
        btnAtualizar.setOnAction(e -> carregarParcelamentos());
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle(STYLE_BTN_DANGER);
        btnFechar.setOnAction(e -> janela.close());
        
        painel.getChildren().addAll(btnAtualizar, btnFechar);
        
        return painel;
    }
    
    /**
     * Carrega todos os parcelamentos.
     */
    private void carregarParcelamentos() {
        ObservableList<ParcelamentoInfo> itens = FXCollections.observableArrayList();
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_LOAD_PARCELAMENTOS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                double valorTotal = rs.getDouble("valor_total");
                int totalParcelas = rs.getInt("total_parcelas");
                int parcelasRestantes = rs.getInt("parcelas_restantes");
                String descricao = rs.getString("descricao");
                
                // Se a descrição for nula, usar descrição padrão
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
            }
            
            tabelaParcelamentos.setItems(itens);
            
            // Se houver itens, selecionar o primeiro
            if (!itens.isEmpty()) {
                tabelaParcelamentos.getSelectionModel().selectFirst();
            }
            
        } catch (SQLException e) {
            exibirErro("Erro ao carregar parcelamentos", e.getMessage());
        }
    }
    
    /**
     * Carrega as parcelas de um parcelamento.
     */
    private void carregarParcelas(int parcelamentoId) {
        ObservableList<ParcelaInfo> itens = FXCollections.observableArrayList();
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            // Buscar o número total de parcelas
            int totalParcelas = obterTotalParcelas(conn, parcelamentoId);
            
            // Buscar as parcelas
            try (PreparedStatement stmt = conn.prepareStatement(SQL_LOAD_PARCELAS)) {
                stmt.setInt(1, parcelamentoId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ParcelaInfo parcela = construirParcelaInfo(rs, totalParcelas);
                        itens.add(parcela);
                    }
                }
            }
            
            tabelaParcelas.setItems(itens);
            
        } catch (SQLException e) {
            exibirErro("Erro ao carregar parcelas", e.getMessage());
        }
    }
    
    /**
     * Obtém o número total de parcelas de um parcelamento.
     */
    private int obterTotalParcelas(Connection conn, int parcelamentoId) throws SQLException {
        String sql = "SELECT total_parcelas FROM parcelamentos WHERE id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_parcelas");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Constrói um objeto ParcelaInfo a partir do ResultSet.
     */
    private ParcelaInfo construirParcelaInfo(ResultSet rs, int totalParcelas) throws SQLException {
        int id = rs.getInt("id");
        int numeroParcela = rs.getInt("numero_parcela");
        double valor = rs.getDouble("valor");
        String dataVencimentoStr = rs.getString("data_vencimento");
        boolean paga = rs.getBoolean("paga");
        
        String dataFormatada = "N/A";
        if (dataVencimentoStr != null && !dataVencimentoStr.isEmpty()) {
            try {
                LocalDate dataVencimento = LocalDate.parse(dataVencimentoStr);
                dataFormatada = dataVencimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                dataFormatada = dataVencimentoStr + " (formato inválido)";
            }
        }
        
        return new ParcelaInfo(
            id,
            numeroParcela + "/" + totalParcelas,
            "R$ " + String.format("%.2f", valor),
            dataFormatada,
            paga ? "Paga" : "A Pagar"
        );
    }
    
    /**
     * Marca uma parcela como paga ou não paga.
     */
    private void marcarParcelaPaga(boolean paga) {
        ParcelaInfo parcelaSelecionada = tabelaParcelas.getSelectionModel().getSelectedItem();
        
        if (parcelaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Vazia", 
                        "Por favor, selecione uma parcela para alterar o status.");
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
                try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PARCELA_STATUS)) {
                    stmt.setBoolean(1, paga);
                    stmt.setInt(2, parcelaSelecionada.getId());
                    stmt.executeUpdate();
                }
                
                // 2. Atualizar o número de parcelas restantes
                atualizarParcelasRestantes(conn, parcelaSelecionada.getId());
                
                // 3. Recarregar os dados
                ParcelamentoInfo parcelamentoSelecionado = tabelaParcelamentos.getSelectionModel().getSelectedItem();
                if (parcelamentoSelecionado != null) {
                    carregarParcelas(parcelamentoSelecionado.getId());
                    carregarParcelamentos(); // Atualizar também a lista de parcelamentos
                }
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Parcela atualizada com sucesso!");
                
            } catch (SQLException e) {
                exibirErro("Erro ao atualizar parcela", e.getMessage());
            }
        }
    }
    
    /**
     * Atualiza o número de parcelas restantes de um parcelamento.
     */
    private void atualizarParcelasRestantes(Connection conn, int parcelaId) throws SQLException {
        // 1. Obter o ID do parcelamento
        int parcelamentoId = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_GET_PARCELAMENTO_ID)) {
            stmt.setInt(1, parcelaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    parcelamentoId = rs.getInt("parcelamento_id");
                }
            }
        }
        
        if (parcelamentoId == 0) {
            throw new SQLException("Não foi possível encontrar o parcelamento da parcela ID " + parcelaId);
        }
        
        // 2. Contar parcelas não pagas
        int parcelasRestantes = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_UNPAID_PARCELAS)) {
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    parcelasRestantes = rs.getInt("restantes");
                }
            }
        }
        
        // 3. Atualizar o parcelamento
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_PARCELAS_RESTANTES)) {
            stmt.setInt(1, parcelasRestantes);
            stmt.setInt(2, parcelamentoId);
            stmt.executeUpdate();
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
    
    /**
     * Exibe um erro.
     */
    private void exibirErro(String titulo, String mensagem) {
        exibirAlerta(Alert.AlertType.ERROR, titulo, mensagem);
    }
    
    /**
     * Classe para representar um parcelamento na tabela.
     */
    public static class ParcelamentoInfo {
        private final int id;
        private final String descricao;
        private final String valorTotal;
        private final String parcelas;
        private final String restantes;
        private final String status;
        
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
        private final int id;
        private final String numeroParcela;
        private final String valor;
        private final String vencimento;
        private final String status;
        
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