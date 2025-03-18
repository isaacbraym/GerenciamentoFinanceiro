package com.gastos;

import com.gastos.db.ConexaoBanco;
import com.gastos.ui.TelaPrincipal;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principal do aplicativo de gerenciamento financeiro.
 */
public class GerenciadorFinanceiroApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicializa o banco de dados
            ConexaoBanco.inicializarBancoDeDados();
            
            // Cria a tela principal
            TelaPrincipal telaPrincipal = new TelaPrincipal();
            
            // Configura o palco principal
            primaryStage.setTitle("Gerenciador Financeiro");
            primaryStage.setScene(telaPrincipal.getScene());
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Método principal que inicia o aplicativo.
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        // Fecha a conexão com o banco de dados ao encerrar o aplicativo
        ConexaoBanco.fecharConexao();
    }
}