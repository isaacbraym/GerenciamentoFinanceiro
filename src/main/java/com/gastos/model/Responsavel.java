package com.gastos.model;

/**
 * Classe que representa um responsável por despesas no sistema.
 */
public class Responsavel {
    private int id;
    private String nome;
    
    public Responsavel() {
    }
    
    public Responsavel(int id, String nome) {
        this.id = id;
        this.nome = nome;
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
    
    @Override
    public String toString() {
        return nome;
    }
}