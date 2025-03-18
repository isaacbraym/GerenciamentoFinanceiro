package com.gastos.controller;

import com.gastos.db.ParcelamentoDAO;
import com.gastos.model.Parcelamento;
import com.gastos.model.Parcelamento.Parcela;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para gerenciar os parcelamentos no sistema.
 */
public class ParcelamentoController {
    private final ParcelamentoDAO parcelamentoDAO;
    
    /**
     * Construtor padrão.
     */
    public ParcelamentoController() {
        this.parcelamentoDAO = new ParcelamentoDAO();
    }
    
    /**
     * Busca um parcelamento pelo ID.
     * @param id o ID do parcelamento
     * @return o parcelamento encontrado ou null se não encontrar
     * @throws SQLException se ocorrer um erro de SQL
     */
    public Parcelamento buscarParcelamentoPorId(int id) throws SQLException {
        try {
            return parcelamentoDAO.buscarPorId(id);
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelamento: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Lista todos os parcelamentos.
     * @return a lista de parcelamentos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarTodosParcelamentos() throws SQLException {
        try {
            return parcelamentoDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Erro ao listar parcelamentos: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Lista os parcelamentos ativos (que ainda têm parcelas a pagar).
     * @return a lista de parcelamentos ativos
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcelamento> listarParcelamentosAtivos() throws SQLException {
        try {
            return parcelamentoDAO.listarParcelamentosAtivos();
        } catch (SQLException e) {
            System.err.println("Erro ao listar parcelamentos ativos: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Marca uma parcela como paga ou não paga.
     * @param parcelaId o ID da parcela
     * @param paga o novo status de pagamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void marcarParcelaPaga(int parcelaId, boolean paga) throws SQLException {
        try {
            parcelamentoDAO.marcarParcelaPaga(parcelaId, paga);
            
            // Buscar o parcelamento_id da parcela
            int parcelamentoId = parcelamentoDAO.buscarParcelamentoIdDaParcela(parcelaId);
            if (parcelamentoId > 0) {
                // Atualizar o número de parcelas restantes
                parcelamentoDAO.atualizarParcelasRestantes(parcelamentoId);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao marcar parcela como paga: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Atualiza o número de parcelas restantes de um parcelamento.
     * @param parcelamentoId o ID do parcelamento
     * @throws SQLException se ocorrer um erro de SQL
     */
    public void atualizarParcelasRestantes(int parcelamentoId) throws SQLException {
        try {
            parcelamentoDAO.atualizarParcelasRestantes(parcelamentoId);
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar parcelas restantes: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Busca as parcelas a vencer no próximo mês.
     * @return a lista de parcelas a vencer
     * @throws SQLException se ocorrer um erro de SQL
     */
    public List<Parcela> buscarParcelasAVencer() throws SQLException {
        try {
            return parcelamentoDAO.buscarParcelasAVencer();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcelas a vencer: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Atualiza um parcelamento existente.
     * @param parcelamento o parcelamento a ser atualizado
     * @return true se a operação foi bem-sucedida
     */
    public boolean atualizarParcelamento(Parcelamento parcelamento) {
        try {
            parcelamentoDAO.atualizar(parcelamento);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar parcelamento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exclui um parcelamento.
     * @param id o ID do parcelamento a ser excluído
     * @return true se a operação foi bem-sucedida
     */
    public boolean excluirParcelamento(int id) {
        try {
            parcelamentoDAO.excluir(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir parcelamento: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}