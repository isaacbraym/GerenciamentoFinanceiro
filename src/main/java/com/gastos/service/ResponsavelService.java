package com.gastos.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.gastos.controller.ResponsavelController;
import com.gastos.model.Responsavel;

import javafx.collections.ObservableList;

/**
 * Serviço para gerenciamento de responsáveis/pessoas.
 */
public class ResponsavelService {
    
    private static final int LIMITE_RESPONSAVEIS = 5;
    private static final String PASTA_IMAGENS = "images";
    
    private final ResponsavelController responsavelController;
    
    /**
     * Construtor padrão.
     */
    public ResponsavelService() {
        this.responsavelController = new ResponsavelController();
        inicializarPastaImagens();
    }
    
    /**
     * Inicializa a pasta de imagens se não existir.
     */
    private void inicializarPastaImagens() {
        File pasta = new File(PASTA_IMAGENS);
        if (!pasta.exists()) {
            boolean criada = pasta.mkdirs();
            if (!criada) {
                System.err.println("Não foi possível criar a pasta de imagens: " + PASTA_IMAGENS);
            } else {
                System.out.println("Pasta de imagens criada: " + PASTA_IMAGENS);
            }
        }
    }
    
    /**
     * Lista todos os responsáveis cadastrados.
     * 
     * @return Lista de responsáveis
     */
    public ObservableList<Responsavel> listarTodos() {
        return responsavelController.listarTodosResponsaveis();
    }
    
    /**
     * Verifica se é possível adicionar mais responsáveis.
     * 
     * @return true se o limite não foi atingido
     */
    public boolean podeCadastrarMais() {
        ObservableList<Responsavel> responsaveis = listarTodos();
        return responsaveis.size() < LIMITE_RESPONSAVEIS;
    }
    
    /**
     * Retorna quantos responsáveis ainda podem ser adicionados.
     * 
     * @return número de vagas disponíveis
     */
    public int vagasDisponiveis() {
        ObservableList<Responsavel> responsaveis = listarTodos();
        return LIMITE_RESPONSAVEIS - responsaveis.size();
    }
    
    /**
     * Salva um responsável.
     * 
     * @param responsavel Responsável a ser salvo
     * @return true se salvou com sucesso
     */
    public boolean salvar(Responsavel responsavel) {
        // Verificar limite de cadastros (apenas para novos)
        if (responsavel.getId() == 0 && !podeCadastrarMais()) {
            throw new IllegalStateException("Limite de " + LIMITE_RESPONSAVEIS + " responsáveis atingido");
        }
        
        return responsavelController.salvarResponsavel(responsavel);
    }
    
    /**
     * Exclui um responsável.
     * 
     * @param id ID do responsável
     * @return true se excluiu com sucesso
     */
    public boolean excluir(int id) {
        // Exclui a foto se existir
        excluirFoto(id);
        return responsavelController.excluirResponsavel(id);
    }
    
    /**
     * Busca um responsável pelo ID.
     * 
     * @param id ID do responsável
     * @return Responsável encontrado ou null
     */
    public Responsavel buscarPorId(int id) {
        return responsavelController.buscarResponsavelPorId(id);
    }
    
    /**
     * Salva a foto do responsável.
     * 
     * @param responsavelId ID do responsável
     * @param arquivoFoto Arquivo de foto selecionado
     * @return true se a foto foi salva com sucesso
     */
    public boolean salvarFoto(int responsavelId, File arquivoFoto) {
        if (arquivoFoto == null || !arquivoFoto.exists()) {
            System.err.println("Arquivo de foto inválido ou inexistente");
            return false;
        }
        
        try {
            // Determinar o caminho e nome do arquivo
            String nomeArquivo = "profile_" + responsavelId + ".png";
            Path destino = Paths.get(PASTA_IMAGENS, nomeArquivo);
            
            // Garantir que a pasta de imagens exista
            inicializarPastaImagens();
            
            // Copiar o arquivo para a pasta de imagens
            Files.copy(arquivoFoto.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Foto salva com sucesso em: " + destino.toAbsolutePath());
            
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao salvar foto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui a foto de um responsável.
     * 
     * @param responsavelId ID do responsável
     * @return true se a foto foi excluída ou não existia
     */
    public boolean excluirFoto(int responsavelId) {
        String nomeArquivo = "profile_" + responsavelId + ".png";
        Path caminhoFoto = Paths.get(PASTA_IMAGENS, nomeArquivo);
        
        try {
            if (Files.exists(caminhoFoto)) {
                Files.delete(caminhoFoto);
                System.out.println("Foto excluída: " + caminhoFoto);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erro ao excluir foto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Verifica se um responsável tem foto.
     * 
     * @param responsavelId ID do responsável
     * @return true se o responsável tem foto
     */
    public boolean temFoto(int responsavelId) {
        if (responsavelId <= 0) {
            return false;
        }
        
        String nomeArquivo = "profile_" + responsavelId + ".png";
        Path caminhoFoto = Paths.get(PASTA_IMAGENS, nomeArquivo);
        return Files.exists(caminhoFoto);
    }
    
    /**
     * Retorna o caminho para a foto de um responsável.
     * 
     * @param responsavelId ID do responsável
     * @return caminho para a foto ou null se não existir
     */
    public String getCaminhoFoto(int responsavelId) {
        if (responsavelId <= 0) {
            return null;
        }
        
        String nomeArquivo = "profile_" + responsavelId + ".png";
        Path caminhoFoto = Paths.get(PASTA_IMAGENS, nomeArquivo);
        
        if (Files.exists(caminhoFoto)) {
            return caminhoFoto.toAbsolutePath().toString();
        }
        return null;
    }
}