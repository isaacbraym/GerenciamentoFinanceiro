package com.gastos;

import com.gastos.db.ConexaoBanco;
import com.gastos.ui.TelaPrincipal;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Classe principal do aplicativo de gerenciamento financeiro.
 * Responsável por inicializar o sistema e gerenciar o ciclo de vida da aplicação.
 */
public class GerenciadorFinanceiroApp extends Application {
    
    // Constantes da aplicação
    private static final String APP_TITLE = "Gerenciador Financeiro";
    private static final int MIN_WIDTH = 1024;
    private static final int MIN_HEIGHT = 768;
    
    /**
     * Método de inicialização da interface gráfica.
     * @param primaryStage o palco principal da aplicação
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            inicializarAplicacao(primaryStage);
        } catch (Exception e) {
            tratarErroInicializacao(e);
        }
    }
    
    /**
     * Inicializa os componentes da aplicação.
     * @param primaryStage o palco principal da aplicação
     * @throws Exception se ocorrer um erro durante a inicialização
     */
    private void inicializarAplicacao(Stage primaryStage) throws Exception {
        // Inicializar o banco de dados
        ConexaoBanco.inicializarBancoDeDados();
        
        // Configurar e exibir a tela principal
        configurarTelaPrincipal(primaryStage);
    }
    
    /**
     * Configura a tela principal da aplicação.
     * @param primaryStage o palco principal da aplicação
     */
    private void configurarTelaPrincipal(Stage primaryStage) {
        // Criar a tela principal
        TelaPrincipal telaPrincipal = new TelaPrincipal();
        
        // Configurar o palco principal
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(telaPrincipal.getScene());
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();
    }
    
    /**
     * Trata erros que ocorrem durante a inicialização da aplicação.
     * @param e a exceção que ocorreu
     */
    private void tratarErroInicializacao(Exception e) {
        System.err.println("Erro ao inicializar a aplicação: " + e.getMessage());
        e.printStackTrace();
        
        // Em caso de erro crítico, encerrar a aplicação
        Platform.exit();
    }
    
    /**
     * Método principal que inicia o aplicativo.
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Método executado ao encerrar a aplicação.
     * Responsável por limpeza e fechamento de recursos.
     */
    @Override
    public void stop() {
        try {
            // Fecha a conexão com o banco de dados ao encerrar o aplicativo
            ConexaoBanco.fecharConexao();
            System.out.println("Aplicação encerrada com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao encerrar a aplicação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}