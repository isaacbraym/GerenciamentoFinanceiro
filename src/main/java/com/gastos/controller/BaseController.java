package com.gastos.controller;

import java.sql.SQLException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Classe base abstrata para os controladores do sistema.
 * Fornece métodos genéricos para operações de banco de dados e tratamento de erros.
 * 
 * @param <T> o tipo de entidade que o controlador gerencia
 */
public abstract class BaseController<T> {

    /**
     * Executa uma operação de banco de dados que retorna uma lista de objetos.
     * Converte o resultado em uma lista observável para uso com JavaFX.
     * 
     * @param <R> o tipo de objeto retornado pela operação
     * @param operacao a operação a ser executada
     * @return uma lista observável com os resultados da operação
     */
    protected <R> ObservableList<R> executarOperacaoLista(DAOOperation<R> operacao) {
        try {
            return FXCollections.observableArrayList(operacao.executar());
        } catch (SQLException e) {
            logErro(e);
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Executa uma operação de banco de dados que retorna um único objeto.
     * 
     * @param <R> o tipo de objeto retornado pela operação
     * @param operacao a operação a ser executada
     * @return o objeto retornado pela operação ou null em caso de erro
     */
    protected <R> R executarOperacaoUnico(DAOOperationUnico<R> operacao) {
        try {
            return operacao.executar();
        } catch (SQLException e) {
            logErro(e);
            return null;
        }
    }

    /**
     * Registra um erro de SQL no sistema.
     * 
     * @param e a exceção de SQL que ocorreu
     */
    protected void logErro(SQLException e) {
        System.err.println("Erro: " + e.getMessage());
        e.printStackTrace();
    }

    /**
     * Interface funcional para operações DAO que retornam uma lista de objetos.
     */
    @FunctionalInterface
    public interface DAOOperation<R> {
        /**
         * Executa a operação DAO.
         * 
         * @return uma lista com os resultados da operação
         * @throws SQLException se ocorrer um erro de SQL
         */
        List<R> executar() throws SQLException;
    }

    /**
     * Interface funcional para operações DAO que retornam um único objeto.
     */
    @FunctionalInterface
    public interface DAOOperationUnico<R> {
        /**
         * Executa a operação DAO.
         * 
         * @return o resultado da operação
         * @throws SQLException se ocorrer um erro de SQL
         */
        R executar() throws SQLException;
    }
}