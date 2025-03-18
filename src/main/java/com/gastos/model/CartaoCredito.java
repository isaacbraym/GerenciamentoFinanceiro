package com.gastos.model;

import java.time.LocalDate;

/**
 * Classe que representa um cartão de crédito no sistema.
 */
public class CartaoCredito {
    private int id;
    private String nome;
    private String bandeira;
    private double limite;
    private int diaFechamento;
    private int diaVencimento;
    private String cor; // Para personalização visual
    
    public CartaoCredito() {
    }
    
    public CartaoCredito(int id, String nome, String bandeira, double limite, 
                         int diaFechamento, int diaVencimento, String cor) {
        this.id = id;
        this.nome = nome;
        this.bandeira = bandeira;
        this.limite = limite;
        this.diaFechamento = diaFechamento;
        this.diaVencimento = diaVencimento;
        this.cor = cor;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getBandeira() {
        return bandeira;
    }
    
    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }
    
    public double getLimite() {
        return limite;
    }
    
    public void setLimite(double limite) {
        this.limite = limite;
    }
    
    public int getDiaFechamento() {
        return diaFechamento;
    }
    
    public void setDiaFechamento(int diaFechamento) {
        this.diaFechamento = diaFechamento;
    }
    
    public int getDiaVencimento() {
        return diaVencimento;
    }
    
    public void setDiaVencimento(int diaVencimento) {
        this.diaVencimento = diaVencimento;
    }
    
    public String getCor() {
        return cor;
    }
    
    public void setCor(String cor) {
        this.cor = cor;
    }
    
    /**
     * Calcula a data de fechamento da próxima fatura com base na data atual
     * @return A data de fechamento da próxima fatura
     */
    public LocalDate calcularProximoFechamento() {
        LocalDate hoje = LocalDate.now();
        LocalDate dataFechamento = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaFechamento);
        
        if (hoje.isAfter(dataFechamento) || hoje.isEqual(dataFechamento)) {
            return dataFechamento.plusMonths(1);
        } else {
            return dataFechamento;
        }
    }
    
    /**
     * Calcula a data de vencimento da próxima fatura com base na data atual
     * @return A data de vencimento da próxima fatura
     */
    public LocalDate calcularProximoVencimento() {
        LocalDate proximoFechamento = calcularProximoFechamento();
        LocalDate dataVencimento = LocalDate.of(
            proximoFechamento.getYear(), 
            proximoFechamento.getMonth(), 
            diaVencimento);
        
        if (diaVencimento < diaFechamento) {
            return dataVencimento.plusMonths(1);
        } else {
            return dataVencimento;
        }
    }
    
    @Override
    public String toString() {
        return nome;
    }
}