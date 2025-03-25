package com.gastos.ui;

import com.gastos.controller.ResponsavelController;
import com.gastos.model.Responsavel;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.function.Consumer;

/**
 * Classe responsável por gerenciar os avatares de pessoas na interface.
 */
public class GerenciadorAvatares {
    
    // Constantes para estilo
    private static final String STYLE_AVATAR_SELECTED = "-fx-effect: dropshadow(three-pass-box, #3498db, 10, 0.5, 0, 0);";
    
    private final FlowPane pessoasContainer;
    private final ResponsavelController responsavelController;
    private Integer responsavelSelecionadoId = null;
    private Consumer<Responsavel> aoSelecionarCallback;
    
    /**
     * Construtor que inicializa o gerenciador de avatares.
     * @param flowPane O container onde os avatares serão exibidos
     * @param aoSelecionar Callback executado quando um avatar é selecionado
     */
    public GerenciadorAvatares(FlowPane flowPane, Consumer<Responsavel> aoSelecionar) {
        this.pessoasContainer = flowPane;
        this.responsavelController = new ResponsavelController();
        this.aoSelecionarCallback = aoSelecionar;
        
        // Configurar o container
        pessoasContainer.setHgap(20);
        pessoasContainer.setVgap(15);
        pessoasContainer.setPrefWrapLength(900);
        pessoasContainer.setAlignment(Pos.CENTER_LEFT);
    }
    
    /**
     * Carrega e exibe os avatares das pessoas cadastradas.
     */
    public void carregarPessoas() {
        try {
            ObservableList<Responsavel> responsaveis = responsavelController.listarTodosResponsaveis();
            
            if (responsaveis != null && !responsaveis.isEmpty()) {
                // Limpar o container antes de adicionar
                pessoasContainer.getChildren().clear();
                
                for (Responsavel responsavel : responsaveis) {
                    VBox avatarBox = criarAvatarPessoa(responsavel);
                    pessoasContainer.getChildren().add(avatarBox);
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
    private VBox criarAvatarPessoa(Responsavel responsavel) {
        VBox avatarBox = new VBox(5);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(5));
        avatarBox.setId("avatar-" + responsavel.getId());
        
        // Criar círculo para a imagem
        StackPane avatarCircle = new StackPane();
        avatarCircle.setPrefSize(70, 70);
        
        // Usar imagem de perfil ou uma imagem padrão
        ImageView imageView = criarImagemPerfil(responsavel);
        
        // Aplicar clip circular
        Circle clip = new Circle(35, 35, 35);
        imageView.setClip(clip);
        
        // Adicionar sombra
        DropShadow sombra = new DropShadow();
        sombra.setRadius(5.0);
        sombra.setOffsetX(0);
        sombra.setOffsetY(2.0);
        sombra.setColor(Color.rgb(0, 0, 0, 0.3));
        avatarCircle.setEffect(sombra);
        
        avatarCircle.getChildren().add(imageView);
        
        // Adicionar nome da pessoa
        Label lblNome = new Label(responsavel.getNome());
        lblNome.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        lblNome.setTextAlignment(TextAlignment.CENTER);
        lblNome.setWrapText(true);
        lblNome.setMaxWidth(80);
        
        avatarBox.getChildren().addAll(avatarCircle, lblNome);
        
        // Adicionar evento de clique
        avatarBox.setOnMouseClicked(e -> selecionarPessoa(responsavel));
        
        // Adicionar efeito hover
        avatarBox.setOnMouseEntered(e -> {
            if (responsavelSelecionadoId == null || responsavelSelecionadoId != responsavel.getId()) {
                avatarCircle.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, #2980b9, 10, 0.3, 0, 0);");
            }
        });
        
        avatarBox.setOnMouseExited(e -> {
            if (responsavelSelecionadoId == null || responsavelSelecionadoId != responsavel.getId()) {
                avatarCircle.setStyle("-fx-cursor: hand;");
            }
        });
        
        return avatarBox;
    }
    
    /**
     * Cria uma imagem de perfil para a pessoa.
     */
    private ImageView criarImagemPerfil(Responsavel responsavel) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(70);
        imageView.setFitHeight(70);
        
        // Tenta carregar a imagem do usuário se existir
        String caminhoImagem = "images/profile_" + responsavel.getId() + ".png";
        File arquivo = new File(caminhoImagem);
        
        if (arquivo.exists()) {
            try {
                Image imagem = new Image(arquivo.toURI().toString());
                imageView.setImage(imagem);
            } catch (Exception e) {
                // Se falhar, usa imagem padrão
                usarImagemPadrao(imageView, responsavel);
            }
        } else {
            // Se não existir, usa imagem padrão
            usarImagemPadrao(imageView, responsavel);
        }
        
        return imageView;
    }
    
    /**
     * Usa uma imagem padrão baseada nas iniciais da pessoa.
     */
    private void usarImagemPadrao(ImageView imageView, Responsavel responsavel) {
        // Criar um StackPane para a imagem padrão (círculo colorido com iniciais)
        StackPane avatarPadrao = new StackPane();
        avatarPadrao.setPrefSize(70, 70);
        
        // Definir uma cor baseada no nome da pessoa
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
        Circle circulo = new Circle(35);
        circulo.setFill(cor);
        
        // Texto com as iniciais
        Label lblIniciais = new Label(iniciais.toUpperCase());
        lblIniciais.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblIniciais.setTextFill(Color.WHITE);
        
        avatarPadrao.getChildren().addAll(circulo, lblIniciais);
        
        // Corrigido: Usar a cor de fundo no ImageView
        String hexColor = formatHexColor(cor);
        imageView.setStyle("-fx-background-color: " + hexColor + ";");
        
        // Criar uma imagem com as iniciais
        Image placeholderImage = createColoredImage(cor);
        imageView.setImage(placeholderImage);
        
        // Adicionar as iniciais como uma camada sobre a ImageView
        StackPane parent = (StackPane) imageView.getParent();
        if (parent != null) {
            Label textLabel = new Label(iniciais.toUpperCase());
            textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            textLabel.setTextFill(Color.WHITE);
            parent.getChildren().add(textLabel);
        }
    }
    
    /**
     * Cria uma imagem de cor sólida para o placeholder.
     */
    private Image createColoredImage(Color color) {
        return null; // Na prática, retornaria uma imagem sólida com a cor especificada
                     // Como estamos usando o style para definir a cor de fundo, podemos deixar o image como null
    }
    
    /**
     * Formata uma cor para o formato hexadecimal.
     */
    private String formatHexColor(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
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
    private void atualizarAvatares(Integer responsavelSelecionadoId) {
        for (Node node : pessoasContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox avatarBox = (VBox) node;
                String id = avatarBox.getId();
                
                if (id != null && id.startsWith("avatar-")) {
                    int avatarId = Integer.parseInt(id.substring(7)); // Extrair o ID do avatar
                    StackPane avatarCircle = (StackPane) avatarBox.getChildren().get(0);
                    
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