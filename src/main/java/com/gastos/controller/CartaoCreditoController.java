package com.gastos.controller;

import java.sql.SQLException;

import com.gastos.db.CartaoCreditoDAO;
import com.gastos.model.CartaoCredito;
import javafx.collections.ObservableList;

public class CartaoCreditoController extends BaseController<CartaoCredito> {
    
    private final CartaoCreditoDAO cartaoDAO;

    public CartaoCreditoController() {
        this.cartaoDAO = new CartaoCreditoDAO();
    }

    public ObservableList<CartaoCredito> listarTodosCartoes() {
        return executarOperacaoLista(cartaoDAO::listarTodos);
    }

    public CartaoCredito buscarCartaoPorId(int id) {
        return executarOperacaoUnico(() -> cartaoDAO.buscarPorId(id));
    }

    public boolean salvarCartao(CartaoCredito cartao) {
        try {
            if (cartao.getId() == 0) {
                int id = cartaoDAO.inserir(cartao);
                cartao.setId(id);
            } else {
                cartaoDAO.atualizar(cartao);
            }
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }

    public boolean excluirCartao(int id) {
        try {
            cartaoDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            logErro(e);
            return false;
        }
    }
}
