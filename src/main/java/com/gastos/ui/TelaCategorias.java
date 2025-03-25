package com.gastos.ui;

import com.gastos.controller.CategoriaController;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;
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

import java.util.Optional;

/**
 * Tela para gerenciar categorias e subcategorias de despesas.
 */
public class TelaCategorias {
    
    // Constantes para estilo
    private static final String STYLE_PANEL = "-fx-background-color: white; -fx-background-radius: 5;";
    private static final String STYLE_BACKGROUND = "-fx-background-color: #f5f5f5;";
    private static final String STYLE_BTN_PRIMARY = "-fx-background-color: #3498db; -fx-text-fill: white;";
    private static final String STYLE_BTN_SUCCESS = "-fx-background-color: #2ecc71; -fx-text-fill: white;";
    private static final String STYLE_BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white;";
    
    private final Stage janela;
    private final CategoriaController categoriaController;
    
    // Componentes da interface
    private TableView<CategoriaDespesa> tabelaCategorias;
    private TableView<SubCategoria> tabelaSubcategorias;
    private TextField txtNovaCategoria;
    private TextField txtNovaSubcategoria;
    
    /**
     * Construtor da tela de categorias.
     */
    public TelaCategorias() {
        this.categoriaController = new CategoriaController();
        
        // Configurar a janela
        this.janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.setTitle("Gerenciar Categorias");
        janela.setMinWidth(800);
        janela.setMinHeight(600);
        
        // Criar a interface
        criarInterface();
    }
    
    /**
     * Exibe a janela.
     */
    public void mostrar() {
        // Carregar as categorias antes de mostrar a janela
        atualizarTabelaCategorias();
        janela.showAndWait();
    }
    
    /**
     * Cria a interface da tela de categorias.
     */
    private void criarInterface() {
        // Painel principal
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle(STYLE_BACKGROUND);
        
        // Título
        Label lblTitulo = new Label("Gerenciamento de Categorias");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        painelPrincipal.setTop(lblTitulo);
        
        // Conteúdo principal
        HBox painelConteudo = new HBox(20);
        
        // Painel de categorias
        VBox painelCategorias = criarPainelCategorias();
        
        // Painel de subcategorias
        VBox painelSubcategorias = criarPainelSubcategorias();
        
        painelConteudo.getChildren().addAll(painelCategorias, painelSubcategorias);
        painelPrincipal.setCenter(painelConteudo);
        
        // Botões de ação principais
        HBox painelBotoes = criarPainelBotoes();
        painelPrincipal.setBottom(painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal);
        janela.setScene(cena);
    }
    
    /**
     * Cria o painel de categorias.
     */
    private VBox criarPainelCategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle(STYLE_PANEL);
        painel.setPrefWidth(380);
        
        // Título do painel
        Label titulo = criarTituloPainel("Categorias");
        
        // Tabela de categorias
        tabelaCategorias = criarTabelaCategorias();
        
        // Painel para adicionar nova categoria
        HBox painelNovaCategoria = criarPainelNovaCategoria();
        
        // Botões de ação para categorias
        HBox painelBotoesCategorias = criarPainelBotoesCategorias();
        
        painel.getChildren().addAll(titulo, tabelaCategorias, painelNovaCategoria, painelBotoesCategorias);
        
