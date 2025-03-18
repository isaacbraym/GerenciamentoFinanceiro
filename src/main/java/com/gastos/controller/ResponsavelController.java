package com.gastos.controller;

import com.gastos.db.ResponsavelDAO;
import com.gastos.model.Responsavel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para gerenciar os responsáveis no sistema.
 */
public class ResponsavelController {
    private final ResponsavelDAO responsavelDAO;
    
    public ResponsavelController() {
        this.responsavelDAO = new ResponsavelDAO();
    }
    
    /**
     * Lista todos os responsáveis do sistema.
     * @return uma lista observável de responsáveis
     */
    public ObservableList<Responsavel> listarTodosResponsaveis() {
        try {
            List<Responsavel> responsaveis = responsavelDAO.listarTodos();
            return FXCollections.observableArrayList(responsaveis);
        } catch (SQLException e) {
            System.err.println("Erro ao listar responsáveis: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Busca um responsável pelo ID.
     * @param id o ID do responsável
     * @return o responsável encontrado ou null se não encontrar
     */
    public Responsavel buscarResponsavelPorId(int id) {
        try {
            return responsavelDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar responsável: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Salva um responsável no banco de dados.
     * @param responsavel o responsável a ser salvo
     * @return true se a operação foi bem-sucedida
     */
    public boolean salvarResponsavel(Responsavel responsavel) {
        try {
            if (responsavel.getId() == 0) {
                int id = responsavelDAO.inserir(responsavel);
                responsavel.setId(id);
            } else {
                responsavelDAO.atualizar(responsavel);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar responsável: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui um responsável do sistema.
     * @param id o ID do responsável a ser excluído
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirResponsavel(int id) {
        try {
            responsavelDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir responsável: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}