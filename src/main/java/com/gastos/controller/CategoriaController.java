package com.gastos.controller;

import java.sql.SQLException;

import com.gastos.db.CategoriaDespesaDAO;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

/**
 * Controlador para gerenciar as categorias de despesas no sistema.
 */
public class CategoriaController extends BaseController<CategoriaDespesa> {

    private final CategoriaDespesaDAO categoriaDAO;

    /**
     * Construtor padrão.
     */
    public CategoriaController() {
        this.categoriaDAO = new CategoriaDespesaDAO();
    }

    /**
     * Lista todas as categorias do sistema.
     * @return uma lista observável de categorias
     */
    public ObservableList<CategoriaDespesa> listarTodasCategorias() {
        return executarOperacaoLista(categoriaDAO::listarTodas);
    }

    /**
     * Busca uma categoria pelo ID.
     * @param id o ID da categoria
     * @return a categoria encontrada ou null se não encontrar
     */
    public CategoriaDespesa buscarCategoriaPorId(int id) {
        return executarOperacaoUnico(() -> categoriaDAO.buscarPorId(id));
    }

    /**
     * Salva ou atualiza uma categoria no banco de dados.
     * @param categoria a categoria a ser salva
     * @return true se a operação foi bem-sucedida
     */
    public boolean salvarCategoria(CategoriaDespesa categoria) {
        try {
            if (categoria.getId() == 0) {
                int id = categoriaDAO.inserir(categoria);
                categoria.setId(id);
            } else {
                categoriaDAO.atualizar(categoria);
            }
            return true;
        } catch (SQLException e) {
            logErro(e);
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
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }

    /**
     * Adiciona uma subcategoria a uma categoria.
     * @param categoria a categoria pai
     * @param subcategoria a subcategoria a ser adicionada
     * @return true se a operação foi bem-sucedida
     */
    public boolean adicionarSubcategoria(CategoriaDespesa categoria, SubCategoria subcategoria) {
        try {
            subcategoria.setCategoriaId(categoria.getId());
            int id = categoriaDAO.inserirSubcategoria(subcategoria);
            subcategoria.setId(id);
            categoria.adicionarSubCategoria(subcategoria);
            return true;
        } catch (SQLException e) {
            logErro(e);
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
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }

    /**
     * Exclui uma subcategoria do sistema.
     * @param subcategoriaId o ID da subcategoria a ser excluída
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirSubcategoria(int subcategoriaId) {
        try {
            categoriaDAO.excluirSubcategoria(subcategoriaId);
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }
}