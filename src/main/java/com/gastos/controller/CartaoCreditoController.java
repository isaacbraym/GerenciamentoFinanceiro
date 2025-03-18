package com.gastos.controller;

import com.gastos.db.CartaoCreditoDAO;
import com.gastos.model.CartaoCredito;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para gerenciar os cartões de crédito no sistema.
 */
public class CartaoCreditoController {
    private final CartaoCreditoDAO cartaoDAO;
    
    public CartaoCreditoController() {
        this.cartaoDAO = new CartaoCreditoDAO();
    }
    
    /**
     * Lista todos os cartões de crédito do sistema.
     * @return uma lista observável de cartões
     */
    public ObservableList<CartaoCredito> listarTodosCartoes() {
        try {
            List<CartaoCredito> cartoes = cartaoDAO.listarTodos();
            return FXCollections.observableArrayList(cartoes);
        } catch (SQLException e) {
            System.err.println("Erro ao listar cartões: " + e.getMessage());
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Busca um cartão pelo ID.
     * @param id o ID do cartão
     * @return o cartão encontrado ou null se não encontrar
     */
    public CartaoCredito buscarCartaoPorId(int id) {
        try {
            return cartaoDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cartão: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Salva um cartão no banco de dados.
     * @param cartao o cartão a ser salvo
     * @return true se a operação foi bem-sucedida
     */
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
            System.err.println("Erro ao salvar cartão: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui um cartão do sistema.
     * @param id o ID do cartão a ser excluído
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirCartao(int id) {
        try {
            cartaoDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir cartão: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calcula o total de gastos em um cartão específico para o mês atual.
     * @param cartaoId o ID do cartão
     * @return o valor total gasto no cartão no mês atual
     */
    public double calcularGastosNoMes(int cartaoId) {
        try {
            return cartaoDAO.calcularGastosNoMes(cartaoId);
        } catch (SQLException e) {
            System.err.println("Erro ao calcular gastos no cartão: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    /**
     * Calcula a porcentagem do limite utilizado em um cartão.
     * @param cartaoId o ID do cartão
     * @return a porcentagem do limite utilizado
     */
    public double calcularPorcentagemLimiteUtilizado(int cartaoId) {
        try {
            CartaoCredito cartao = cartaoDAO.buscarPorId(cartaoId);
            double gastos = cartaoDAO.calcularGastosNoMes(cartaoId);
            
            if (cartao != null && cartao.getLimite() > 0) {
                return (gastos / cartao.getLimite()) * 100;
            }
            
            return 0.0;
        } catch (SQLException e) {
            System.err.println("Erro ao calcular porcentagem do limite: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
}