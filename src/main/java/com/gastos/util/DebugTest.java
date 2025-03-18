package com.gastos.util;

import com.gastos.db.ConexaoBanco;
import com.gastos.model.CategoriaDespesa;
import com.gastos.model.Despesa;
import com.gastos.controller.CategoriaController;
import com.gastos.controller.DespesaController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe para testes e depuração do banco de dados.
 * Pode ser executada para verificar se o banco está funcionando corretamente.
 */
public class DebugTest {

    public static void main(String[] args) {
        testarBancoDeDados();
    }
    
    /**
     * Testa o banco de dados e realiza operações básicas de CRUD.
     */
    public static void testarBancoDeDados() {
        System.out.println("\n=== INICIANDO TESTES DE DEPURAÇÃO DO BANCO DE DADOS ===");
        
        try {
            // Verificar conexão com o banco
            Connection conn = ConexaoBanco.getConexao();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexão com o banco estabelecida com sucesso!");
            } else {
                System.out.println("❌ Falha ao conectar ao banco de dados!");
                return;
            }
            
            // Verificar integridade do banco
            ConexaoBanco.verificarIntegridadeBanco();
            
            // Inserir uma despesa de teste
            inserirDespesaTeste();
            
            // Listar todas as despesas
            listarTodasDespesas();
            
            // Testar funções do DespesaController
            testarDespesaController();
            
            System.out.println("\n=== TESTES DE DEPURAÇÃO CONCLUÍDOS ===");
            
        } catch (Exception e) {
            System.err.println("❌ ERRO DURANTE OS TESTES: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fechar conexão ao final dos testes
            ConexaoBanco.fecharConexao();
        }
    }
    
    /**
     * Insere uma despesa de teste no banco de dados.
     */
    private static void inserirDespesaTeste() {
        System.out.println("\n--- Inserindo despesa de teste ---");
        
        try {
            // Obter a primeira categoria disponível
            CategoriaController categoriaController = new CategoriaController();
            List<CategoriaDespesa> categorias = new ArrayList<>(categoriaController.listarTodasCategorias());
            
            if (categorias.isEmpty()) {
                System.out.println("❌ Não há categorias disponíveis para o teste!");
                return;
            }
            
            CategoriaDespesa categoria = categorias.get(0);
            System.out.println("Usando categoria: " + categoria.getNome() + " (ID: " + categoria.getId() + ")");
            
            // Criar uma despesa de teste
            Despesa despesa = new Despesa();
            despesa.setDescricao("Despesa de teste - " + System.currentTimeMillis());
            despesa.setValor(100.00);
            despesa.setDataCompra(LocalDate.now());
            despesa.setDataVencimento(LocalDate.now().plusDays(10));
            despesa.setPago(false);
            despesa.setFixo(false);
            despesa.setCategoria(categoria);
            
            // Salvar usando o controlador
            DespesaController despesaController = new DespesaController();
            DespesaController.Resultado resultado = despesaController.salvarDespesa(despesa);
            
            if (resultado.isSucesso()) {
                System.out.println("✅ Despesa de teste criada com sucesso! ID: " + despesa.getId());
            } else {
                System.out.println("❌ Falha ao criar despesa de teste: " + resultado.getMensagem());
                
                // Tentar inserir diretamente no banco
                inserirDespesaTesteDireta(despesa);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao inserir despesa de teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Insere uma despesa diretamente no banco, ignorando os controladores.
     * @param despesa A despesa a ser inserida
     */
    private static void inserirDespesaTesteDireta(Despesa despesa) {
        System.out.println("\n--- Tentando inserir despesa diretamente no banco ---");
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            
            String sql = "INSERT INTO despesas (descricao, valor, data_compra, data_vencimento, pago, fixo, categoria_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, despesa.getDescricao() + " (INSERÇÃO DIRETA)");
            stmt.setDouble(2, despesa.getValor());
            stmt.setString(3, despesa.getDataCompra().toString());
            stmt.setString(4, despesa.getDataVencimento().toString());
            stmt.setBoolean(5, despesa.isPago());
            stmt.setBoolean(6, despesa.isFixo());
            stmt.setInt(7, despesa.getCategoria().getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    System.out.println("✅ Despesa inserida diretamente no banco com sucesso! ID: " + id);
                } else {
                    System.out.println("⚠️ Despesa inserida, mas não foi possível obter o ID.");
                }
            } else {
                System.out.println("❌ Falha ao inserir despesa diretamente no banco.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro SQL ao inserir despesa diretamente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Lista todas as despesas diretamente do banco de dados.
     */
    private static void listarTodasDespesas() {
        System.out.println("\n--- Listando todas as despesas diretamente do banco ---");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConexaoBanco.getConexao();
            stmt = conn.createStatement();
            
            String sql = "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, " +
                         "c.nome as categoria_nome " +
                         "FROM despesas d " +
                         "LEFT JOIN categorias c ON d.categoria_id = c.id " +
                         "ORDER BY d.id DESC";
            
            rs = stmt.executeQuery(sql);
            
            int contador = 0;
            while (rs.next()) {
                contador++;
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                double valor = rs.getDouble("valor");
                String dataCompra = rs.getString("data_compra");
                String dataVencimento = rs.getString("data_vencimento");
                boolean pago = rs.getBoolean("pago");
                String categoriaNome = rs.getString("categoria_nome");
                
                System.out.println(contador + ". ID: " + id + 
                                  ", Descrição: " + descricao + 
                                  ", Valor: R$ " + valor + 
                                  ", Data Compra: " + dataCompra + 
                                  ", Vencimento: " + dataVencimento + 
                                  ", Pago: " + (pago ? "Sim" : "Não") + 
                                  ", Categoria: " + categoriaNome);
            }
            
            if (contador == 0) {
                System.out.println("❌ Nenhuma despesa encontrada no banco de dados!");
            } else {
                System.out.println("✅ Total de " + contador + " despesas encontradas no banco!");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar despesas: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Testa as funções do DespesaController.
     */
    private static void testarDespesaController() {
        System.out.println("\n--- Testando DespesaController ---");
        
        DespesaController controller = new DespesaController();
        
        try {
            System.out.println("Testando listarTodasDespesas()...");
            int totalTodas = controller.listarTodasDespesas().size();
            System.out.println("✅ listarTodasDespesas() retornou " + totalTodas + " despesas");
            
            System.out.println("Testando listarDespesasDoMes()...");
            int totalMes = controller.listarDespesasDoMes().size();
            System.out.println("✅ listarDespesasDoMes() retornou " + totalMes + " despesas");
            
            System.out.println("Testando calcularTotalDespesasDoMes()...");
            double totalValor = controller.calcularTotalDespesasDoMes();
            System.out.println("✅ calcularTotalDespesasDoMes() retornou R$ " + totalValor);
            
            System.out.println("Testando obterDadosGraficoPorCategoria()...");
            int totalDadosGrafico = controller.obterDadosGraficoPorCategoria().size();
            System.out.println("✅ obterDadosGraficoPorCategoria() retornou " + totalDadosGrafico + " registros");
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar DespesaController: " + e.getMessage());
            e.printStackTrace();
        }
    }
}