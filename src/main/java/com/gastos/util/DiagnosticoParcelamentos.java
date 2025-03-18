package com.gastos.util;

import com.gastos.db.ConexaoBanco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário de diagnóstico para verificar a integridade dos parcelamentos.
 * Esta classe deve ser usada apenas em ambiente de desenvolvimento.
 */
public class DiagnosticoParcelamentos {
    
    // Consultas SQL
    private static final String SQL_COUNT_PARCELAMENTOS = 
            "SELECT COUNT(*) as total FROM parcelamentos";
    
    private static final String SQL_SAMPLE_PARCELAMENTOS = 
            "SELECT id, valor_total, total_parcelas, parcelas_restantes, data_inicio " +
            "FROM parcelamentos LIMIT 10";
    
    private static final String SQL_DESPESAS_COM_PARCELAMENTOS = 
            "SELECT d.id, d.descricao, d.valor, d.parcelamento_id FROM despesas d " +
            "WHERE d.parcelamento_id IS NOT NULL";
    
    private static final String SQL_COUNT_PARCELAS = 
            "SELECT COUNT(*) as total FROM parcelas";
    
    private static final String SQL_PARCELAS_POR_PARCELAMENTO = 
            "SELECT parcelamento_id, COUNT(*) as total FROM parcelas GROUP BY parcelamento_id";
    
    private static final String SQL_LISTAR_PARCELAMENTOS = 
            "SELECT id, valor_total, total_parcelas FROM parcelamentos";
    
    private static final String SQL_LISTAR_PARCELAS = 
            "SELECT id, numero_parcela, valor, data_vencimento, paga " +
            "FROM parcelas WHERE parcelamento_id = ? ORDER BY numero_parcela";
    
    private static final String SQL_DESPESA_VINCULADA = 
            "SELECT id, descricao FROM despesas WHERE parcelamento_id = ?";

    public static void main(String[] args) {
        System.out.println("\n===== DIAGNÓSTICO DE PARCELAMENTOS =====\n");
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            // Executar diagnósticos
            verificarTabelaParcelamentos(conn);
            verificarDespesasComParcelamentos(conn);
            verificarParcelas(conn);
            verificarParcelasDetalhadas(conn);
            
        } catch (Exception e) {
            reportarErro("ERRO DURANTE DIAGNÓSTICO", e);
        }
        
