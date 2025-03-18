package com.gastos.util;

import com.gastos.db.ConexaoBanco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Classe para diagnóstico específico de parcelamentos.
 * Execute esta classe para verificar os parcelamentos no banco de dados.
 */
public class DiagnosticoParcelamentos {

    public static void main(String[] args) {
        System.out.println("\n===== DIAGNÓSTICO DE PARCELAMENTOS =====\n");
        
        try (Connection conn = ConexaoBanco.getConexao()) {
            // 1. Verificar a tabela de parcelamentos
            verificarTabelaParcelamentos(conn);
            
            // 2. Verificar despesas com parcelamentos
            verificarDespesasComParcelamentos(conn);
            
            // 3. Verificar parcelas existentes
            verificarParcelas(conn);
            
            // 4. Para cada parcelamento, verificar suas parcelas
            verificarParcelasDetalhadas(conn);
            
        } catch (Exception e) {
            System.err.println("ERRO DURANTE DIAGNÓSTICO: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n===== FIM DO DIAGNÓSTICO =====");
    }
    
    /**
     * Verifica a tabela de parcelamentos e mostra quantos existem.
     */
    private static void verificarTabelaParcelamentos(Connection conn) throws Exception {
        System.out.println("--- Verificando tabela de parcelamentos ---");
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM parcelamentos")) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("Total de parcelamentos no banco: " + total);
                
                if (total == 0) {
                    System.out.println("⚠️ ALERTA: Não existem parcelamentos no banco de dados!");
                }
            }
        }
        
        // Mostrar amostra de parcelamentos
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, valor_total, total_parcelas, parcelas_restantes, data_inicio FROM parcelamentos LIMIT 10")) {
            
            System.out.println("\nAmostra de parcelamentos:");
            int contador = 0;
            
            while (rs.next()) {
                contador++;
                System.out.println(contador + ". ID: " + rs.getInt("id") +
                                  ", Valor: R$ " + rs.getDouble("valor_total") +
                                  ", Parcelas: " + rs.getInt("total_parcelas") +
                                  ", Restantes: " + rs.getInt("parcelas_restantes") +
                                  ", Data início: " + rs.getString("data_inicio"));
            }
            
            if (contador == 0) {
                System.out.println("Nenhum parcelamento encontrado!");
            }
        }
    }
    
    /**
     * Verifica despesas que têm parcelamentos associados.
     */
    private static void verificarDespesasComParcelamentos(Connection conn) throws Exception {
        System.out.println("\n--- Verificando despesas com parcelamentos ---");
        
        String sql = "SELECT d.id, d.descricao, d.valor, d.parcelamento_id FROM despesas d " +
                     "WHERE d.parcelamento_id IS NOT NULL";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int contador = 0;
            while (rs.next()) {
                contador++;
                System.out.println(contador + ". Despesa ID: " + rs.getInt("id") +
                                  ", Descrição: " + rs.getString("descricao") +
                                  ", Valor: R$ " + rs.getDouble("valor") +
                                  ", Parcelamento ID: " + rs.getInt("parcelamento_id"));
            }
            
            if (contador == 0) {
                System.out.println("⚠️ ALERTA: Nenhuma despesa com parcelamento encontrada!");
                System.out.println("Isso indica que os parcelamentos existentes não estão vinculados a despesas.");
            } else {
                System.out.println("Total de " + contador + " despesas com parcelamentos.");
            }
        }
    }
    
    /**
     * Verifica as parcelas existentes no banco.
     */
    private static void verificarParcelas(Connection conn) throws Exception {
        System.out.println("\n--- Verificando parcelas existentes ---");
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM parcelas")) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("Total de parcelas no banco: " + total);
                
                if (total == 0) {
                    System.out.println("⚠️ ALERTA: Não existem parcelas no banco de dados!");
                }
            }
        }
        
        // Verificar parcelas por parcelamento
        String sql = "SELECT parcelamento_id, COUNT(*) as total FROM parcelas GROUP BY parcelamento_id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\nContagem de parcelas por parcelamento:");
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
     * Verifica detalhadamente as parcelas de cada parcelamento.
     */
    private static void verificarParcelasDetalhadas(Connection conn) throws Exception {
        System.out.println("\n--- Verificando parcelas por parcelamento (detalhado) ---");
        
        // Primeiro, buscar todos os parcelamentos
        String sqlParcelamentos = "SELECT id, valor_total, total_parcelas FROM parcelamentos";
        
        try (Statement stmtParc = conn.createStatement();
             ResultSet rsParc = stmtParc.executeQuery(sqlParcelamentos)) {
            
            int contadorParcelamentos = 0;
            
            while (rsParc.next()) {
                contadorParcelamentos++;
                int parcelamentoId = rsParc.getInt("id");
                double valorTotal = rsParc.getDouble("valor_total");
                int totalParcelas = rsParc.getInt("total_parcelas");
                
                System.out.println("\nParcelamento #" + contadorParcelamentos + 
                                  " (ID: " + parcelamentoId + 
                                  ", Valor: R$ " + valorTotal + 
                                  ", Parcelas: " + totalParcelas + ")");
                
                // Buscar as parcelas deste parcelamento
                String sqlParcelas = "SELECT id, numero_parcela, valor, data_vencimento, paga " +
                                    "FROM parcelas WHERE parcelamento_id = ? ORDER BY numero_parcela";
                
                try (PreparedStatement stmtParcelas = conn.prepareStatement(sqlParcelas)) {
                    stmtParcelas.setInt(1, parcelamentoId);
                    
                    try (ResultSet rsParcelas = stmtParcelas.executeQuery()) {
                        int contadorParcelas = 0;
                        
                        while (rsParcelas.next()) {
                            contadorParcelas++;
                            System.out.println("  - Parcela #" + contadorParcelas + 
                                              " (ID: " + rsParcelas.getInt("id") + 
                                              ", Nº: " + rsParcelas.getInt("numero_parcela") + 
                                              ", Valor: R$ " + rsParcelas.getDouble("valor") + 
                                              ", Vencimento: " + rsParcelas.getString("data_vencimento") + 
                                              ", Paga: " + (rsParcelas.getBoolean("paga") ? "Sim" : "Não") + ")");
                        }
                        
                        if (contadorParcelas == 0) {
                            System.out.println("  ⚠️ Este parcelamento não tem parcelas!");
                        } else if (contadorParcelas != totalParcelas) {
                            System.out.println("  ⚠️ Inconsistência: O parcelamento deveria ter " + 
                                              totalParcelas + " parcelas, mas tem " + contadorParcelas + "!");
                        }
                    }
                }
                
                // Verificar se há alguma despesa vinculada a este parcelamento
                String sqlDespesa = "SELECT id, descricao FROM despesas WHERE parcelamento_id = ?";
                
                try (PreparedStatement stmtDespesa = conn.prepareStatement(sqlDespesa)) {
                    stmtDespesa.setInt(1, parcelamentoId);
                    
                    try (ResultSet rsDespesa = stmtDespesa.executeQuery()) {
                        if (rsDespesa.next()) {
                            System.out.println("  → Vinculado à despesa ID " + rsDespesa.getInt("id") + 
                                              " (" + rsDespesa.getString("descricao") + ")");
                        } else {
                            System.out.println("  ⚠️ Este parcelamento não está vinculado a nenhuma despesa!");
                        }
                    }
                }
            }
            
            if (contadorParcelamentos == 0) {
                System.out.println("Nenhum parcelamento encontrado para análise!");
            }
        }
    }
}