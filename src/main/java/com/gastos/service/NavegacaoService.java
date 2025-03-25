package com.gastos.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import com.gastos.ui.TelaCadastroDespesa;
import com.gastos.ui.TelaCadastroPessoa;
import com.gastos.ui.TelaCategorias;
import com.gastos.ui.TelaParcelamentos;
import com.gastos.ui.TelaTodasDespesas;

/**
 * Serviço para navegação entre telas e exibição de diálogos.
 * Versão atualizada com suporte à tela de cadastro de pessoas.
 */
public class NavegacaoService {
    
    private final Runnable atualizarDashboardCallback;
    
    /**
     * Construtor que recebe um callback para atualizar o dashboard após navegação.
     * 
     * @param atualizarDashboardCallback Callback para atualizar o dashboard
     */
    public NavegacaoService(Runnable atualizarDashboardCallback) {
        this.atualizarDashboardCallback = atualizarDashboardCallback;
    }
    
    /**
     * Abre a tela de cadastro de nova despesa.
     */
    public void abrirTelaNovaDespesa() {
        TelaCadastroDespesa telaNovaDespesa = new TelaCadastroDespesa();
        telaNovaDespesa.mostrar();
        Platform.runLater(atualizarDashboardCallback);
    }

    /**
     * Abre a tela de gerenciamento de categorias.
     */
    public void abrirTelaCategorias() {
        TelaCategorias telaCategorias = new TelaCategorias();
        telaCategorias.mostrar();
        Platform.runLater(atualizarDashboardCallback);
    }
    
    /**
     * Abre a tela de gerenciamento de pessoas.
     */
    public void abrirTelaCadastroPessoa() {
        TelaCadastroPessoa telaPessoas = new TelaCadastroPessoa();
        telaPessoas.mostrar();
        Platform.runLater(atualizarDashboardCallback);
    }

    /**
     * Abre a tela de gerenciamento de parcelamentos.
     */
    public void abrirTelaParcelamentos() {
        try {
            TelaParcelamentos telaParcelamentos = new TelaParcelamentos();
            telaParcelamentos.mostrar();
            Platform.runLater(atualizarDashboardCallback);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de parcelamentos", e.getMessage());
        }
    }

    /**
     * Abre a tela com todas as despesas.
     */
    public void abrirTelaTodasDespesas() {
        try {
            TelaTodasDespesas telaTodasDespesas = new TelaTodasDespesas();
            telaTodasDespesas.mostrar();
            Platform.runLater(atualizarDashboardCallback);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de todas as despesas", e.getMessage());
        }
    }

    /**
     * Mostra alerta de funcionalidade em desenvolvimento.
     */
    public void mostrarTelaEmDesenvolvimento() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Em desenvolvimento");
        alert.setHeaderText(null);
        alert.setContentText("Esta funcionalidade está em desenvolvimento.");
        alert.showAndWait();
    }

    /**
     * Mostra uma mensagem de erro.
     */
    public void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}