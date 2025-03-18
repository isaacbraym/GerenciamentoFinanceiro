package com.gastos.model;

/**
 * Classe que representa uma subcategoria de despesa no sistema.
 * Cada subcategoria pertence a uma categoria específica.
 */
public class SubCategoria {
    private int id;
    private String nome;
    private int categoriaId;
    
    public SubCategoria() {
    }
    
    public SubCategoria(int id, String nome, int categoriaId) {
        this.id = id;
        this.nome = nome;
        this.categoriaId = categoriaId;
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
    
    public int getCategoriaId() {
        return categoriaId;
    }
    
    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
    
    @Override
    public String toString() {
        return nome;
    }
}