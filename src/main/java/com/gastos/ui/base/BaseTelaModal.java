package com.gastos.ui.base; // Crie um novo pacote ui.base

import com.gastos.service.UIComponentFactory; // Assume que UIComponentFactory está acessível
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Classe base abstrata para telas modais da aplicação.
 * Encapsula a criação da janela (Stage), cena (Scene) e layout básico.
 */
public abstract class BaseTelaModal {

    protected final Stage stage;
    protected final BorderPane rootPane;
    protected final UIComponentFactory uiFactory; // Para consistência na criação de botões

    /**
     * Construtor da tela modal base.
     *
     * @param titulo      Título da janela.
     * @param minLargura  Largura mínima da janela.
     * @param minAltura   Altura mínima da janela.
     */
    public BaseTelaModal(String titulo, double minLargura, double minAltura) {
        this.uiFactory = new UIComponentFactory(); // Instancia a fábrica de componentes

        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(titulo);
        stage.setMinWidth(minLargura);
        stage.setMinHeight(minAltura);

        this.rootPane = new BorderPane();
        rootPane.setPadding(new Insets(20));
        rootPane.setStyle("-fx-background-color: #f5f5f5;"); // Estilo base

        // Define as áreas do BorderPane chamando os métodos abstratos
        rootPane.setCenter(criarConteudoPrincipal());
        rootPane.setBottom(criarPainelBotoes());

        // Cria a cena
        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
    }

    /**
     * Método abstrato que as subclasses devem implementar para criar
     * o conteúdo principal da tela (geralmente a área central).
     *
     * @return O Node contendo o conteúdo principal.
     */
    protected abstract Node criarConteudoPrincipal();

    /**
     * Método abstrato que as subclasses devem implementar para criar
     * o painel de botões de ação (geralmente na parte inferior).
     *
     * @return O Node contendo o painel de botões.
     */
    protected abstract Node criarPainelBotoes();

    /**
     * Exibe a janela modal e aguarda até que ela seja fechada.
     */
    public void mostrar() {
        stage.showAndWait();
    }

    /**
     * Fecha a janela modal.
     */
    protected void fechar() {
        stage.close();
    }

    /**
     * Exibe um alerta simples para o usuário.
     *
     * @param tipo     O tipo de alerta (INFORMATION, WARNING, ERROR, etc.).
     * @param titulo   O título do alerta.
     * @param mensagem A mensagem a ser exibida.
     */
    protected void exibirAlerta(AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null); // Sem cabeçalho extra
        alerta.setContentText(mensagem);
        alerta.initOwner(stage); // Define a janela pai para o alerta
        alerta.showAndWait();
    }

    /**
     * Atalho para exibir um alerta de erro.
     */
    protected void exibirErro(String titulo, String mensagem) {
        exibirAlerta(AlertType.ERROR, titulo, mensagem);
    }

    /**
     * Atalho para exibir um alerta de informação.
     */
    protected void exibirInformacao(String titulo, String mensagem) {
        exibirAlerta(AlertType.INFORMATION, titulo, mensagem);
    }

    /**
     * Atalho para exibir um alerta de aviso.
     */
    protected void exibirAviso(String titulo, String mensagem) {
        exibirAlerta(AlertType.WARNING, titulo, mensagem);
    }

    /**
     * Retorna a instância do Stage gerenciada por esta tela.
     * @return O Stage.
     */
    public Stage getStage() {
        return stage;
    }
}