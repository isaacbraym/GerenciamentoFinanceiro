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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Tela para gerenciar categorias e subcategorias de despesas.
 */
public class TelaCategorias {
    
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
        painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");
        
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
        HBox painelBotoes = new HBox(15);
        painelBotoes.setAlignment(Pos.CENTER_RIGHT);
        painelBotoes.setPadding(new Insets(10, 0, 0, 0));
        
        Button btnResetar = new Button("Resetar Banco de Dados");
        btnResetar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnResetar.setOnAction(e -> resetarBancoDados());
        
        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnFechar.setOnAction(e -> janela.close());
        
        painelBotoes.getChildren().addAll(btnResetar, btnFechar);
        painelPrincipal.setBottom(painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal);
        janela.setScene(cena);
    }
    
    /**
     * Cria o painel de categorias.
     * @return o painel de categorias
     */
    private VBox criarPainelCategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        painel.setPrefWidth(380);
        
        // Título do painel
        Label titulo = new Label("Categorias");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Tabela de categorias
        tabelaCategorias = new TableView<>();
        tabelaCategorias.setPrefHeight(300);
        
        // Configurar colunas
        TableColumn<CategoriaDespesa, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(340);
        
        tabelaCategorias.getColumns().add(colunaNome);
        
        // Adicionar listener para selecionar categoria
        tabelaCategorias.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        atualizarTabelaSubcategorias(newValue);
                    } else {
                        tabelaSubcategorias.getItems().clear();
                    }
                });
        
        // Painel para adicionar nova categoria
        HBox painelNovaCategoria = new HBox(10);
        painelNovaCategoria.setAlignment(Pos.CENTER_LEFT);
        
        txtNovaCategoria = new TextField();
        txtNovaCategoria.setPromptText("Nova categoria");
        txtNovaCategoria.setPrefWidth(250);
        
        Button btnAdicionarCategoria = new Button("Adicionar");
        btnAdicionarCategoria.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnAdicionarCategoria.setOnAction(e -> adicionarCategoria());
        
        painelNovaCategoria.getChildren().addAll(txtNovaCategoria, btnAdicionarCategoria);
        
        // Botões de ação para categorias
        HBox painelBotoesCategorias = new HBox(10);
        painelBotoesCategorias.setAlignment(Pos.CENTER_LEFT);
        
        Button btnEditarCategoria = new Button("Editar");
        btnEditarCategoria.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnEditarCategoria.setOnAction(e -> editarCategoria());
        
        Button btnExcluirCategoria = new Button("Excluir");
        btnExcluirCategoria.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnExcluirCategoria.setOnAction(e -> excluirCategoria());
        
        painelBotoesCategorias.getChildren().addAll(btnEditarCategoria, btnExcluirCategoria);
        
        painel.getChildren().addAll(titulo, tabelaCategorias, painelNovaCategoria, painelBotoesCategorias);
        
        return painel;
    }
    
    /**
     * Cria o painel de subcategorias.
     * @return o painel de subcategorias
     */
    private VBox criarPainelSubcategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        painel.setPrefWidth(380);
        
        // Título do painel
        Label titulo = new Label("Subcategorias");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Tabela de subcategorias
        tabelaSubcategorias = new TableView<>();
        tabelaSubcategorias.setPrefHeight(300);
        
        // Configurar colunas
        TableColumn<SubCategoria, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(340);
        
        tabelaSubcategorias.getColumns().add(colunaNome);
        
        // Painel para adicionar nova subcategoria
        HBox painelNovaSubcategoria = new HBox(10);
        painelNovaSubcategoria.setAlignment(Pos.CENTER_LEFT);
        
        txtNovaSubcategoria = new TextField();
        txtNovaSubcategoria.setPromptText("Nova subcategoria");
        txtNovaSubcategoria.setPrefWidth(250);
        
        Button btnAdicionarSubcategoria = new Button("Adicionar");
        btnAdicionarSubcategoria.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnAdicionarSubcategoria.setOnAction(e -> adicionarSubcategoria());
        
        painelNovaSubcategoria.getChildren().addAll(txtNovaSubcategoria, btnAdicionarSubcategoria);
        
        // Botões de ação para subcategorias
        HBox painelBotoesSubcategorias = new HBox(10);
        painelBotoesSubcategorias.setAlignment(Pos.CENTER_LEFT);
        
        Button btnEditarSubcategoria = new Button("Editar");
        btnEditarSubcategoria.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnEditarSubcategoria.setOnAction(e -> editarSubcategoria());
        
        Button btnExcluirSubcategoria = new Button("Excluir");
        btnExcluirSubcategoria.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnExcluirSubcategoria.setOnAction(e -> excluirSubcategoria());
        
        painelBotoesSubcategorias.getChildren().addAll(btnEditarSubcategoria, btnExcluirSubcategoria);
        
        painel.getChildren().addAll(titulo, tabelaSubcategorias, painelNovaSubcategoria, painelBotoesSubcategorias);
        
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
            
            // IMPORTANTE: Atualizar a tabela após salvar
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
                // IMPORTANTE: Atualizar a tabela após editar
                atualizarTabelaCategorias();
                
                // Selecionar novamente a categoria editada
                for (CategoriaDespesa cat : tabelaCategorias.getItems()) {
                    if (cat.getId() == categoriaSelecionada.getId()) {
                        tabelaCategorias.getSelectionModel().select(cat);
                        break;
                    }
                }
                
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
                // IMPORTANTE: Atualizar as tabelas após excluir
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
            exibirAlerta(Alert.AlertType.WARNING, "Seleção vazia", "Por favor, selecione uma categoria antes de adicionar uma subcategoria.");
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
        System.out.println("Atualizando tabela de categorias...");
        
        // Buscar categorias atualizadas
        ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
        
        // Salvar a seleção atual
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        int categoriaIdSelecionada = (categoriaSelecionada != null) ? categoriaSelecionada.getId() : -1;
        
        // Atualizar a tabela
        tabelaCategorias.setItems(categorias);
        
        // Tentar restaurar a seleção
        if (categoriaIdSelecionada != -1) {
            for (CategoriaDespesa categoria : categorias) {
                if (categoria.getId() == categoriaIdSelecionada) {
                    tabelaCategorias.getSelectionModel().select(categoria);
                    break;
                }
            }
        }
        
        // Se não houver categorias, exibir uma mensagem
        if (categorias.isEmpty()) {
            System.out.println("Nenhuma categoria encontrada na tabela.");
        }
    }
    
    /**
     * Atualiza a tabela de subcategorias com base na categoria selecionada.
     * @param categoria a categoria selecionada
     */
    private void atualizarTabelaSubcategorias(CategoriaDespesa categoria) {
        if (categoria != null) {
            System.out.println("Atualizando subcategorias para categoria: " + categoria.getNome());
            ObservableList<SubCategoria> subcategorias = FXCollections.observableArrayList(categoria.getSubCategorias());
            tabelaSubcategorias.setItems(subcategorias);
            
            if (subcategorias.isEmpty()) {
                System.out.println("Nenhuma subcategoria encontrada para esta categoria.");
            } else {
                System.out.println("Subcategorias encontradas: " + subcategorias.size());
            }
        } else {
            tabelaSubcategorias.getItems().clear();
        }
    }
    
    /**
     * Exibe um alerta.
     * @param tipo o tipo de alerta
     * @param titulo o título do alerta
     * @param mensagem a mensagem do alerta
     */
    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
    
    /**
     * Reseta o banco de dados (para resolução de problemas).
     */
    private void resetarBancoDados() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Resetar Banco de Dados");
        confirmacao.setContentText("Esta operação irá apagar TODOS os dados e recriar o banco de dados. Tem certeza que deseja continuar?");
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // Chamar o método de reset do banco de dados
                com.gastos.db.ConexaoBanco.resetarBancoDados();
                
                // Atualizar a interface
                atualizarTabelaCategorias();
                tabelaSubcategorias.getItems().clear();
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Banco de dados reinicializado com sucesso!");
            } catch (Exception e) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao resetar banco de dados: " + e.getMessage());
            }
        }
    }
}