        System.out.println("\n===== FIM DO DIAGNÓSTICO =====");
    }
    
    /**
     * Verificar tabela de parcelamentos.
     */
    private static void verificarTabelaParcelamentos(Connection conn) throws SQLException {
        System.out.println("--- Verificando tabela de parcelamentos ---");
        
        // Contar parcelamentos
        int totalParcelamentos = contarRegistros(conn, SQL_COUNT_PARCELAMENTOS);
        System.out.println("Total de parcelamentos no banco: " + totalParcelamentos);
        
        if (totalParcelamentos == 0) {
            System.out.println("⚠️ ALERTA: Não existem parcelamentos no banco de dados!");
            return;
        }
        
        // Mostrar amostra de parcelamentos
        System.out.println("\nAmostra de parcelamentos:");
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SAMPLE_PARCELAMENTOS)) {
            
            int contador = 0;
            while (rs.next()) {
                contador++;
                System.out.println(contador + ". ID: " + rs.getInt("id") +
                                  ", Valor: R$ " + rs.getDouble("valor_total") +
                                  ", Parcelas: " + rs.getInt("total_parcelas") +
                                  ", Restantes: " + rs.getInt("parcelas_restantes") +
                                  ", Data início: " + formatarData(rs.getString("data_inicio")));
            }
        }
    }
    
    /**
     * Verificar despesas vinculadas a parcelamentos.
     */
    private static void verificarDespesasComParcelamentos(Connection conn) throws SQLException {
        System.out.println("\n--- Verificando despesas com parcelamentos ---");
        
        List<DespesaParcelada> despesasParceladas = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_DESPESAS_COM_PARCELAMENTOS)) {
            
            while (rs.next()) {
                DespesaParcelada despesa = new DespesaParcelada(
                    rs.getInt("id"),
                    rs.getString("descricao"),
                    rs.getDouble("valor"),
                    rs.getInt("parcelamento_id")
                );
                despesasParceladas.add(despesa);
            }
        }
        
        if (despesasParceladas.isEmpty()) {
            System.out.println("⚠️ ALERTA: Nenhuma despesa com parcelamento encontrada!");
            System.out.println("Isso indica que os parcelamentos existentes não estão vinculados a despesas.");
        } else {
            System.out.println("Total de " + despesasParceladas.size() + " despesas com parcelamentos.");
            
            // Mostrar até 5 exemplos
            int limite = Math.min(despesasParceladas.size(), 5);
            for (int i = 0; i < limite; i++) {
                DespesaParcelada d = despesasParceladas.get(i);
                System.out.println((i+1) + ". Despesa ID: " + d.id +
                                  ", Descrição: " + d.descricao +
                                  ", Valor: R$ " + d.valor +
                                  ", Parcelamento ID: " + d.parcelamentoId);
            }
            
            if (despesasParceladas.size() > 5) {
                System.out.println("... e mais " + (despesasParceladas.size() - 5) + " despesa(s)");
            }
        }
    }
    
    /**
     * Verificar parcelas existentes.
     */
    private static void verificarParcelas(Connection conn) throws SQLException {
        System.out.println("\n--- Verificando parcelas existentes ---");
        
        // Contar parcelas
        int totalParcelas = contarRegistros(conn, SQL_COUNT_PARCELAS);
        System.out.println("Total de parcelas no banco: " + totalParcelas);
        
        if (totalParcelas == 0) {
            System.out.println("⚠️ ALERTA: Não existem parcelas no banco de dados!");
            return;
        }
        
        // Verificar parcelas por parcelamento
        System.out.println("\nContagem de parcelas por parcelamento:");
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_PARCELAS_POR_PARCELAMENTO)) {
            
            int contador = 0;
            while (rs.next()) {
                contador++;
                System.out.println(contador + ". Parcelamento ID: " + rs.getInt("parcelamento_id") +
                                  ", Total de parcelas: " + rs.getInt("total"));
            }
            
            if (contador == 0) {
                System.out.println("Nenhuma parcela associada a parcelamentos!");
            }
        }
    }
    
    /**
     * Verificar detalhadamente as parcelas de cada parcelamento.
     */
    private static void verificarParcelasDetalhadas(Connection conn) throws SQLException {
        System.out.println("\n--- Verificando parcelas por parcelamento (detalhado) ---");
        
        List<Parcelamento> parcelamentos = listarParcelamentos(conn);
        
        System.out.println("Analisando " + parcelamentos.size() + " parcelamentos...");
        
        int parcelamentosComDefeito = 0;
        
        for (Parcelamento p : parcelamentos) {
            System.out.println("\nParcelamento #" + p.id + 
                              " (Valor: R$ " + p.valorTotal + 
                              ", Parcelas: " + p.totalParcelas + ")");
            
            // Buscar parcelas
            List<Parcela> parcelas = listarParcelas(conn, p.id);
            
            // Verificar consistência
            boolean temDefeito = false;
            
            if (parcelas.isEmpty()) {
                System.out.println("  ⚠️ Este parcelamento não tem parcelas!");
                temDefeito = true;
            } else if (parcelas.size() != p.totalParcelas) {
                System.out.println("  ⚠️ Inconsistência: O parcelamento deveria ter " + 
                                  p.totalParcelas + " parcelas, mas tem " + parcelas.size() + "!");
                temDefeito = true;
            }
            
            // Mostrar parcelas (limitado a 3 para não poluir a saída)
            if (!parcelas.isEmpty()) {
                int limite = Math.min(parcelas.size(), 3);
                for (int i = 0; i < limite; i++) {
                    Parcela parcela = parcelas.get(i);
                    System.out.println("  - Parcela #" + parcela.numero + 
                                      " (Valor: R$ " + parcela.valor + 
                                      ", Vencimento: " + formatarData(parcela.dataVencimento) + 
                                      ", Paga: " + (parcela.paga ? "Sim" : "Não") + ")");
                }
                
                if (parcelas.size() > 3) {
                    System.out.println("  ... e mais " + (parcelas.size() - 3) + " parcela(s)");
                }
            }
            
            // Verificar se há despesa vinculada
            DespesaVinculada despesa = buscarDespesaVinculada(conn, p.id);
            if (despesa != null) {
                System.out.println("  → Vinculado à despesa ID " + despesa.id + 
                                  " (" + despesa.descricao + ")");
            } else {
                System.out.println("  ⚠️ Este parcelamento não está vinculado a nenhuma despesa!");
                temDefeito = true;
            }
            
            if (temDefeito) {
                parcelamentosComDefeito++;
            }
        }
        
        // Resumo final
        System.out.println("\nResumo: " + parcelamentosComDefeito + " de " + 
                          parcelamentos.size() + " parcelamentos têm inconsistências.");
    }
    
    /**
     * Lista todos os parcelamentos.
     */
    private static List<Parcelamento> listarParcelamentos(Connection conn) throws SQLException {
        List<Parcelamento> parcelamentos = new ArrayList<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_LISTAR_PARCELAMENTOS)) {
            
            while (rs.next()) {
                Parcelamento p = new Parcelamento(
                    rs.getInt("id"),
                    rs.getDouble("valor_total"),
                    rs.getInt("total_parcelas")
                );
                parcelamentos.add(p);
            }
        }
        
        return parcelamentos;
    }
    
    /**
     * Lista as parcelas de um parcelamento.
     */
    private static List<Parcela> listarParcelas(Connection conn, int parcelamentoId) throws SQLException {
        List<Parcela> parcelas = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(SQL_LISTAR_PARCELAS)) {
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Parcela p = new Parcela(
                        rs.getInt("id"),
                        rs.getInt("numero_parcela"),
                        rs.getDouble("valor"),
                        rs.getString("data_vencimento"),
                        rs.getBoolean("paga")
                    );
                    parcelas.add(p);
                }
            }
        }
        
        return parcelas;
    }
    
    /**
     * Busca a despesa vinculada a um parcelamento.
     */
    private static DespesaVinculada buscarDespesaVinculada(Connection conn, int parcelamentoId) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_DESPESA_VINCULADA)) {
            stmt.setInt(1, parcelamentoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new DespesaVinculada(
                        rs.getInt("id"),
                        rs.getString("descricao")
                    );
                }
            }
        }
        
        return null;
    }
    
    /**
     * Conta registros em uma tabela usando a consulta SQL fornecida.
     */
    private static int contarRegistros(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Formata uma data para exibição.
     */
    private static String formatarData(String dataISO) {
        if (dataISO == null || dataISO.isEmpty()) {
            return "N/A";
        }
        
        try {
            LocalDate data = LocalDate.parse(dataISO);
            return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dataISO + " (formato inválido)";
        }
    }
    
    /**
     * Reporta um erro de maneira padronizada.
     */
    private static void reportarErro(String mensagem, Exception e) {
        System.err.println(mensagem + ": " + e.getMessage());
        e.printStackTrace();
    }
    
    // Classes internas para armazenar dados
    
    /**
     * Representa um parcelamento para fins de diagnóstico.
     */
    private static class Parcelamento {
        final int id;
        final double valorTotal;
        final int totalParcelas;
        
        Parcelamento(int id, double valorTotal, int totalParcelas) {
            this.id = id;
            this.valorTotal = valorTotal;
            this.totalParcelas = totalParcelas;
        }
    }
    
    /**
     * Representa uma parcela para fins de diagnóstico.
     */
    private static class Parcela {
        final int id;
        final int numero;
        final double valor;
        final String dataVencimento;
        final boolean paga;
        
        Parcela(int id, int numero, double valor, String dataVencimento, boolean paga) {
            this.id = id;
            this.numero = numero;
            this.valor = valor;
            this.dataVencimento = dataVencimento;
            this.paga = paga;
        }
    }
    
    /**
     * Representa uma despesa vinculada a um parcelamento.
     */
    private static class DespesaVinculada {
        final int id;
        final String descricao;
        
        DespesaVinculada(int id, String descricao) {
            this.id = id;
            this.descricao = descricao;
        }
    }
    
    /**
     * Representa uma despesa parcelada para fins de diagnóstico.
     */
    private static class DespesaParcelada {
        final int id;
        final String descricao;
        final double valor;
        final int parcelamentoId;
        
        DespesaParcelada(int id, String descricao, double valor, int parcelamentoId) {
            this.id = id;
            this.descricao = descricao;
            this.valor = valor;
            this.parcelamentoId = parcelamentoId;
        }
    }
}