package com.gastos.controller;

import java.sql.SQLException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class BaseController<T> {

    // Método genérico para executar operações de banco e retornar lista de objetos
    protected <R> ObservableList<R> executarOperacaoLista(DAOOperation<R> operacao) {
        try {
            return FXCollections.observableArrayList(operacao.executar());
        } catch (SQLException e) {
            logErro(e);
            return FXCollections.observableArrayList();
        }
    }

    // Método genérico para executar operações de banco e retornar um único objeto
    protected <R> R executarOperacaoUnico(DAOOperationUnico<R> operacao) {
        try {
            return operacao.executar();
        } catch (SQLException e) {
            logErro(e);
            return null;
        }
    }

    // Log de erro centralizado
    protected void logErro(SQLException e) {
        System.err.println("Erro: " + e.getMessage());
        e.printStackTrace();
    }

    // Interfaces para operações DAO
    @FunctionalInterface
    public interface DAOOperation<R> {
        List<R> executar() throws SQLException;
    }

    @FunctionalInterface
    public interface DAOOperationUnico<R> {
        R executar() throws SQLException;
    }
}
