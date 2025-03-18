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
 * Classe para testes e diagnóstico do banco de dados.
 * NOTA: Esta classe deve ser usada apenas em ambiente de desenvolvimento.
 */
public class DebugTest {

    private static final String SQL_LISTAR_DESPESAS = 
            "SELECT d.id, d.descricao, d.valor, d.data_compra, d.data_vencimento, d.pago, " +
            "c.nome as categoria_nome " +
            "FROM despesas d " +
            "LEFT JOIN categorias c ON d.categoria_id = c.id " +
            "ORDER BY d.id DESC";
    
    private static final String SQL_INSERIR_DESPESA = 
            "INSERT INTO despesas (descricao, valor, data_compra, data_vencimento, pago, fixo, categoria_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * Método principal para execução dos testes.
     */
    public static void main(String[] args) {
        executarDiagnostico();
    }
    
    /**
     * Executa todos os testes de diagnóstico.
     */
    public static void executarDiagnostico() {
        System.out.println("\n=== INICIANDO DIAGNÓSTICO DO BANCO DE DADOS ===");
        
        try {
            // Verificar conexão
            verificarConexao();
            
            // Verificar integridade das tabelas
            ConexaoBanco.verificarIntegridadeBanco();
            
            // Testar inserção de despesa
            testarInsercaoDespesa();
            
            // Testar listagem de despesas
            testarListagemDespesas();
            
            // Testar controladores
            testarControladores();
            
            System.out.println("\n=== DIAGNÓSTICO CONCLUÍDO COM SUCESSO ===");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRO DURANTE O DIAGNÓSTICO: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fechar conexão
            ConexaoBanco.fecharConexao();
        }
    }
    
    /**
     * Verifica a conexão com o banco de dados.
     */
    private static void verificarConexao() throws SQLException {
        System.out.println("\n--- Verificando conexão com o banco de dados ---");
        
        Connection conn = ConexaoBanco.getConexao();
        if (conn != null && !conn.isClosed()) {
            System.out.println("✅ Conexão com o banco estabelecida com sucesso!");
        } else {
            System.out.println("❌ Falha ao conectar ao banco de dados!");
            throw new SQLException("Não foi possível estabelecer conexão com o banco de dados");
        }
    }
    
