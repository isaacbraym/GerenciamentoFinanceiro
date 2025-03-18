package com.gastos.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um parcelamento no sistema.
 */
public class Parcelamento {
    private int id;
    private double valorTotal;
    private int totalParcelas;
    private int parcelasRestantes;
    private LocalDate dataInicio;
    private List<Parcela> parcelas;
    
    public Parcelamento() {
        this.parcelas = new ArrayList<>();
        this.dataInicio = LocalDate.now();
    }
    
    public Parcelamento(int id, double valorTotal, int totalParcelas, LocalDate dataInicio) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.totalParcelas = totalParcelas;
        this.parcelasRestantes = totalParcelas;
        this.dataInicio = dataInicio;
        this.parcelas = new ArrayList<>();
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public int getTotalParcelas() {
        return totalParcelas;
    }
    
    public void setTotalParcelas(int totalParcelas) {
        this.totalParcelas = totalParcelas;
    }
    
    public int getParcelasRestantes() {
        return parcelasRestantes;
    }
    
    public void setParcelasRestantes(int parcelasRestantes) {
        this.parcelasRestantes = parcelasRestantes;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public List<Parcela> getParcelas() {
        return parcelas;
    }
    
    public void setParcelas(List<Parcela> parcelas) {
        this.parcelas = parcelas;
    }
    
    /**
     * Adiciona uma nova parcela à lista de parcelas
     * @param parcela a parcela a ser adicionada
     */
    public void adicionarParcela(Parcela parcela) {
        this.parcelas.add(parcela);
    }
    
    /**
     * Calcula o valor de cada parcela
     * @return o valor de cada parcela
     */
    public double getValorParcela() {
        if (totalParcelas <= 0) return 0;
        return Math.round((valorTotal / totalParcelas) * 100.0) / 100.0; // Arredondamento para 2 casas decimais
    }
    
    /**
     * Gera todas as parcelas do parcelamento
     * @param cartao o cartão de crédito usado (se aplicável)
     */
    public void gerarParcelas(CartaoCredito cartao) {
        // Limpar parcelas anteriores
        this.parcelas.clear();
        
        // Validar parâmetros
        if (totalParcelas <= 0) {
            System.err.println("ERRO: Número de parcelas deve ser maior que zero!");
            return;
        }
        
        if (valorTotal <= 0) {
            System.err.println("ERRO: Valor total deve ser maior que zero!");
            return;
        }
        
        if (dataInicio == null) {
            System.err.println("ERRO: Data de início não pode ser nula!");
            dataInicio = LocalDate.now();
        }
        
        double valorParcela = getValorParcela();
        
        System.out.println("Gerando " + totalParcelas + " parcelas de R$ " + valorParcela);
        System.out.println("Data início: " + dataInicio);
        
        for (int i = 1; i <= totalParcelas; i++) {
            Parcela parcela = new Parcela();
            parcela.setNumeroParcela(i);
            parcela.setValor(valorParcela);
            
            // Para a última parcela, ajustar o valor para garantir que a soma seja exatamente o valor total
            if (i == totalParcelas) {
                double somaAnterior = valorParcela * (totalParcelas - 1);
                double valorUltimaParcela = valorTotal - somaAnterior;
                // Arredondar para 2 casas decimais
                valorUltimaParcela = Math.round(valorUltimaParcela * 100.0) / 100.0;
                parcela.setValor(valorUltimaParcela);
                System.out.println("Ajustando valor da última parcela para R$ " + valorUltimaParcela);
            }
            
            // Cálculo da data de vencimento
            LocalDate dataVencimento;
            
            try {
                if (cartao != null) {
                    // Se for cartão de crédito, usar a lógica de vencimento do cartão
                    LocalDate dataBase = dataInicio;
                    
                    // Adicionar meses à data base baseado no número da parcela
                    int mesesAdicionais = i - 1;
                    dataBase = dataBase.plusMonths(mesesAdicionais);
                    
                    // Obter ano e mês da data base
                    int ano = dataBase.getYear();
                    int mes = dataBase.getMonthValue();
                    
                    // Calcular o dia de vencimento, garantindo que seja válido para o mês
                    int diaVencimento = cartao.getDiaVencimento();
                    int ultimoDiaMes = dataBase.withDayOfMonth(1).plusMonths(1).minusDays(1).getDayOfMonth();
                    
                    // Se o dia de vencimento for maior que o último dia do mês, usar o último dia
                    if (diaVencimento > ultimoDiaMes) {
                        diaVencimento = ultimoDiaMes;
                    }
                    
                    // Criar a data de vencimento
                    dataVencimento = LocalDate.of(ano, mes, diaVencimento);
                    
                    // Se a data de vencimento for anterior à data de início, avançar um mês
                    if (dataVencimento.isBefore(dataInicio)) {
                        dataVencimento = dataVencimento.plusMonths(1);
                    }
                    
                    System.out.println("Parcela " + i + ": Vencimento calculado com cartão: " + dataVencimento);
                } else {
                    // Caso contrário, simplesmente adicionar meses à data inicial
                    dataVencimento = dataInicio.plusMonths(i - 1);
                    System.out.println("Parcela " + i + ": Vencimento padrão: " + dataVencimento);
                }
            } catch (Exception e) {
                // Em caso de erro no cálculo da data, usar uma data segura
                System.err.println("ERRO ao calcular data de vencimento para parcela " + i + ": " + e.getMessage());
                dataVencimento = dataInicio.plusMonths(i - 1);
                System.out.println("Usando data alternativa de vencimento: " + dataVencimento);
            }
            
            parcela.setDataVencimento(dataVencimento);
            parcela.setPaga(i == 1 && dataInicio.equals(LocalDate.now())); // Primeira parcela já paga se a data for hoje
            
            parcelas.add(parcela);
            System.out.println("Parcela " + i + " gerada com vencimento em " + dataVencimento);
        }
        
        System.out.println("Total de parcelas geradas: " + parcelas.size());
    }
    
    /**
     * Atualiza o número de parcelas restantes com base no status de pagamento das parcelas
     */
    public void atualizarParcelasRestantes() {
        int pagas = 0;
        for (Parcela parcela : parcelas) {
            if (parcela.isPaga()) {
                pagas++;
            }
        }
        this.parcelasRestantes = totalParcelas - pagas;
        System.out.println("Parcelas restantes atualizadas: " + this.parcelasRestantes);
    }
    
    /**
     * Retorna uma string com informações sobre o parcelamento
     * @return informações do parcelamento
     */
    public String getResumo() {
        StringBuilder sb = new StringBuilder();
        sb.append(totalParcelas).append("x de R$ ").append(String.format("%.2f", getValorParcela()));
        sb.append(" (").append(parcelasRestantes).append(" restante(s))");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return totalParcelas + "x de R$ " + String.format("%.2f", getValorParcela());
    }
    
    /**
     * Classe interna para representar cada parcela do parcelamento
     */
    public static class Parcela {
        private int id;
        private int numeroParcela;
        private double valor;
        private LocalDate dataVencimento;
        private boolean paga;
        private int parcelamentoId; // Campo adicional para facilitar operações
        
        public Parcela() {
            this.paga = false;
        }
        
        // Getters e Setters
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public int getNumeroParcela() {
            return numeroParcela;
        }
        
        public void setNumeroParcela(int numeroParcela) {
            this.numeroParcela = numeroParcela;
        }
        
        public double getValor() {
            return valor;
        }
        
        public void setValor(double valor) {
            this.valor = valor;
        }
        
        public LocalDate getDataVencimento() {
            return dataVencimento;
        }
        
        public void setDataVencimento(LocalDate dataVencimento) {
            this.dataVencimento = dataVencimento;
        }
        
        public boolean isPaga() {
            return paga;
        }
        
        public void setPaga(boolean paga) {
            this.paga = paga;
        }
        
        public int getParcelamentoId() {
            return parcelamentoId;
        }
        
        public void setParcelamentoId(int parcelamentoId) {
            this.parcelamentoId = parcelamentoId;
        }
        
        @Override
        public String toString() {
            return numeroParcela + "/" + " - R$ " + String.format("%.2f", valor) + 
                   (paga ? " [PAGA]" : " [A PAGAR]") + 
                   " - Venc.: " + dataVencimento;
        }
    }
}