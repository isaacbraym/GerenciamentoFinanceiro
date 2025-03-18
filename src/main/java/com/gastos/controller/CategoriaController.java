package com.gastos.controller;

import com.gastos.db.CategoriaDespesaDAO;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para gerenciar as categorias de despesas no sistema.
 */
public class CategoriaController {
    private final CategoriaDespesaDAO categoriaDAO;
    
    public CategoriaController() {
        this.categoriaDAO = new CategoriaDespesaDAO();
    }
    
    /**
     * Lista todas as categorias do sistema.
     * @return uma lista observável de categorias
     */
    public ObservableList<CategoriaDespesa> listarTodasCategorias() {
        try {
            List<CategoriaDespesa> categorias = categoriaDAO.listarTodas();
            return FXCollections.observableArrayList(categorias);
        } catch (SQLException e) {
            System.err.println("Erro ao listar categorias: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Busca uma categoria pelo ID.
     * @param id o ID da categoria
     * @return a categoria encontrada ou null se não encontrar
     */
    public CategoriaDespesa buscarCategoriaPorId(int id) {
        try {
            return categoriaDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Salva uma categoria no banco de dados.
     * @param categoria a categoria a ser salva
     * @return true se a operação foi bem-sucedida
     */
    public boolean salvarCategoria(CategoriaDespesa categoria) {
        try {
            if (categoria.getId() == 0) {
                int id = categoriaDAO.inserir(categoria);
                categoria.setId(id);
                System.out.println("Categoria salva com sucesso. ID: " + id);
            } else {
                categoriaDAO.atualizar(categoria);
                System.out.println("Categoria atualizada com sucesso. ID: " + categoria.getId());
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar categoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui uma categoria do sistema.
     * @param id o ID da categoria a ser excluída
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirCategoria(int id) {
        try {
            categoriaDAO.excluir(id);
            System.out.println("Categoria excluída com sucesso. ID: " + id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir categoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Adiciona uma subcategoria a uma categoria.
     * @param categoria a categoria
     * @param subcategoria a subcategoria a ser adicionada
     * @return true se a operação foi bem-sucedida
     */
    public boolean adicionarSubcategoria(CategoriaDespesa categoria, SubCategoria subcategoria) {
        try {
            subcategoria.setCategoriaId(categoria.getId());
            int id = categoriaDAO.inserirSubcategoria(subcategoria);
            subcategoria.setId(id);
            
            // Atualizar o objeto na memória
            categoria.adicionarSubCategoria(subcategoria);
            System.out.println("Subcategoria adicionada com sucesso. ID: " + id);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar subcategoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui uma subcategoria.
     * @param subcategoriaId o ID da subcategoria a ser excluída
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirSubcategoria(int subcategoriaId) {
        try {
            categoriaDAO.excluirSubcategoria(subcategoriaId);
            System.out.println("Subcategoria excluída com sucesso. ID: " + subcategoriaId);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir subcategoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Atualiza uma subcategoria existente.
     * @param subcategoria a subcategoria a ser atualizada
     * @return true se a operação foi bem-sucedida
     */
    public boolean atualizarSubcategoria(SubCategoria subcategoria) {
        try {
            categoriaDAO.atualizarSubcategoria(subcategoria);
            System.out.println("Subcategoria atualizada com sucesso. ID: " + subcategoria.getId());
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar subcategoria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}