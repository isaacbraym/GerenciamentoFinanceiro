package com.gastos.ui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Diálogo para recortar imagens.
 */
public class ImageCropperDialog {
    private final Stage stage;
    private final Image originalImage;
    private ImageView imageView;
    
    // Propriedades para a seleção da área de recorte
    private final DoubleProperty startX = new SimpleDoubleProperty();
    private final DoubleProperty startY = new SimpleDoubleProperty();
    private final DoubleProperty endX = new SimpleDoubleProperty();
    private final DoubleProperty endY = new SimpleDoubleProperty();
    
    private Rectangle selectionRect;
    private Image croppedImage = null;
    
    /**
     * Construtor do diálogo de recorte de imagem.
     * 
     * @param originalImage Imagem original a ser recortada
     */
    public ImageCropperDialog(Image originalImage) {
        this.originalImage = originalImage;
        
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Recortar Imagem");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        createUI();
    }
    
    /**
     * Cria a interface do diálogo.
     */
    private void createUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        
        // Título
        Label title = new Label("Selecione a área de recorte");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Container da imagem
        Pane imageContainer = new Pane();
        imageView = new ImageView(originalImage);
        
        // Ajustar tamanho da imagem para caber na tela
        fitImageToScreen();
        
        // Criar o retângulo de seleção
        selectionRect = new Rectangle();
        selectionRect.setFill(Color.TRANSPARENT);
        selectionRect.setStroke(Color.DODGERBLUE);
        selectionRect.setStrokeWidth(2);
        selectionRect.getStrokeDashArray().addAll(5.0, 5.0);
        
        // Vincular posição e tamanho do retângulo às propriedades
        selectionRect.xProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(startX.get(), endX.get()), startX, endX));
        selectionRect.yProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(startY.get(), endY.get()), startY, endY));
        selectionRect.widthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.abs(endX.get() - startX.get()), startX, endX));
        selectionRect.heightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.abs(endY.get() - startY.get()), startY, endY));
        
        // Adicionar componentes ao container
        imageContainer.getChildren().addAll(imageView, selectionRect);
        
        // Configurar eventos do mouse
        setupMouseEvents(imageContainer);
        
        // Área de instruções
        Label instructions = new Label("Clique e arraste para selecionar a área de recorte.\n" +
                                       "Após selecionar, clique em Recortar para concluir.");
        instructions.setStyle("-fx-font-style: italic;");
        
        // Botões de ação
        Button btnCrop = new Button("Recortar");
        btnCrop.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnCrop.setOnAction(e -> performCrop());
        
        Button btnCancel = new Button("Cancelar");
        btnCancel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnCancel.setOnAction(e -> stage.close());
        
        HBox buttonsBox = new HBox(10, btnCrop, btnCancel);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Organizar componentes no layout
        VBox topBox = new VBox(10, title, instructions);
        
        root.setTop(topBox);
        root.setCenter(imageContainer);
        root.setBottom(buttonsBox);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    /**
     * Ajusta o tamanho da imagem para caber na tela.
     */
    private void fitImageToScreen() {
        double maxWidth = 700; // Máxima largura que a imagem pode ter
        double maxHeight = 500; // Máxima altura que a imagem pode ter
        
        double width = originalImage.getWidth();
        double height = originalImage.getHeight();
        
        double scale = 1.0;
        
        // Calcular escala necessária para caber na tela
        if (width > maxWidth || height > maxHeight) {
            double scaleX = maxWidth / width;
            double scaleY = maxHeight / height;
            scale = Math.min(scaleX, scaleY);
        }
        
        // Aplicar escala
        imageView.setFitWidth(width * scale);
        imageView.setFitHeight(height * scale);
        imageView.setPreserveRatio(true);
    }
    
    /**
     * Configura os eventos do mouse para a seleção da área de recorte.
     */
    private void setupMouseEvents(Pane imageContainer) {
        imageContainer.setOnMousePressed(event -> handleMousePressed(event));
        imageContainer.setOnMouseDragged(event -> handleMouseDragged(event));
        imageContainer.setOnMouseReleased(event -> handleMouseReleased(event));
    }
    
    private void handleMousePressed(MouseEvent event) {
        startX.set(event.getX());
        startY.set(event.getY());
        endX.set(event.getX());
        endY.set(event.getY());
    }
    
    private void handleMouseDragged(MouseEvent event) {
        endX.set(event.getX());
        endY.set(event.getY());
    }
    
    private void handleMouseReleased(MouseEvent event) {
        // Garantir que temos uma região de seleção válida
        if (Math.abs(endX.get() - startX.get()) < 5 || 
            Math.abs(endY.get() - startY.get()) < 5) {
            // Seleção muito pequena - limpar
            startX.set(0);
            startY.set(0);
            endX.set(0);
            endY.set(0);
        }
    }
    
    /**
     * Realiza o recorte da imagem com base na área selecionada.
     */
    private void performCrop() {
        // Verificar se há uma área selecionada
        if (selectionRect.getWidth() <= 0 || selectionRect.getHeight() <= 0) {
            croppedImage = originalImage; // Usar a imagem original se não houver seleção
            stage.close();
            return;
        }
        
        // Calcular coordenadas reais na imagem original
        double imageViewWidth = imageView.getFitWidth();
        double imageViewHeight = imageView.getFitHeight();
        
        double imageWidth = originalImage.getWidth();
        double imageHeight = originalImage.getHeight();
        
        // Calcular fator de escala
        double scaleX = imageWidth / imageViewWidth;
        double scaleY = imageHeight / imageViewHeight;
        
        // Calcular coordenadas na imagem original
        int x = (int) (Math.min(startX.get(), endX.get()) * scaleX);
        int y = (int) (Math.min(startY.get(), endY.get()) * scaleY);
        int width = (int) (Math.abs(endX.get() - startX.get()) * scaleX);
        int height = (int) (Math.abs(endY.get() - startY.get()) * scaleY);
        
        // Garantir que as coordenadas estão dentro dos limites da imagem
        x = Math.max(0, Math.min(x, (int) imageWidth - 1));
        y = Math.max(0, Math.min(y, (int) imageHeight - 1));
        width = Math.min(width, (int) imageWidth - x);
        height = Math.min(height, (int) imageHeight - y);
        
        // Realizar o recorte
        PixelReader reader = originalImage.getPixelReader();
        WritableImage croppedImg = new WritableImage(reader, x, y, width, height);
        croppedImage = croppedImg;
        
        // Fechar o diálogo
        stage.close();
    }
    
    /**
     * Exibe o diálogo e aguarda o recorte.
     * 
     * @return A imagem recortada ou null se o usuário cancelar
     */
    public Image showAndWait() {
        stage.showAndWait();
        return croppedImage;
    }
}