        return painel;
    }
    
    /**
     * Cria o título para um painel.
     */
    private Label criarTituloPainel(String texto) {
        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        return titulo;
    }
    
    /**
     * Cria a tabela de categorias.
     */
    private TableView<CategoriaDespesa> criarTabelaCategorias() {
        TableView<CategoriaDespesa> tabela = new TableView<>();
        tabela.setPrefHeight(300);
        
        // Configurar colunas
        TableColumn<CategoriaDespesa, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(340);
        
        tabela.getColumns().add(colunaNome);
        
        // Adicionar listener para selecionar categoria
        tabela.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        atualizarTabelaSubcategorias(newValue);
                    } else {
                        tabelaSubcategorias.getItems().clear();
                    }
                });
        
        return tabela;
    }
    
    /**
     * Cria o painel para adicionar nova categoria.
     */
    private HBox criarPainelNovaCategoria() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);
        
        txtNovaCategoria = new TextField();
        txtNovaCategoria.setPromptText("Nova categoria");
        txtNovaCategoria.setPrefWidth(250);
        
        Button btnAdicionarCategoria = new Button("Adicionar");
        btnAdicionarCategoria.setStyle(STYLE_BTN_SUCCESS);
        btnAdicionarCategoria.setOnAction(e -> adicionarCategoria());
        
        painel.getChildren().addAll(txtNovaCategoria, btnAdicionarCategoria);
        return painel;
    }
    
    /**
     * Cria o painel de botões para categorias.
     */
    private HBox criarPainelBotoesCategorias() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);
        
        Button btnEditarCategoria = new Button("Editar");
        btnEditarCategoria.setStyle(STYLE_BTN_PRIMARY);
        btnEditarCategoria.setOnAction(e -> editarCategoria());
        
        Button btnExcluirCategoria = new Button("Excluir");
        btnExcluirCategoria.setStyle(STYLE_BTN_DANGER);
        btnExcluirCategoria.setOnAction(e -> excluirCategoria());
        
        painel.getChildren().addAll(btnEditarCategoria, btnExcluirCategoria);
        return painel;
    }
    
    /**
     * Cria o painel de subcategorias.
     */
    private VBox criarPainelSubcategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle(STYLE_PANEL);
        painel.setPrefWidth(380);
        
        // Título do painel
        Label titulo = criarTituloPainel("Subcategorias");
        
        // Tabela de subcategorias
        tabelaSubcategorias = criarTabelaSubcategorias();
        
        // Painel para adicionar nova subcategoria
        HBox painelNovaSubcategoria = criarPainelNovaSubcategoria();
        
        // Botões de ação para subcategorias
        HBox painelBotoesSubcategorias = criarPainelBotoesSubcategorias();
        
        painel.getChildren().addAll(titulo, tabelaSubcategorias, painelNovaSubcategoria, painelBotoesSubcategorias);
        
        return painel;
    }
    
    /**
     * Cria a tabela de subcategorias.
     */
    private TableView<SubCategoria> criarTabelaSubcategorias() {
        TableView<SubCategoria> tabela = new TableView<>();
        tabela.setPrefHeight(300);
        
        // Configurar colunas
        TableColumn<SubCategoria, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(340);
        
        tabela.getColumns().add(colunaNome);
        return tabela;
    }
    
    /**
     * Cria o painel para adicionar nova subcategoria.
     */
    private HBox criarPainelNovaSubcategoria() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);
        
        txtNovaSubcategoria = new TextField();
        txtNovaSubcategoria.setPromptText("Nova subcategoria");
        txtNovaSubcategoria.setPrefWidth(250);
        
        Button btnAdicionarSubcategoria = new Button("Adicionar");
        btnAdicionarSubcategoria.setStyle(STYLE_BTN_SUCCESS);
        btnAdicionarSubcategoria.setOnAction(e -> adicionarSubcategoria());
        
        painel.getChildren().addAll(txtNovaSubcategoria, btnAdicionarSubcategoria);
        return painel;
    }
    
    /**
     * Cria o painel de botões para subcategorias.
     */
    private HBox criarPainelBotoesSubcategorias() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);
        
        Button btnEditarSubcategoria = new Button("Editar");
        btnEditarSubcategoria.setStyle(STYLE_BTN_PRIMARY);
        btnEditarSubcategoria.setOnAction(e -> editarSubcategoria());
        
        Button btnExcluirSubcategoria = new Button("Excluir");
        btnExcluirSubcategoria.setStyle(STYLE_BTN_DANGER);
        btnExcluirSubcategoria.setOnAction(e -> excluirSubcategoria());
        
        painel.getChildren().addAll(btnEditarSubcategoria, btnExcluirSubcategoria);
        return painel;
    }
    
    /**
     * Cria o painel de botões principais.
     */
    private HBox criarPainelBotoes() {
        HBox painel = new HBox(15);
        painel.setAlignment(Pos.CENTER_RIGHT);
        painel.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle(STYLE_BTN_PRIMARY);
        btnFechar.setOnAction(e -> janela.close());
        
        painel.getChildren().add(btnFechar);
        return painel;
    }
    
    /**
     * Adiciona uma nova categoria.
     */
    private void adicionarCategoria() {
        String nomeCategoria = txtNovaCategoria.getText().trim();
        
        if (nomeCategoria.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos vazios", "Por favor, informe o nome da categoria.");
            return;
        }
        
        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setNome(nomeCategoria);
        
        if (categoriaController.salvarCategoria(categoria)) {
            txtNovaCategoria.clear();
            
            // Atualizar a tabela após salvar
            atualizarTabelaCategorias();
            
            // Selecionar a categoria recém-adicionada
            tabelaCategorias.getSelectionModel().select(categoria);
            
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Categoria adicionada com sucesso!");
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao adicionar categoria. Tente novamente.");
        }
    }
    
    /**
     * Edita a categoria selecionada.
     */
    private void editarCategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        
        if (categoriaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", "Por favor, selecione uma categoria para editar.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(categoriaSelecionada.getNome());
        dialog.setTitle("Editar Categoria");
        dialog.setHeaderText("Editar nome da categoria");
        dialog.setContentText("Nome:");
        
        Optional<String> resultado = dialog.showAndWait();
        
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String novoNome = resultado.get().trim();
            categoriaSelecionada.setNome(novoNome);
            
            if (categoriaController.salvarCategoria(categoriaSelecionada)) {
                // Atualizar a tabela após editar
                atualizarTabelaCategorias();
                
                // Selecionar novamente a categoria editada
                tabelaCategorias.getItems().stream()
                    .filter(c -> c.getId() == categoriaSelecionada.getId())
                    .findFirst()
                    .ifPresent(c -> tabelaCategorias.getSelectionModel().select(c));
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Categoria atualizada com sucesso!");
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao atualizar categoria. Tente novamente.");
            }
        }
    }
    
    /**
     * Exclui a categoria selecionada.
     */
    private void excluirCategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        
        if (categoriaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", "Por favor, selecione uma categoria para excluir.");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Categoria");
        confirmacao.setContentText("Tem certeza que deseja excluir a categoria '" + categoriaSelecionada.getNome() + 
                                   "'? Todas as subcategorias associadas também serão excluídas.");
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (categoriaController.excluirCategoria(categoriaSelecionada.getId())) {
                // Atualizar as tabelas após excluir
                atualizarTabelaCategorias();
                tabelaSubcategorias.getItems().clear();
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Categoria excluída com sucesso!");
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao excluir categoria. Tente novamente.");
            }
        }
    }
    
    /**
     * Adiciona uma nova subcategoria.
     */
    private void adicionarSubcategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        
        if (categoriaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", 
                         "Por favor, selecione uma categoria antes de adicionar uma subcategoria.");
            return;
        }
        
        String nomeSubcategoria = txtNovaSubcategoria.getText().trim();
        
        if (nomeSubcategoria.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos vazios", "Por favor, informe o nome da subcategoria.");
            return;
        }
        
        SubCategoria subcategoria = new SubCategoria();
        subcategoria.setNome(nomeSubcategoria);
        
        if (categoriaController.adicionarSubcategoria(categoriaSelecionada, subcategoria)) {
            txtNovaSubcategoria.clear();
            
            // Atualizar categoria para obter subcategorias atualizadas
            categoriaSelecionada = categoriaController.buscarCategoriaPorId(categoriaSelecionada.getId());
            
            // Atualizar a tabela de subcategorias
            if (categoriaSelecionada != null) {
                atualizarTabelaSubcategorias(categoriaSelecionada);
            }
            
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Subcategoria adicionada com sucesso!");
        } else {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao adicionar subcategoria. Tente novamente.");
        }
    }
    
    /**
     * Edita a subcategoria selecionada.
     */
    private void editarSubcategoria() {
        SubCategoria subcategoriaSelecionada = tabelaSubcategorias.getSelectionModel().getSelectedItem();
        
        if (subcategoriaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", "Por favor, selecione uma subcategoria para editar.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(subcategoriaSelecionada.getNome());
        dialog.setTitle("Editar Subcategoria");
        dialog.setHeaderText("Editar nome da subcategoria");
        dialog.setContentText("Nome:");
        
        Optional<String> resultado = dialog.showAndWait();
        
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String novoNome = resultado.get().trim();
            subcategoriaSelecionada.setNome(novoNome);
            
            if (categoriaController.atualizarSubcategoria(subcategoriaSelecionada)) {
                // Atualizar categoria para obter subcategorias atualizadas
                CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
                if (categoriaSelecionada != null) {
                    categoriaSelecionada = categoriaController.buscarCategoriaPorId(categoriaSelecionada.getId());
                    atualizarTabelaSubcategorias(categoriaSelecionada);
                }
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Subcategoria atualizada com sucesso!");
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao atualizar subcategoria. Tente novamente.");
            }
        }
    }
    
    /**
     * Exclui a subcategoria selecionada.
     */
    private void excluirSubcategoria() {
        SubCategoria subcategoriaSelecionada = tabelaSubcategorias.getSelectionModel().getSelectedItem();
        
        if (subcategoriaSelecionada == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", "Por favor, selecione uma subcategoria para excluir.");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Subcategoria");
        confirmacao.setContentText("Tem certeza que deseja excluir a subcategoria '" + subcategoriaSelecionada.getNome() + "'?");
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (categoriaController.excluirSubcategoria(subcategoriaSelecionada.getId())) {
                // Atualizar categoria para obter subcategorias atualizadas
                CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
                if (categoriaSelecionada != null) {
                    categoriaSelecionada = categoriaController.buscarCategoriaPorId(categoriaSelecionada.getId());
                    atualizarTabelaSubcategorias(categoriaSelecionada);
                }
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Subcategoria excluída com sucesso!");
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao excluir subcategoria. Tente novamente.");
            }
        }
    }
    
    /**
     * Atualiza a tabela de categorias.
     */
    private void atualizarTabelaCategorias() {
        // Buscar categorias atualizadas
        ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
        
        // Salvar a seleção atual
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        int categoriaIdSelecionada = (categoriaSelecionada != null) ? categoriaSelecionada.getId() : -1;
        
        // Atualizar a tabela
        tabelaCategorias.setItems(categorias);
        
        // Tentar restaurar a seleção
        if (categoriaIdSelecionada != -1) {
            categorias.stream()
                .filter(c -> c.getId() == categoriaIdSelecionada)
                .findFirst()
                .ifPresent(c -> tabelaCategorias.getSelectionModel().select(c));
        }
    }
    
    /**
     * Atualiza a tabela de subcategorias com base na categoria selecionada.
     */
    private void atualizarTabelaSubcategorias(CategoriaDespesa categoria) {
        if (categoria != null) {
            ObservableList<SubCategoria> subcategorias = FXCollections.observableArrayList(categoria.getSubCategorias());
            tabelaSubcategorias.setItems(subcategorias);
        } else {
            tabelaSubcategorias.getItems().clear();
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