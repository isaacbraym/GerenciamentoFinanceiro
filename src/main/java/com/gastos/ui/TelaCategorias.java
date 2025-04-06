package com.gastos.ui;

import com.gastos.controller.CategoriaController;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;
import com.gastos.ui.base.BaseTelaModal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Tela para gerenciar categorias e subcategorias de despesas.
 * Refatorada para usar BaseTelaModal.
 */
public class TelaCategorias extends BaseTelaModal {

    // Componentes da interface específicos desta tela
    private TableView<CategoriaDespesa> tabelaCategorias;
    private TableView<SubCategoria> tabelaSubcategorias;
    private TextField txtNovaCategoria;
    private TextField txtNovaSubcategoria;
    
    private final CategoriaController categoriaController;

    /**
     * Construtor da tela de categorias.
     */
    public TelaCategorias() {
        // Chama o construtor da classe base com título e dimensões
        super("Gerenciar Categorias", 800, 600);
        this.categoriaController = new CategoriaController();

        // Carregar os dados iniciais
        atualizarTabelaCategorias();
    }

    /**
     * Cria o conteúdo principal (área central do BorderPane).
     */
    @Override
    protected Node criarConteudoPrincipal() {
        // Conteúdo principal
        HBox painelConteudo = new HBox(20);
        painelConteudo.setPadding(new Insets(10, 0, 10, 0)); // Padding para espaçamento

        // Painel de categorias
        VBox painelCategorias = criarPainelCategorias();

        // Painel de subcategorias
        VBox painelSubcategorias = criarPainelSubcategorias();

        painelConteudo.getChildren().addAll(painelCategorias, painelSubcategorias);
        return painelConteudo;
    }

    /**
     * Cria o painel de botões (área inferior do BorderPane).
     */
    @Override
    protected Node criarPainelBotoes() {
        // Usa UIComponentFactory para criar o botão
        Button btnFechar = uiFactory.criarBotaoPrimario("Fechar", e -> fechar());
        // Usa UIComponentFactory para criar o painel de botões
        HBox painelBotoes = uiFactory.criarPainelBotoes(btnFechar);
        // Adiciona padding superior para separar do conteúdo
        painelBotoes.setPadding(new Insets(15, 0, 0, 0));
        return painelBotoes;
    }

    // --- Métodos específicos da TelaCategorias ---

    private VBox criarPainelCategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        painel.setPrefWidth(380);

        Label titulo = criarTituloPainel("Categorias");
        tabelaCategorias = criarTabelaCategorias();
        HBox painelNovaCategoria = criarPainelNovaCategoria();
        HBox painelBotoesCategorias = criarPainelBotoesCategorias();

