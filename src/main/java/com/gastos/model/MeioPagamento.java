package com.gastos.model;

/**
 * Classe que representa um meio de pagamento no sistema.
 */
public class MeioPagamento {
    private int id;
    private String nome;
    private boolean isCartaoCredito;
    
    public MeioPagamento() {
    }
    
    public MeioPagamento(int id, String nome, boolean isCartaoCredito) {
        this.id = id;
        this.nome = nome;
        this.isCartaoCredito = isCartaoCredito;
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
    
    public boolean isCartaoCredito() {
        return isCartaoCredito;
    }
    
    public void setCartaoCredito(boolean isCartaoCredito) {
        this.isCartaoCredito = isCartaoCredito;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}