package com.gastos.service;

import java.io.File;
import java.util.function.Consumer;

import com.gastos.controller.ResponsavelController;
import com.gastos.model.Responsavel;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Serviço para gerenciamento de avatares dos responsáveis.
 * Adaptado da classe GerenciadorAvatares existente.
 */
public class AvatarService {
    
    // Constantes para estilo
    private static final String STYLE_AVATAR_SELECTED = "-fx-effect: dropshadow(three-pass-box, #3498db, 10, 0.5, 0, 0);";
    
    private final HBox avatarContainer;
    private final ResponsavelController responsavelController;
    private Integer responsavelSelecionadoId = null;
    private Consumer<Responsavel> aoSelecionarCallback;
    
    /**
     * Construtor que inicializa o serviço de avatares.
     * 
     * @param container Container onde os avatares serão exibidos
     * @param aoSelecionar Callback executado quando um avatar é selecionado
     */
    public AvatarService(HBox container, Consumer<Responsavel> aoSelecionar) {
        this.avatarContainer = container;
        this.responsavelController = new ResponsavelController();
        this.aoSelecionarCallback = aoSelecionar;
        
        // Configurar o container
        avatarContainer.setSpacing(15);
        avatarContainer.setAlignment(Pos.CENTER_LEFT);
    }
    
    /**
     * Carrega e exibe os avatares das pessoas cadastradas.
     */
    public void carregarAvatares() {
        try {
            ObservableList<Responsavel> responsaveis = responsavelController.listarTodosResponsaveis();
            
            if (responsaveis != null && !responsaveis.isEmpty()) {
                // Limpar o container antes de adicionar
                avatarContainer.getChildren().clear();
                
                // Criar um avatar para cada pessoa
                for (Responsavel responsavel : responsaveis) {
                    StackPane avatar = criarAvatar(responsavel);
                    avatarContainer.getChildren().add(avatar);
                }
                
                // Atualizar o estado visual dos avatares
                atualizarAvatares(responsavelSelecionadoId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cria um avatar para uma pessoa.
     */
    private StackPane criarAvatar(Responsavel responsavel) {
        StackPane avatarCircle = new StackPane();
        avatarCircle.setPrefSize(50, 50);
        avatarCircle.setId("avatar-" + responsavel.getId());
        
        // Usar imagem de perfil ou uma imagem padrão
        ImageView imageView = new ImageView();
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        
        // Aplicar clip circular
        Circle clip = new Circle(25, 25, 25);
        imageView.setClip(clip);
        
        // Tentar carregar a imagem do usuário
        String caminhoImagem = "images/profile_" + responsavel.getId() + ".png";
        File arquivo = new File(caminhoImagem);
        
        if (arquivo.exists()) {
            try {
                Image imagem = new Image(arquivo.toURI().toString());
                imageView.setImage(imagem);
                avatarCircle.getChildren().add(imageView);
            } catch (Exception e) {
                // Se falhar, usar avatar padrão com iniciais
                usarAvatarPadrao(avatarCircle, responsavel);
            }
        } else {
            // Se não existir imagem, usar avatar padrão com iniciais
            usarAvatarPadrao(avatarCircle, responsavel);
        }
        
        // Adicionar sombra
        DropShadow sombra = new DropShadow();
        sombra.setRadius(5.0);
        sombra.setOffsetX(0);
        sombra.setOffsetY(2.0);
        sombra.setColor(Color.rgb(0, 0, 0, 0.3));
        avatarCircle.setEffect(sombra);
        
        // Adicionar tooltip com o nome da pessoa
        Tooltip tooltip = new Tooltip(responsavel.getNome());
        Tooltip.install(avatarCircle, tooltip);
        
        // Adicionar evento de clique
        avatarCircle.setOnMouseClicked(e -> selecionarPessoa(responsavel));
        
        // Adicionar efeito hover
        avatarCircle.setOnMouseEntered(e -> {
            if (responsavelSelecionadoId == null || responsavelSelecionadoId != responsavel.getId()) {
                avatarCircle.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, #2980b9, 10, 0.3, 0, 0);");
            }
        });
        
        avatarCircle.setOnMouseExited(e -> {
            if (responsavelSelecionadoId == null || responsavelSelecionadoId != responsavel.getId()) {
                avatarCircle.setStyle("-fx-cursor: hand;");
            }
        });
        
        return avatarCircle;
    }
    
    /**
     * Usa um avatar padrão com as iniciais da pessoa.
     */
    private void usarAvatarPadrao(StackPane avatarCircle, Responsavel responsavel) {
        // Limpar avatar existente
        avatarCircle.getChildren().clear();
        
        // Gerar iniciais da pessoa
        String[] partes = responsavel.getNome().split(" ");
        String iniciais = "";
        
        if (partes.length > 0) {
            iniciais += partes[0].charAt(0);
            if (partes.length > 1) {
                iniciais += partes[partes.length - 1].charAt(0);
            }
        } else {
            iniciais = "??";
        }
        
        // Gerar cor baseada no hash do nome
        int hash = responsavel.getNome().hashCode();
        double hue = Math.abs(hash % 360);
        Color cor = Color.hsb(hue, 0.7, 0.8);
        
        // Criar círculo colorido
        Circle circulo = new Circle(25);
        circulo.setFill(cor);
        
        // Texto com as iniciais
        Label lblIniciais = new Label(iniciais.toUpperCase());
        lblIniciais.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblIniciais.setTextFill(Color.WHITE);
        
        avatarCircle.getChildren().addAll(circulo, lblIniciais);
    }
    
    /**
     * Seleciona uma pessoa e notifica o callback.
     */
    private void selecionarPessoa(Responsavel responsavel) {
        // Se já está selecionada, desseleciona
        if (responsavelSelecionadoId != null && responsavelSelecionadoId == responsavel.getId()) {
            setResponsavelSelecionadoId(null);
            if (aoSelecionarCallback != null) {
                aoSelecionarCallback.accept(null);
            }
        } else {
            // Seleciona a pessoa
            setResponsavelSelecionadoId(responsavel.getId());
            if (aoSelecionarCallback != null) {
                aoSelecionarCallback.accept(responsavel);
            }
        }
    }
    
    /**
     * Atualiza a aparência dos avatares conforme seleção.
     */
    public void atualizarAvatares(Integer responsavelSelecionadoId) {
        for (Node node : avatarContainer.getChildren()) {
            if (node instanceof StackPane) {
                StackPane avatarCircle = (StackPane) node;
                String id = avatarCircle.getId();
                
                if (id != null && id.startsWith("avatar-")) {
                    int avatarId = Integer.parseInt(id.substring(7)); // Extrair o ID do avatar
                    
                    if (responsavelSelecionadoId != null && avatarId == responsavelSelecionadoId) {
                        // Avatar selecionado
                        avatarCircle.setStyle(STYLE_AVATAR_SELECTED);
                    } else {
                        // Avatar não selecionado
                        avatarCircle.setStyle("");
                    }
                }
            }
        }
    }
    
    /**
     * Define o ID do responsável selecionado e atualiza os avatares.
     */
    public void setResponsavelSelecionadoId(Integer id) {
        this.responsavelSelecionadoId = id;
        atualizarAvatares(id);
    }
    
    /**
     * Retorna o ID do responsável selecionado atualmente.
     */
    public Integer getResponsavelSelecionadoId() {
        return responsavelSelecionadoId;
    }
    
    /**
     * Define o callback a ser chamado quando um avatar é selecionado.
     */
    public void setAoSelecionarCallback(Consumer<Responsavel> callback) {
        this.aoSelecionarCallback = callback;
    }
}