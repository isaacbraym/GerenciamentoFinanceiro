package com.gastos.controller;

import com.gastos.db.MeioPagamentoDAO;
import com.gastos.model.MeioPagamento;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para gerenciar os meios de pagamento no sistema.
 */
public class MeioPagamentoController {
    private final MeioPagamentoDAO meioPagamentoDAO;
    
    /**
     * Construtor padrão que inicializa o DAO.
     */
    public MeioPagamentoController() {
        this.meioPagamentoDAO = new MeioPagamentoDAO();
    }
    
    /**
     * Lista todos os meios de pagamento do sistema.
     * @return uma lista observável de meios de pagamento
     */
    public ObservableList<MeioPagamento> listarTodosMeiosPagamento() {
        try {
            List<MeioPagamento> meiosPagamento = meioPagamentoDAO.listarTodos();
            return FXCollections.observableArrayList(meiosPagamento);
        } catch (SQLException e) {
            registrarErro("listar meios de pagamento", e);
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Lista apenas os meios de pagamento do tipo cartão de crédito.
     * @return uma lista observável de meios de pagamento do tipo cartão
     */
    public ObservableList<MeioPagamento> listarCartoes() {
        try {
            List<MeioPagamento> meiosPagamento = meioPagamentoDAO.listarTodos();
            List<MeioPagamento> cartoes = meiosPagamento.stream()
                .filter(MeioPagamento::isCartaoCredito)
                .collect(Collectors.toList());
            
            return FXCollections.observableArrayList(cartoes);
        } catch (SQLException e) {
            registrarErro("listar cartões", e);
            return FXCollections.observableArrayList();
        }
    }
    
    /**
     * Busca um meio de pagamento pelo ID.
     * @param id o ID do meio de pagamento
     * @return o meio de pagamento encontrado ou null se não encontrar
     */
    public MeioPagamento buscarMeioPagamentoPorId(int id) {
        try {
            return meioPagamentoDAO.buscarPorId(id);
        } catch (SQLException e) {
            registrarErro("buscar meio de pagamento", e);
            return null;
        }
    }
    
    /**
     * Salva um meio de pagamento no banco de dados.
     * @param meioPagamento o meio de pagamento a ser salvo
     * @return true se a operação foi bem-sucedida
     */
    public boolean salvarMeioPagamento(MeioPagamento meioPagamento) {
        try {
            if (meioPagamento.getId() == 0) {
                int id = meioPagamentoDAO.inserir(meioPagamento);
                meioPagamento.setId(id);
            } else {
                meioPagamentoDAO.atualizar(meioPagamento);
            }
            return true;
        } catch (SQLException e) {
            registrarErro("salvar meio de pagamento", e);
            return false;
        }
    }
    
    /**
     * Exclui um meio de pagamento do sistema.
     * @param id o ID do meio de pagamento a ser excluído
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirMeioPagamento(int id) {
        try {
            meioPagamentoDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            registrarErro("excluir meio de pagamento", e);
            return false;
        }
    }
    
    /**
     * Registra um erro no sistema.
     * @param operacao a operação que falhou
     * @param e a exceção que ocorreu
     */
    private void registrarErro(String operacao, SQLException e) {
        System.err.println("Erro ao " + operacao + ": " + e.getMessage());
        e.printStackTrace();
    }
}