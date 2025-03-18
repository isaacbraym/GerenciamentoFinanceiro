package com.gastos.model;

import java.time.LocalDate;

/**
 * Classe que representa uma despesa no sistema.
 */
public class Despesa {
    private int id;
    private String descricao;
    private double valor;
    private LocalDate dataCompra;
    private LocalDate dataVencimento;
    private boolean pago;
    private boolean fixo;
    private CategoriaDespesa categoria;
    private SubCategoria subCategoria;
    private Responsavel responsavel;
    private MeioPagamento meioPagamento;
    private CartaoCredito cartaoCredito;
    private Parcelamento parcelamento;
    
    public Despesa() {
        this.dataCompra = LocalDate.now();
        this.pago = false;
        this.fixo = false;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public double getValor() {
        return valor;
    }
    
    public void setValor(double valor) {
        this.valor = valor;
    }
    
    public LocalDate getDataCompra() {
        return dataCompra;
    }
    
    public void setDataCompra(LocalDate dataCompra) {
        this.dataCompra = dataCompra;
    }
    
    public LocalDate getDataVencimento() {
        return dataVencimento;
    }
    
    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }
    
    public boolean isPago() {
        return pago;
    }
    
    public void setPago(boolean pago) {
        this.pago = pago;
    }
    
    public boolean isFixo() {
        return fixo;
    }
    
    public void setFixo(boolean fixo) {
        this.fixo = fixo;
    }
    
    public CategoriaDespesa getCategoria() {
        return categoria;
    }
    
    public void setCategoria(CategoriaDespesa categoria) {
        this.categoria = categoria;
    }
    
    public SubCategoria getSubCategoria() {
        return subCategoria;
    }
    
    public void setSubCategoria(SubCategoria subCategoria) {
        this.subCategoria = subCategoria;
    }
    
    public Responsavel getResponsavel() {
        return responsavel;
    }
    
    public void setResponsavel(Responsavel responsavel) {
        this.responsavel = responsavel;
    }
    
    public MeioPagamento getMeioPagamento() {
        return meioPagamento;
    }
    
    public void setMeioPagamento(MeioPagamento meioPagamento) {
        this.meioPagamento = meioPagamento;
    }
    
    public CartaoCredito getCartaoCredito() {
        return cartaoCredito;
    }
    
    public void setCartaoCredito(CartaoCredito cartaoCredito) {
        this.cartaoCredito = cartaoCredito;
    }
    
    public Parcelamento getParcelamento() {
        return parcelamento;
    }
    
    public void setParcelamento(Parcelamento parcelamento) {
        this.parcelamento = parcelamento;
    }
    
    /**
     * Verifica se esta despesa está relacionada a um cartão de crédito
     * @return true se for um pagamento com cartão de crédito
     */
    public boolean isCartaoCredito() {
        return meioPagamento != null && meioPagamento.isCartaoCredito() && cartaoCredito != null;
    }
    
    /**
     * Verifica se esta despesa é parcelada
     * @return true se for uma compra parcelada
     */
    public boolean isParcelada() {
        return parcelamento != null;
    }
    
    @Override
    public String toString() {
        return descricao + " - R$ " + String.format("%.2f", valor);
    }
}