package com.gastos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa uma categoria de despesa no sistema.
 * Cada categoria pode ter várias subcategorias associadas.
 */
public class CategoriaDespesa {
    private int id;
    private String nome;
    private List<SubCategoria> subCategorias;
    
    public CategoriaDespesa() {
        this.subCategorias = new ArrayList<>();
    }
    
    public CategoriaDespesa(int id, String nome) {
        this.id = id;
        this.nome = nome;
        this.subCategorias = new ArrayList<>();
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
    
    public List<SubCategoria> getSubCategorias() {
        return subCategorias;
    }
    
    public void setSubCategorias(List<SubCategoria> subCategorias) {
        this.subCategorias = subCategorias;
    }
    
    public void adicionarSubCategoria(SubCategoria subCategoria) {
        this.subCategorias.add(subCategoria);
    }
    
    @Override
    public String toString() {
        return nome;
    }
}