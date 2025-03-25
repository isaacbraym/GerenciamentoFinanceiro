package com.gastos.service;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Fábrica para criação de componentes de interface do usuário.
 * Centraliza a criação de elementos visuais reutilizáveis.
 */
public class UIComponentFactory {

    // Constantes para estilos
    private static final String STYLE_CARD_TOTAL = "-fx-background-color: #3498db; -fx-background-radius: 10;";
    private static final String STYLE_CARD_PAGO = "-fx-background-color: #2ecc71; -fx-background-radius: 10;";
    private static final String STYLE_CARD_A_PAGAR = "-fx-background-color: #e74c3c; -fx-background-radius: 10;";
    private static final String STYLE_PANEL = "-fx-background-color: white; -fx-background-radius: 10;";
    private static final String STYLE_BTN_PRIMARY = "-fx-background-color: #3498db; -fx-text-fill: white;";
    private static final String STYLE_BTN_SUCCESS = "-fx-background-color: #2ecc71; -fx-text-fill: white;";
    private static final String STYLE_BTN_DANGER = "-fx-background-color: #e74c3c; -fx-text-fill: white;";
    private static final String STYLE_BTN_MENU = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";
    private static final String STYLE_BTN_MENU_HOVER = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER-LEFT;";
    private static final String STYLE_BTN_SUBMENU = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 15;";
    private static final String STYLE_BTN_SUBMENU_HOVER = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 15;";
    
    /**
     * Cria um cartão de resumo financeiro.
     * 
     * @param titulo Título do cartão
     * @param valor Valor financeiro a exibir
     * @param estilo Estilo CSS do cartão
     * @return VBox configurado como cartão
     */
    public VBox criarCartaoResumo(String titulo, String valor, String estilo) {
        VBox cartao = new VBox(10);
        cartao.setPadding(new Insets(20));
        cartao.setPrefWidth(300);
        cartao.setPrefHeight(150);
        cartao.setStyle(estilo);
        cartao.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.WHITE);

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblValor.setTextFill(Color.WHITE);

        cartao.getChildren().addAll(lblTitulo, lblValor);
        return cartao;
    }
    
    /**
     * Cria um cartão de resumo financeiro para o total de despesas.
     * 
     * @param valor Valor financeiro a exibir
     * @return VBox configurado como cartão
     */
    public VBox criarCartaoTotalDespesas(String valor) {
        return criarCartaoResumo("Total Despesas do Mês", valor, STYLE_CARD_TOTAL);
    }
    
    /**
     * Cria um cartão de resumo financeiro para o total pago.
     * 
     * @param valor Valor financeiro a exibir
     * @return VBox configurado como cartão
     */
    public VBox criarCartaoTotalPago(String valor) {
        return criarCartaoResumo("Total Pago", valor, STYLE_CARD_PAGO);
    }
    
    /**
     * Cria um cartão de resumo financeiro para o total a pagar.
     * 
     * @param valor Valor financeiro a exibir
     * @return VBox configurado como cartão
     */
    public VBox criarCartaoTotalAPagar(String valor) {
        return criarCartaoResumo("Total a Pagar", valor, STYLE_CARD_A_PAGAR);
    }
    
    /**
     * Cria um botão para o menu lateral.
     * 
     * @param texto Texto do botão
     * @param id ID do botão
     * @param acao Ação a ser executada ao clicar
     * @return Botão configurado
     */
    public Button criarBotaoMenu(String texto, String id, EventHandler<ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setId(id);
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setPrefHeight(40);
        botao.setStyle(STYLE_BTN_MENU);

        // Efeito hover
        botao.setOnMouseEntered(e -> botao.setStyle(STYLE_BTN_MENU_HOVER));
        botao.setOnMouseExited(e -> botao.setStyle(STYLE_BTN_MENU));

        // Configurar evento de clique
        botao.setOnAction(acao);

        return botao;
    }
    
    /**
     * Cria um botão para o submenu.
     * 
     * @param texto Texto do botão
     * @param id ID do botão
     * @param acao Ação a ser executada ao clicar
     * @return Botão configurado
     */
    public Button criarBotaoSubMenu(String texto, String id, EventHandler<ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setId(id);
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setPrefHeight(35);
        botao.setStyle(STYLE_BTN_SUBMENU);

        // Efeito hover
        botao.setOnMouseEntered(e -> botao.setStyle(STYLE_BTN_SUBMENU_HOVER));
        botao.setOnMouseExited(e -> botao.setStyle(STYLE_BTN_SUBMENU));

        // Configurar evento de clique
        botao.setOnAction(acao);

        return botao;
    }
    
    /**
     * Cria um painel com título.
     * 
     * @param titulo Título do painel
     * @return VBox configurado como painel com título
     */
    public VBox criarPainelComTitulo(String titulo) {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(20));
        painel.setStyle(STYLE_PANEL);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        painel.getChildren().add(lblTitulo);
        return painel;
    }
    
    /**
     * Cria um botão primário (azul).
     * 
     * @param texto Texto do botão
     * @param acao Ação a ser executada ao clicar
     * @return Botão configurado
     */
    public Button criarBotaoPrimario(String texto, EventHandler<ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setStyle(STYLE_BTN_PRIMARY);
        botao.setOnAction(acao);
        return botao;
    }
    
    /**
     * Cria um botão de sucesso (verde).
     * 
     * @param texto Texto do botão
     * @param acao Ação a ser executada ao clicar
     * @return Botão configurado
     */
    public Button criarBotaoSucesso(String texto, EventHandler<ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setStyle(STYLE_BTN_SUCCESS);
        botao.setOnAction(acao);
        return botao;
    }
    
    /**
     * Cria um botão de perigo (vermelho).
     * 
     * @param texto Texto do botão
     * @param acao Ação a ser executada ao clicar
     * @return Botão configurado
     */
    public Button criarBotaoPerigo(String texto, EventHandler<ActionEvent> acao) {
        Button botao = new Button(texto);
        botao.setStyle(STYLE_BTN_DANGER);
        botao.setOnAction(acao);
        return botao;
    }
    
    /**
     * Cria um painel horizontal para botões, alinhados à direita.
     * 
     * @param botoes Botões a serem incluídos no painel
     * @return HBox configurado com os botões
     */
    public HBox criarPainelBotoes(Button... botoes) {
        HBox painel = new HBox(15);
        painel.setPadding(new Insets(15, 0, 0, 0));
        painel.setAlignment(Pos.CENTER_RIGHT);
        
        painel.getChildren().addAll(botoes);
        return painel;
    }
}