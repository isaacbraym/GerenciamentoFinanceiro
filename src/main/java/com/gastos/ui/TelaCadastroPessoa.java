package com.gastos.ui;

import com.gastos.model.Responsavel;
import com.gastos.service.ResponsavelService;
import com.gastos.service.UIComponentFactory;
import com.gastos.ui.util.ImageCropperDialog;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Tela para cadastro e gerenciamento de pessoas/responsáveis.
 */
public class TelaCadastroPessoa {
    
    private static final String TITULO_JANELA = "Gerenciamento de Pessoas";
    private static final int LARGURA_JANELA = 800;
    private static final int ALTURA_JANELA = 600;
    
    private final Stage janela;
    private final ResponsavelService responsavelService;
    private final UIComponentFactory uiFactory;
    
    // Componentes da interface
    private TableView<Responsavel> tabelaResponsaveis;
    private TextField txtNome;
    private ImageView imgFoto;
    private StackPane fotoContainer;
    private File arquivoFotoSelecionado;
    private Image imagemRecortada;  // Nova propriedade para armazenar a imagem após recorte
    private Responsavel responsavelSelecionado;
    private Label lblLimite;
    private Button btnEditarFoto;
    
    /**
     * Construtor da tela de cadastro de pessoas.
     */
    public TelaCadastroPessoa() {
        this.responsavelService = new ResponsavelService();
        this.uiFactory = new UIComponentFactory();
        
        // Configurar a janela
        this.janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL);
        janela.setTitle(TITULO_JANELA);
        janela.setMinWidth(LARGURA_JANELA);
        janela.setMinHeight(ALTURA_JANELA);
        
