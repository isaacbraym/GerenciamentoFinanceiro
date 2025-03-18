package com.gastos.controller;

import java.sql.SQLException;

import com.gastos.db.CategoriaDespesaDAO;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.SubCategoria;
import javafx.collections.ObservableList;

/**
 * Controlador para gerenciar as categorias de despesas no sistema.
 */
public class CategoriaController extends BaseController<CategoriaDespesa> {

    private final CategoriaDespesaDAO categoriaDAO;

    public CategoriaController() {
        this.categoriaDAO = new CategoriaDespesaDAO();
    }

    // Método para listar todas as categorias
    public ObservableList<CategoriaDespesa> listarTodasCategorias() {
        return executarOperacaoLista(categoriaDAO::listarTodas);
    }

    // Método para buscar categoria pelo ID
    public CategoriaDespesa buscarCategoriaPorId(int id) {
        return executarOperacaoUnico(() -> categoriaDAO.buscarPorId(id));
    }

    // Método para salvar ou atualizar categoria
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

    // Excluir uma categoria
    public boolean excluirCategoria(int id) {
        try {
            categoriaDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }

    // Adicionar uma subcategoria a uma categoria
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

    // Atualizar uma subcategoria
    public boolean atualizarSubcategoria(SubCategoria subcategoria) {
        try {
            categoriaDAO.atualizarSubcategoria(subcategoria);
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }

    // Excluir uma subcategoria
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