    /**
     * Testa a inserção de uma despesa no banco de dados.
     */
    private static void testarInsercaoDespesa() {
        System.out.println("\n--- Testando inserção de despesa ---");
        
        try {
            // Obter uma categoria existente
            CategoriaController categoriaController = new CategoriaController();
            List<CategoriaDespesa> categorias = new ArrayList<>(categoriaController.listarTodasCategorias());
            
            if (categorias.isEmpty()) {
                System.out.println("❌ Não há categorias disponíveis para o teste!");
                return;
            }
            
            CategoriaDespesa categoria = categorias.get(0);
            System.out.println("Usando categoria: " + categoria.getNome() + " (ID: " + categoria.getId() + ")");
            
            // Criar e salvar despesa usando o controlador
            Despesa despesa = criarDespesaTeste(categoria);
            
            DespesaController despesaController = new DespesaController();
            DespesaController.Resultado resultado = despesaController.salvarDespesa(despesa);
            
            if (resultado.isSucesso()) {
                System.out.println("✅ Despesa de teste criada com sucesso! ID: " + despesa.getId());
            } else {
                System.out.println("❌ Falha ao criar despesa via controlador: " + resultado.getMensagem());
                // Tentar inserção direta
                inserirDespesaDireta(despesa);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar inserção de despesa: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cria um objeto Despesa para testes.
     */
    private static Despesa criarDespesaTeste(CategoriaDespesa categoria) {
        Despesa despesa = new Despesa();
        despesa.setDescricao("Teste diagnóstico - " + System.currentTimeMillis());
        despesa.setValor(100.00);
        despesa.setDataCompra(LocalDate.now());
        despesa.setDataVencimento(LocalDate.now().plusDays(10));
        despesa.setPago(false);
        despesa.setFixo(false);
        despesa.setCategoria(categoria);
        return despesa;
    }
    
    /**
     * Insere uma despesa diretamente no banco de dados.
     */
    private static void inserirDespesaDireta(Despesa despesa) {
        System.out.println("\n--- Tentando inserção direta no banco ---");
        
        try (Connection conn = ConexaoBanco.getConexao();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERIR_DESPESA, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, despesa.getDescricao() + " (INSERÇÃO DIRETA)");
            stmt.setDouble(2, despesa.getValor());
            stmt.setString(3, despesa.getDataCompra().toString());
            stmt.setString(4, despesa.getDataVencimento().toString());
            stmt.setBoolean(5, despesa.isPago());
            stmt.setBoolean(6, despesa.isFixo());
            stmt.setInt(7, despesa.getCategoria().getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        System.out.println("✅ Despesa inserida diretamente com sucesso! ID: " + id);
                    } else {
                        System.out.println("⚠️ Despesa inserida, mas não foi possível obter o ID.");
                    }
                }
            } else {
                System.out.println("❌ Falha ao inserir despesa diretamente no banco.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro SQL ao inserir despesa diretamente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Testa a listagem de despesas do banco de dados.
     */
    private static void testarListagemDespesas() {
        System.out.println("\n--- Testando listagem de despesas ---");
        
        try (Connection conn = ConexaoBanco.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_LISTAR_DESPESAS)) {
            
            int contador = 0;
            while (rs.next()) {
                contador++;
                // Mostrar apenas as 5 primeiras despesas para manter a saída compacta
                if (contador <= 5) {
                    System.out.println(contador + ". ID: " + rs.getInt("id") + 
                                      ", Descrição: " + rs.getString("descricao") + 
                                      ", Valor: R$ " + rs.getDouble("valor") + 
                                      ", Categoria: " + rs.getString("categoria_nome"));
                }
            }
            
            if (contador == 0) {
                System.out.println("❌ Nenhuma despesa encontrada no banco de dados!");
            } else {
                if (contador > 5) {
                    System.out.println("... mais " + (contador - 5) + " despesa(s)");
                }
                System.out.println("✅ Total de " + contador + " despesas encontradas no banco!");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar despesas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Testa os principais controladores do sistema.
     */
    private static void testarControladores() {
        System.out.println("\n--- Testando controladores ---");
        
        testarDespesaController();
        testarCategoriaController();
    }
    
    /**
     * Testa o DespesaController.
     */
    private static void testarDespesaController() {
        System.out.println("\nTestando DespesaController...");
        
        DespesaController controller = new DespesaController();
        
        try {
            // Testar listarTodasDespesas
            int totalTodas = controller.listarTodasDespesas().size();
            System.out.println("✅ listarTodasDespesas() retornou " + totalTodas + " despesas");
            
            // Testar listarDespesasDoMes
            int totalMes = controller.listarDespesasDoMes().size();
            System.out.println("✅ listarDespesasDoMes() retornou " + totalMes + " despesas");
            
            // Testar calcularTotalDespesasDoMes
            double totalValor = controller.calcularTotalDespesasDoMes();
            System.out.println("✅ calcularTotalDespesasDoMes() retornou R$ " + totalValor);
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar DespesaController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Testa o CategoriaController.
     */
    private static void testarCategoriaController() {
        System.out.println("\nTestando CategoriaController...");
        
        CategoriaController controller = new CategoriaController();
        
        try {
            // Testar listarTodasCategorias
            int total = controller.listarTodasCategorias().size();
            System.out.println("✅ listarTodasCategorias() retornou " + total + " categorias");
            
            // Testar categorias com subcategorias
            int categoriasComSubcategorias = 0;
            int totalSubcategorias = 0;
            
            for (CategoriaDespesa categoria : controller.listarTodasCategorias()) {
                int subCategorias = categoria.getSubCategorias().size();
                if (subCategorias > 0) {
                    categoriasComSubcategorias++;
                    totalSubcategorias += subCategorias;
                }
            }
            
            System.out.println("✅ " + categoriasComSubcategorias + " categorias possuem " + 
                              totalSubcategorias + " subcategorias no total");
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao testar CategoriaController: " + e.getMessage());
            e.printStackTrace();
        }
    }
}