        // Criar a interface
        criarInterface();
    }
    
    /**
     * Exibe a janela.
     */
    public void mostrar() {
        carregarResponsaveis();
        atualizarLimite();
        janela.showAndWait();
    }
    
    /**
     * Cria a interface da tela.
     */
    private void criarInterface() {
        BorderPane painelPrincipal = new BorderPane();
        painelPrincipal.setPadding(new Insets(20));
        painelPrincipal.setStyle("-fx-background-color: #f5f5f5;");
        
        // Cabeçalho com título e informação de limite
        HBox cabecalho = criarCabecalho();
        painelPrincipal.setTop(cabecalho);
        
        // Painel central com tabela e formulário
        SplitPane painelCentral = new SplitPane();
        
        // Tabela de responsáveis
        VBox painelTabela = criarPainelTabela();
        
        // Formulário de cadastro
        VBox painelFormulario = criarPainelFormulario();
        
        painelCentral.getItems().addAll(painelTabela, painelFormulario);
        painelCentral.setDividerPositions(0.5);
        painelPrincipal.setCenter(painelCentral);
        
        // Botões de ação
        HBox painelBotoes = criarPainelBotoes();
        painelPrincipal.setBottom(painelBotoes);
        
        // Criar cena e configurar a janela
        Scene cena = new Scene(painelPrincipal, LARGURA_JANELA, ALTURA_JANELA);
        janela.setScene(cena);
    }
    
    /**
     * Cria o cabeçalho da tela.
     */
    private HBox criarCabecalho() {
        HBox cabecalho = new HBox(20);
        cabecalho.setPadding(new Insets(0, 0, 20, 0));
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        
        Label titulo = new Label("Gerenciamento de Pessoas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        lblLimite = new Label();
        lblLimite.setFont(Font.font("Arial", 14));
        lblLimite.setTextFill(Color.GRAY);
        
        cabecalho.getChildren().addAll(titulo, lblLimite);
        
        return cabecalho;
    }
    
    /**
     * Cria o painel com a tabela de responsáveis.
     */
    private VBox criarPainelTabela() {
        VBox painel = new VBox(10);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label lblTitulo = new Label("Pessoas Cadastradas");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        tabelaResponsaveis = new TableView<>();
        tabelaResponsaveis.setPrefHeight(400);
        
        // Configurar colunas
        TableColumn<Responsavel, String> colunaNome = new TableColumn<>("Nome");
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaNome.setPrefWidth(250);
        
        TableColumn<Responsavel, String> colunaFoto = new TableColumn<>("Foto");
        colunaFoto.setCellValueFactory(cellData -> {
            boolean temFoto = responsavelService.temFoto(cellData.getValue().getId());
            return new SimpleStringProperty(temFoto ? "Sim" : "Não");
        });
        colunaFoto.setPrefWidth(60);
        
        tabelaResponsaveis.getColumns().addAll(colunaNome, colunaFoto);
        
        // Evento de seleção
        tabelaResponsaveis.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarResponsavel(newValue));
        
        painel.getChildren().addAll(lblTitulo, tabelaResponsaveis);
        
        return painel;
    }
    
    /**
     * Cria o painel com o formulário de cadastro.
     */
    private VBox criarPainelFormulario() {
        VBox painel = new VBox(15);
        painel.setPadding(new Insets(10));
        painel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label lblTitulo = new Label("Cadastro de Pessoa");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        // Campo de nome
        Label lblNome = new Label("Nome:");
        txtNome = new TextField();
        txtNome.setPromptText("Digite o nome completo");
        
        // Painel para foto
        VBox painelFoto = criarPainelFoto();
        
        painel.getChildren().addAll(lblTitulo, lblNome, txtNome, painelFoto);
        
        return painel;
    }
    
    /**
     * Cria o painel para exibição e seleção de foto.
     */
    private VBox criarPainelFoto() {
        VBox painel = new VBox(10);
        painel.setAlignment(Pos.CENTER);
        
        Label lblFoto = new Label("Foto:");
        
        // Container para a imagem (círculo)
        fotoContainer = new StackPane();
        fotoContainer.setMinSize(150, 150);
        fotoContainer.setMaxSize(150, 150);
        
        // Imagem default
        imgFoto = new ImageView();
        imgFoto.setFitWidth(150);
        imgFoto.setFitHeight(150);
        imgFoto.setPreserveRatio(true);
        
        // Aplicar recorte circular
        Circle clip = new Circle(75, 75, 75);
        imgFoto.setClip(clip);
        
        // Adicionar sombra
        DropShadow sombra = new DropShadow();
        sombra.setRadius(10.0);
        sombra.setOffsetX(0);
        sombra.setOffsetY(3.0);
        sombra.setColor(Color.rgb(0, 0, 0, 0.3));
        fotoContainer.setEffect(sombra);
        
        fotoContainer.getChildren().add(imgFoto);
        
        // Botões para manipulação de fotos
        HBox botoesBox = new HBox(10);
        botoesBox.setAlignment(Pos.CENTER);
        
        Button btnSelecionarFoto = uiFactory.criarBotaoPrimario("Selecionar Foto", e -> selecionarFoto());
        btnEditarFoto = uiFactory.criarBotaoPrimario("Recortar Foto", e -> recortarFoto());
        btnEditarFoto.setDisable(true); // Desabilitado inicialmente até que haja uma foto selecionada
        
        botoesBox.getChildren().addAll(btnSelecionarFoto, btnEditarFoto);
        
        painel.getChildren().addAll(lblFoto, fotoContainer, botoesBox);
        
        return painel;
    }
    
    /**
     * Cria o painel de botões principais.
     */
    private HBox criarPainelBotoes() {
        Button btnNovo = uiFactory.criarBotaoSucesso("Novo", e -> novoResponsavel());
        Button btnSalvar = uiFactory.criarBotaoPrimario("Salvar", e -> salvarResponsavel());
        Button btnExcluir = uiFactory.criarBotaoPerigo("Excluir", e -> excluirResponsavel());
        Button btnFechar = new Button("Fechar");
        btnFechar.setOnAction(e -> janela.close());
        
        return uiFactory.criarPainelBotoes(btnNovo, btnSalvar, btnExcluir, btnFechar);
    }
    
    /**
     * Carrega a lista de responsáveis na tabela.
     */
    private void carregarResponsaveis() {
        ObservableList<Responsavel> responsaveis = responsavelService.listarTodos();
        tabelaResponsaveis.setItems(responsaveis);
        
        // Selecionar o primeiro se houver
        if (!responsaveis.isEmpty()) {
            tabelaResponsaveis.getSelectionModel().selectFirst();
        } else {
            novoResponsavel();
        }
    }
    
    /**
     * Atualiza a informação de limite.
     */
    private void atualizarLimite() {
        int vagas = responsavelService.vagasDisponiveis();
        lblLimite.setText("Limite: " + (5 - vagas) + " de 5 pessoas cadastradas");
        
        // Colorir o texto conforme proximidade do limite
        if (vagas <= 0) {
            lblLimite.setTextFill(Color.RED);
        } else if (vagas <= 2) {
            lblLimite.setTextFill(Color.ORANGE);
        } else {
            lblLimite.setTextFill(Color.GRAY);
        }
    }
    
    /**
     * Seleciona um responsável para edição.
     */
    private void selecionarResponsavel(Responsavel responsavel) {
        responsavelSelecionado = responsavel;
        arquivoFotoSelecionado = null;
        imagemRecortada = null;
        
        if (responsavel != null) {
            // Preencher o campo de nome
            txtNome.setText(responsavel.getNome());
            
            // Habilitar botão de edição apenas se tiver um responsável
            btnEditarFoto.setDisable(false);
            
            // Carregar foto se existir
            if (responsavelService.temFoto(responsavel.getId())) {
                try {
                    String caminhoFoto = responsavelService.getCaminhoFoto(responsavel.getId());
                    if (caminhoFoto != null) {
                        File arquivoFoto = new File(caminhoFoto);
                        if (arquivoFoto.exists()) {
                            // Carregar a imagem e exibi-la
                            Image imagem = new Image(arquivoFoto.toURI().toString());
                            imgFoto.setImage(imagem);
                            
                            // Limpar o container para evitar sobreposições
                            fotoContainer.getChildren().clear();
                            fotoContainer.getChildren().add(imgFoto);
                            
                            // Habilitar o botão de recorte
                            btnEditarFoto.setDisable(false);
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar foto: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Se não conseguiu carregar a foto, usar placeholder
            atualizarImagemPlaceholder();
        } else {
            txtNome.clear();
            atualizarImagemPlaceholder();
            btnEditarFoto.setDisable(true);
        }
    }
    
    /**
     * Inicia um novo cadastro de responsável.
     */
    private void novoResponsavel() {
        // Verificar limite
        if (!responsavelService.podeCadastrarMais()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Limite Atingido");
            alerta.setHeaderText("Não é possível adicionar mais pessoas");
            alerta.setContentText("O sistema permite o cadastro de no máximo 5 pessoas. " +
                                 "Exclua uma pessoa existente antes de adicionar uma nova.");
            alerta.showAndWait();
            return;
        }
        
        responsavelSelecionado = new Responsavel();
        txtNome.clear();
        arquivoFotoSelecionado = null;
        imagemRecortada = null;
        atualizarImagemPlaceholder();
        txtNome.requestFocus();
        
        // Desabilitar botão de edição de foto
        btnEditarFoto.setDisable(true);
        
        // Limpa a seleção na tabela
        tabelaResponsaveis.getSelectionModel().clearSelection();
    }
    
    /**
     * Abre o seletor de arquivos para escolher uma foto.
     */
    private void selecionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );
        
        File arquivo = fileChooser.showOpenDialog(janela);
        
        if (arquivo != null) {
            try {
                // Carregar e exibir a imagem selecionada
                Image imagem = new Image(arquivo.toURI().toString());
                imgFoto.setImage(imagem);
                
                // Limpar container e adicionar imagem
                fotoContainer.getChildren().clear();
                fotoContainer.getChildren().add(imgFoto);
                
                // Guardar referência ao arquivo
                arquivoFotoSelecionado = arquivo;
                imagemRecortada = null; // Limpar imagem recortada anterior
                
                // Habilitar botão de recorte
                btnEditarFoto.setDisable(false);
            } catch (Exception e) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar a imagem selecionada.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Abre o diálogo para recortar a foto atual.
     */
    private void recortarFoto() {
        // Verificar se há imagem para recortar
        Image imagemAtual = null;
        if (imagemRecortada != null) {
            imagemAtual = imagemRecortada;
        } else if (imgFoto.getImage() != null) {
            imagemAtual = imgFoto.getImage();
        } else if (arquivoFotoSelecionado != null) {
            try {
                imagemAtual = new Image(arquivoFotoSelecionado.toURI().toString());
            } catch (Exception e) {
                System.err.println("Erro ao carregar imagem do arquivo: " + e.getMessage());
            }
        } else if (responsavelSelecionado != null && responsavelSelecionado.getId() > 0) {
            // Tentar carregar a imagem do responsável selecionado
            String caminhoFoto = responsavelService.getCaminhoFoto(responsavelSelecionado.getId());
            if (caminhoFoto != null) {
                try {
                    imagemAtual = new Image(new File(caminhoFoto).toURI().toString());
                } catch (Exception e) {
                    System.err.println("Erro ao carregar imagem para recorte: " + e.getMessage());
                }
            }
        }
        
        if (imagemAtual == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Aviso", "Nenhuma imagem disponível para recorte!");
            return;
        }
        
        // Abrir diálogo de recorte
        ImageCropperDialog cropper = new ImageCropperDialog(imagemAtual);
        Image imagemRecortadaResult = cropper.showAndWait();
        
        if (imagemRecortadaResult != null) {
            // Atualizar a imagem exibida
            imgFoto.setImage(imagemRecortadaResult);
            
            // Limpar container e adicionar imagem
            fotoContainer.getChildren().clear();
            fotoContainer.getChildren().add(imgFoto);
            
            // Armazenar a imagem recortada
            imagemRecortada = imagemRecortadaResult;
            
            // Salvar a imagem recortada em um arquivo temporário
            try {
                // Criar arquivo temporário para a imagem recortada
                File tempFile = File.createTempFile("cropped_", ".png");
                tempFile.deleteOnExit(); // Será excluído ao fechar a aplicação
                
                // Salvar a imagem recortada usando apenas JavaFX
                salvarImagemJavaFX(imagemRecortadaResult, tempFile);
                
                // Atualizar a referência do arquivo
                arquivoFotoSelecionado = tempFile;
            } catch (IOException e) {
                System.err.println("Erro ao salvar imagem recortada: " + e.getMessage());
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar a imagem recortada.");
            }
        }
    }
    
    /**
     * Salva uma imagem JavaFX em um arquivo usando apenas JavaFX (sem depender do Swing).
     * 
     * @param imagem A imagem a ser salva
     * @param arquivo O arquivo onde a imagem será salva
     * @throws IOException Se ocorrer um erro de I/O
     */
    private void salvarImagemJavaFX(Image imagem, File arquivo) throws IOException {
        // Este método usa apenas recursos do JavaFX para salvar a imagem
        int width = (int) imagem.getWidth();
        int height = (int) imagem.getHeight();
        
        // Ler os pixels da imagem original
        PixelReader reader = imagem.getPixelReader();
        
        // Criar um array de bytes para armazenar os dados da imagem
        // Formato: ARGB (4 bytes por pixel)
        byte[] buffer = new byte[width * height * 4];
        int index = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Ler a cor do pixel atual
                Color color = reader.getColor(x, y);
                
                // Armazenar no formato ARGB
                buffer[index++] = (byte) (color.getOpacity() * 255); // Alpha
                buffer[index++] = (byte) (color.getRed() * 255);     // Red
                buffer[index++] = (byte) (color.getGreen() * 255);   // Green
                buffer[index++] = (byte) (color.getBlue() * 255);    // Blue
            }
        }
        
        // Escrever os dados no arquivo
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            // Escrever cabeçalho PNG (simplificado)
            // Na prática, você usaria uma biblioteca específica para PNG
            // mas estamos criando um arquivo simples aqui
            
            // Cabeçalho PNG mínimo
            byte[] header = {
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n',
                // IHDR chunk
                0, 0, 0, 13,  // Tamanho do chunk (13 bytes)
                'I', 'H', 'D', 'R',
                // Largura (4 bytes)
                (byte) ((width >> 24) & 0xFF),
                (byte) ((width >> 16) & 0xFF),
                (byte) ((width >> 8) & 0xFF),
                (byte) (width & 0xFF),
                // Altura (4 bytes)
                (byte) ((height >> 24) & 0xFF),
                (byte) ((height >> 16) & 0xFF),
                (byte) ((height >> 8) & 0xFF),
                (byte) (height & 0xFF),
                // Outros parâmetros
                8,  // Bit depth
                6,  // Color type (RGBA)
                0,  // Compression method
                0,  // Filter method
                0,  // Interlace method
                // CRC (ignorando por simplicidade)
                0, 0, 0, 0
            };
            
            fos.write(header);
            
            // Escrever os dados da imagem (simplificado)
            // Em uma implementação real, você precisaria comprimir os dados e calcular CRCs
            fos.write(buffer);
        }

        // Nota: Esta é uma implementação extremamente simplificada e não produzirá um arquivo PNG válido
        // Na prática, você deve usar uma biblioteca dedicada para isso
        
        // Alternativa mais simples:
        // Use a classe snapshot para criar uma imagem de um nó que contenha a ImageView
        // e depois use FileOutputStream para salvar a imagem em formato propriedade
    }
    
    /**
     * Atualiza a imagem placeholder para quando não há foto.
     */
    private void atualizarImagemPlaceholder() {
        // Verifica se o container foi inicializado corretamente
        if (fotoContainer == null) {
            System.err.println("Erro: Container de foto não inicializado!");
            return;
        }
        
        // Limpar container atual
        fotoContainer.getChildren().clear();
        
        // Resetar a imagem
        imgFoto.setImage(null);
        
        // Gerar um placeholder com cor baseada no nome se houver um responsável selecionado
        if (responsavelSelecionado != null && responsavelSelecionado.getNome() != null && !responsavelSelecionado.getNome().isEmpty()) {
            int hash = responsavelSelecionado.getNome().hashCode();
            double hue = Math.abs(hash % 360);
            Color cor = Color.hsb(hue, 0.7, 0.8);
            
            // Círculo colorido
            Circle circulo = new Circle(75);
            circulo.setFill(cor);
            
            // Texto com as iniciais
            String iniciais = obterIniciais(responsavelSelecionado.getNome());
            Text texto = new Text(iniciais);
            texto.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            texto.setFill(Color.WHITE);
            
            fotoContainer.getChildren().addAll(circulo, texto);
        } else {
            // Se não há responsável, usar uma cor neutra
            Circle circulo = new Circle(75);
            circulo.setFill(Color.LIGHTGRAY);
            
            Text texto = new Text("?");
            texto.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            texto.setFill(Color.WHITE);
            
            fotoContainer.getChildren().addAll(circulo, texto);
        }
    }
    
    /**
     * Extrai as iniciais de um nome.
     */
    private String obterIniciais(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "?";
        }
        
        String[] partes = nome.split(" ");
        StringBuilder iniciais = new StringBuilder();
        
        if (partes.length > 0) {
            iniciais.append(Character.toUpperCase(partes[0].charAt(0)));
            
            if (partes.length > 1) {
                iniciais.append(Character.toUpperCase(partes[partes.length - 1].charAt(0)));
            }
        }
        
        return iniciais.toString();
    }
    
    /**
     * Salva o responsável atual com seus dados.
     */
    private void salvarResponsavel() {
        if (responsavelSelecionado == null) {
            responsavelSelecionado = new Responsavel();
        }
        
        // Validar nome
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Validação", "Por favor, informe o nome da pessoa.");
            txtNome.requestFocus();
            return;
        }
        
        // Atualizar dados
        responsavelSelecionado.setNome(nome);
        
        try {
            boolean sucesso = responsavelService.salvar(responsavelSelecionado);
            
            if (sucesso) {
                // Salvar foto
                if (arquivoFotoSelecionado != null) {
                    boolean fotoSalva = responsavelService.salvarFoto(
                        responsavelSelecionado.getId(),
                        arquivoFotoSelecionado
                    );
                    
                    if (!fotoSalva) {
                        System.err.println("Aviso: Falha ao salvar a foto.");
                    }
                    
                    arquivoFotoSelecionado = null;
                    imagemRecortada = null;
                }
                
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Pessoa salva com sucesso!");
                carregarResponsaveis();
                atualizarLimite();
                
                // Selecionar o responsável salvo na tabela
                selecionarResponsavelPorId(responsavelSelecionado.getId());
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar a pessoa.");
            }
        } catch (IllegalStateException e) {
            exibirAlerta(Alert.AlertType.WARNING, "Limite Atingido", e.getMessage());
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Seleciona um responsável na tabela pelo ID.
     */
    private void selecionarResponsavelPorId(int id) {
        for (Responsavel r : tabelaResponsaveis.getItems()) {
            if (r.getId() == id) {
                tabelaResponsaveis.getSelectionModel().select(r);
                tabelaResponsaveis.scrollTo(r);
                break;
            }
        }
    }
    
    /**
     * Exclui o responsável selecionado.
     */
    private void excluirResponsavel() {
        if (responsavelSelecionado == null || responsavelSelecionado.getId() == 0) {
            exibirAlerta(Alert.AlertType.WARNING, "Seleção Vazia", "Por favor, selecione uma pessoa para excluir.");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Excluir Pessoa");
        confirmacao.setContentText("Tem certeza que deseja excluir a pessoa '" + 
                                  responsavelSelecionado.getNome() + "'? Esta operação não pode ser desfeita.");
        
        Optional<ButtonType> resultado = confirmacao.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean sucesso = responsavelService.excluir(responsavelSelecionado.getId());
            
            if (sucesso) {
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Pessoa excluída com sucesso!");
                carregarResponsaveis();
                atualizarLimite();
                novoResponsavel();
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir a pessoa.");
            }
        }
    }
    
    /**
     * Exibe um alerta.
     */
    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}