        painel.getChildren().addAll(titulo, tabelaCategorias, painelNovaCategoria, painelBotoesCategorias);
        VBox.setVgrow(tabelaCategorias, Priority.ALWAYS); // Faz a tabela crescer verticalmente
        return painel;
    }

    private Label criarTituloPainel(String texto) {
        Label titulo = new Label(texto);
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        return titulo;
    }

    private TableView<CategoriaDespesa> criarTabelaCategorias() {
        TableView<CategoriaDespesa> tabela = new TableView<>();
        //tabela.setPrefHeight(300); // Altura agora controlada pelo VBox com VGrow

        TableColumn<CategoriaDespesa, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.prefWidthProperty().bind(tabela.widthProperty().subtract(2)); // Coluna ocupa toda a largura

        tabela.getColumns().add(colunaNome);

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

    private HBox criarPainelNovaCategoria() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);

        txtNovaCategoria = new TextField();
        txtNovaCategoria.setPromptText("Nova categoria");
        HBox.setHgrow(txtNovaCategoria, Priority.ALWAYS); // Faz o TextField crescer

        // Usa UIComponentFactory para criar botão
        Button btnAdicionarCategoria = uiFactory.criarBotaoSucesso("Adicionar", e -> adicionarCategoria());

        painel.getChildren().addAll(txtNovaCategoria, btnAdicionarCategoria);
        return painel;
    }

    private HBox criarPainelBotoesCategorias() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);

        // Usa UIComponentFactory para criar botões
        Button btnEditarCategoria = uiFactory.criarBotaoPrimario("Editar", e -> editarCategoria());
        Button btnExcluirCategoria = uiFactory.criarBotaoPerigo("Excluir", e -> excluirCategoria());

        painel.getChildren().addAll(btnEditarCategoria, btnExcluirCategoria);
        return painel;
    }

    private VBox criarPainelSubcategorias() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        painel.setPrefWidth(380);

        Label titulo = criarTituloPainel("Subcategorias");
        tabelaSubcategorias = criarTabelaSubcategorias();
        HBox painelNovaSubcategoria = criarPainelNovaSubcategoria();
        HBox painelBotoesSubcategorias = criarPainelBotoesSubcategorias();

        painel.getChildren().addAll(titulo, tabelaSubcategorias, painelNovaSubcategoria, painelBotoesSubcategorias);
        VBox.setVgrow(tabelaSubcategorias, Priority.ALWAYS); // Faz a tabela crescer verticalmente
        return painel;
    }

    private TableView<SubCategoria> criarTabelaSubcategorias() {
        TableView<SubCategoria> tabela = new TableView<>();
        //tabela.setPrefHeight(300); // Altura agora controlada pelo VBox com VGrow

        TableColumn<SubCategoria, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.prefWidthProperty().bind(tabela.widthProperty().subtract(2)); // Coluna ocupa toda a largura

        tabela.getColumns().add(colunaNome);
        return tabela;
    }

    private HBox criarPainelNovaSubcategoria() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);

        txtNovaSubcategoria = new TextField();
        txtNovaSubcategoria.setPromptText("Nova subcategoria");
        HBox.setHgrow(txtNovaSubcategoria, Priority.ALWAYS); // Faz o TextField crescer

        // Usa UIComponentFactory para criar botão
        Button btnAdicionarSubcategoria = uiFactory.criarBotaoSucesso("Adicionar", e -> adicionarSubcategoria());

        painel.getChildren().addAll(txtNovaSubcategoria, btnAdicionarSubcategoria);
        return painel;
    }

    private HBox criarPainelBotoesSubcategorias() {
        HBox painel = new HBox(10);
        painel.setAlignment(Pos.CENTER_LEFT);

        // Usa UIComponentFactory para criar botões
        Button btnEditarSubcategoria = uiFactory.criarBotaoPrimario("Editar", e -> editarSubcategoria());
        Button btnExcluirSubcategoria = uiFactory.criarBotaoPerigo("Excluir", e -> excluirSubcategoria());

        painel.getChildren().addAll(btnEditarSubcategoria, btnExcluirSubcategoria);
        return painel;
    }

    // --- Métodos de Lógica (ações dos botões) ---

    private void adicionarCategoria() {
        String nomeCategoria = txtNovaCategoria.getText().trim();
        if (nomeCategoria.isEmpty()) {
            exibirAviso("Campos vazios", "Por favor, informe o nome da categoria.");
            return;
        }
        CategoriaDespesa categoria = new CategoriaDespesa();
        categoria.setNome(nomeCategoria);
        if (categoriaController.salvarCategoria(categoria)) {
            txtNovaCategoria.clear();
            atualizarTabelaCategorias();
            tabelaCategorias.getSelectionModel().select(categoria);
            exibirInformacao("Sucesso", "Categoria adicionada com sucesso!");
        } else {
            exibirErro("Erro", "Erro ao adicionar categoria. Tente novamente.");
        }
    }

    private void editarCategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSelecionada == null) {
            exibirAviso("Seleção vazia", "Por favor, selecione uma categoria para editar.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(categoriaSelecionada.getNome());
        dialog.setTitle("Editar Categoria");
        dialog.setHeaderText("Editar nome da categoria");
        dialog.setContentText("Nome:");
        dialog.initOwner(stage); // Define a janela pai

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String novoNome = resultado.get().trim();
            categoriaSelecionada.setNome(novoNome);
            if (categoriaController.salvarCategoria(categoriaSelecionada)) {
                atualizarTabelaCategorias();
                // Restaurar seleção
                tabelaCategorias.getItems().stream()
                    .filter(c -> c.getId() == categoriaSelecionada.getId())
                    .findFirst()
                    .ifPresent(c -> tabelaCategorias.getSelectionModel().select(c));
                exibirInformacao("Sucesso", "Categoria atualizada com sucesso!");
            } else {
                exibirErro("Erro", "Erro ao atualizar categoria. Tente novamente.");
            }
        }
    }

    private void excluirCategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSelecionada == null) {
            exibirAviso("Seleção vazia", "Por favor, selecione uma categoria para excluir.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Categoria");
        confirmacao.setContentText("Tem certeza que deseja excluir a categoria '" + categoriaSelecionada.getNome() +
                                   "'? Todas as subcategorias associadas também serão excluídas.");
        confirmacao.initOwner(stage); // Define a janela pai

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (categoriaController.excluirCategoria(categoriaSelecionada.getId())) {
                atualizarTabelaCategorias();
                tabelaSubcategorias.getItems().clear();
                exibirInformacao("Sucesso", "Categoria excluída com sucesso!");
            } else {
                exibirErro("Erro", "Erro ao excluir categoria. Tente novamente.");
            }
        }
    }

    private void adicionarSubcategoria() {
        CategoriaDespesa categoriaSelecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSelecionada == null) {
            exibirAviso("Seleção vazia", "Por favor, selecione uma categoria antes de adicionar uma subcategoria.");
            return;
        }
        String nomeSubcategoria = txtNovaSubcategoria.getText().trim();
        if (nomeSubcategoria.isEmpty()) {
            exibirAviso("Campos vazios", "Por favor, informe o nome da subcategoria.");
            return;
        }
        SubCategoria subcategoria = new SubCategoria();
        subcategoria.setNome(nomeSubcategoria);
        if (categoriaController.adicionarSubcategoria(categoriaSelecionada, subcategoria)) {
            txtNovaSubcategoria.clear();
            CategoriaDespesa categoriaAtualizada = categoriaController.buscarCategoriaPorId(categoriaSelecionada.getId());
            if (categoriaAtualizada != null) {
                atualizarTabelaSubcategorias(categoriaAtualizada);
                tabelaCategorias.getSelectionModel().select(categoriaAtualizada);
            }
            exibirInformacao("Sucesso", "Subcategoria adicionada com sucesso!");
        } else {
            exibirErro("Erro", "Erro ao adicionar subcategoria. Tente novamente.");
        }
    }

    private void editarSubcategoria() {
        SubCategoria subcategoriaSelecionada = tabelaSubcategorias.getSelectionModel().getSelectedItem();
        if (subcategoriaSelecionada == null) {
            exibirAviso("Seleção vazia", "Por favor, selecione uma subcategoria para editar.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(subcategoriaSelecionada.getNome());
        dialog.setTitle("Editar Subcategoria");
        dialog.setHeaderText("Editar nome da subcategoria");
        dialog.setContentText("Nome:");
        dialog.initOwner(stage); // Define a janela pai

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
            String novoNome = resultado.get().trim();
            subcategoriaSelecionada.setNome(novoNome);
            if (categoriaController.atualizarSubcategoria(subcategoriaSelecionada)) {
                // Recarregar a categoria pai
                CategoriaDespesa categoriaPai = tabelaCategorias.getSelectionModel().getSelectedItem();
                if (categoriaPai != null) {
                    CategoriaDespesa categoriaAtualizada = categoriaController.buscarCategoriaPorId(categoriaPai.getId());
                    if (categoriaAtualizada != null) {
                        atualizarTabelaSubcategorias(categoriaAtualizada);
                        // Selecionar a subcategoria editada
                        tabelaSubcategorias.getItems().stream()
                            .filter(s -> s.getId() == subcategoriaSelecionada.getId())
                            .findFirst()
                            .ifPresent(s -> tabelaSubcategorias.getSelectionModel().select(s));
                    }
                }
                exibirInformacao("Sucesso", "Subcategoria atualizada com sucesso!");
            } else {
                exibirErro("Erro", "Erro ao atualizar subcategoria. Tente novamente.");
            }
        }
    }

    private void excluirSubcategoria() {
        SubCategoria subcategoriaSelecionada = tabelaSubcategorias.getSelectionModel().getSelectedItem();
        if (subcategoriaSelecionada == null) {
            exibirAviso("Seleção vazia", "Por favor, selecione uma subcategoria para excluir.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Subcategoria");
        confirmacao.setContentText("Tem certeza que deseja excluir a subcategoria '" + subcategoriaSelecionada.getNome() + "'?");
        confirmacao.initOwner(stage); // Define a janela pai

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (categoriaController.excluirSubcategoria(subcategoriaSelecionada.getId())) {
                // Recarregar categoria pai
                CategoriaDespesa categoriaPai = tabelaCategorias.getSelectionModel().getSelectedItem();
                if (categoriaPai != null) {
                    CategoriaDespesa categoriaAtualizada = categoriaController.buscarCategoriaPorId(categoriaPai.getId());
                    if (categoriaAtualizada != null) {
                        atualizarTabelaSubcategorias(categoriaAtualizada);
                    }
                }
                exibirInformacao("Sucesso", "Subcategoria excluída com sucesso!");
            } else {
                exibirErro("Erro", "Erro ao excluir subcategoria. Tente novamente.");
            }
        }
    }

    // --- Métodos de Atualização de Tabelas ---

    private void atualizarTabelaCategorias() {
        ObservableList<CategoriaDespesa> categorias = categoriaController.listarTodasCategorias();
        CategoriaDespesa selecionada = tabelaCategorias.getSelectionModel().getSelectedItem(); // Salva seleção
        tabelaCategorias.setItems(categorias);
        // Tenta restaurar seleção
        if (selecionada != null) {
            tabelaCategorias.getItems().stream()
                .filter(c -> c.getId() == selecionada.getId())
                .findFirst()
                .ifPresent(c -> tabelaCategorias.getSelectionModel().select(c));
        } else if (!categorias.isEmpty()) {
            tabelaCategorias.getSelectionModel().selectFirst(); // Seleciona o primeiro se nada estava selecionado
        } else {
            tabelaSubcategorias.getItems().clear(); // Limpa subcategorias se não há categorias
        }
    }

    private void atualizarTabelaSubcategorias(CategoriaDespesa categoria) {
        if (categoria != null) {
            // É importante buscar as subcategorias novamente caso tenham sido alteradas
            CategoriaDespesa categoriaAtualizada = categoriaController.buscarCategoriaPorId(categoria.getId());
            if(categoriaAtualizada != null) {
                ObservableList<SubCategoria> subcategorias = FXCollections.observableArrayList(categoriaAtualizada.getSubCategorias());
                tabelaSubcategorias.setItems(subcategorias);
            } else {
                tabelaSubcategorias.getItems().clear();
            }
        } else {
            tabelaSubcategorias.getItems().clear();
        }
    }
}