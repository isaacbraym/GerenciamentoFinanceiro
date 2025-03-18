package com.gastos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

/**
 * Classe responsável por gerenciar a conexão com o banco de dados SQLite.
 */
public class ConexaoBanco {
    private static final String URL = "jdbc:sqlite:gerenciador_financeiro.db";
    private static Connection conexao;
    
    /**
     * Obtém uma conexão com o banco de dados.
     * @return uma conexão com o banco de dados
     * @throws SQLException se ocorrer um erro de SQL
     */
    public static Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            // Verificar se o arquivo de banco de dados existe
            File dbFile = new File("gerenciador_financeiro.db");
            boolean dbExistia = dbFile.exists();
            
            // Estabelecer conexão
            conexao = DriverManager.getConnection(URL);
            
            // Ativar chaves estrangeiras
            Statement stmt = conexao.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = WAL"); // Melhora a concorrência
            stmt.close();
            
            // Log
            System.out.println("Conexão estabelecida com o banco de dados. " + 
                              (dbExistia ? "Usando banco existente." : "Criando novo banco."));
            
            // Se o banco foi recém-criado, inicializá-lo
            if (!dbExistia) {
                inicializarBancoDados();
            }
        }
        return conexao;
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     */
    public static void fecharConexao() {
        if (conexao != null) {
            try {
                conexao.close();
                conexao = null; // Importante: garantir que a referência seja nula
                System.out.println("Conexão com o banco de dados fechada.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão com banco de dados: " + e.getMessage());
            }
        }
    }
    
    /**
     * Inicializa o banco de dados criando as tabelas necessárias.
     */
    public static void inicializarBancoDados() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConexao();
            stmt = conn.createStatement();
            
            System.out.println("Iniciando criação/verificação de tabelas...");
            
            // Cria a tabela de categorias
            stmt.execute("CREATE TABLE IF NOT EXISTS categorias (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL)");
            
            // Cria a tabela de subcategorias
            stmt.execute("CREATE TABLE IF NOT EXISTS subcategorias (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL, " +
                         "categoria_id INTEGER NOT NULL, " +
                         "FOREIGN KEY (categoria_id) REFERENCES categorias(id))");
            
            // Cria a tabela de responsáveis
            stmt.execute("CREATE TABLE IF NOT EXISTS responsaveis (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL)");
            
            // Cria a tabela de meios de pagamento
            stmt.execute("CREATE TABLE IF NOT EXISTS meios_pagamento (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL, " +
                         "cartao_credito BOOLEAN NOT NULL)");
            
            // Cria a tabela de cartões de crédito
            stmt.execute("CREATE TABLE IF NOT EXISTS cartoes_credito (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL, " +
                         "bandeira TEXT NOT NULL, " +
                         "limite REAL NOT NULL, " +
                         "dia_fechamento INTEGER NOT NULL, " +
                         "dia_vencimento INTEGER NOT NULL, " +
                         "cor TEXT)");
            
            // Cria a tabela de parcelamentos
            stmt.execute("CREATE TABLE IF NOT EXISTS parcelamentos (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "valor_total REAL NOT NULL, " +
                         "total_parcelas INTEGER NOT NULL, " +
                         "parcelas_restantes INTEGER NOT NULL, " +
                         "data_inicio TEXT NOT NULL)");
            
            // Cria a tabela de parcelas
            stmt.execute("CREATE TABLE IF NOT EXISTS parcelas (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "parcelamento_id INTEGER NOT NULL, " +
                         "numero_parcela INTEGER NOT NULL, " +
                         "valor REAL NOT NULL, " +
                         "data_vencimento TEXT NOT NULL, " +
                         "paga BOOLEAN NOT NULL, " +
                         "FOREIGN KEY (parcelamento_id) REFERENCES parcelamentos(id))");
            
            // Cria a tabela de despesas
            stmt.execute("CREATE TABLE IF NOT EXISTS despesas (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "descricao TEXT NOT NULL, " +
                         "valor REAL NOT NULL, " +
                         "data_compra TEXT NOT NULL, " +
                         "data_vencimento TEXT, " +
                         "pago BOOLEAN NOT NULL, " +
                         "fixo BOOLEAN NOT NULL, " +
                         "categoria_id INTEGER NOT NULL, " +
                         "subcategoria_id INTEGER, " +
                         "responsavel_id INTEGER, " +
                         "meio_pagamento_id INTEGER, " +
                         "cartao_id INTEGER, " +
                         "parcelamento_id INTEGER, " +
                         "FOREIGN KEY (categoria_id) REFERENCES categorias(id), " +
                         "FOREIGN KEY (subcategoria_id) REFERENCES subcategorias(id), " +
                         "FOREIGN KEY (responsavel_id) REFERENCES responsaveis(id), " +
                         "FOREIGN KEY (meio_pagamento_id) REFERENCES meios_pagamento(id), " +
                         "FOREIGN KEY (cartao_id) REFERENCES cartoes_credito(id), " +
                         "FOREIGN KEY (parcelamento_id) REFERENCES parcelamentos(id))");
            
            System.out.println("Tabelas criadas/verificadas com sucesso!");
            
            // Listar as tabelas existentes para debug
            ResultSet tables = conn.getMetaData().getTables(null, null, "%", null);
            System.out.println("Tabelas existentes no banco:");
            while (tables.next()) {
                System.out.println(" - " + tables.getString("TABLE_NAME"));
            }
            tables.close();
            
            // Verificar e inserir dados iniciais
            inserirDadosIniciais(conn);
            
            System.out.println("Banco de dados inicializado com sucesso!");
            
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Não fechamos a conexão aqui pois ela pode ser reutilizada
        }
    }
    
    /**
     * Insere dados iniciais no banco de dados.
     * @param conn a conexão com o banco de dados
     * @throws SQLException se ocorrer um erro de SQL
     */
    private static void inserirDadosIniciais(Connection conn) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = conn.createStatement();
            
            // Contar categorias existentes
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM categorias");
            rs.next();
            int totalCategorias = rs.getInt("total");
            System.out.println("Total de categorias existentes: " + totalCategorias);
            
            // Se não existirem categorias, insere os dados iniciais
            if (totalCategorias == 0) {
                System.out.println("Inserindo dados iniciais no banco de dados...");
                
                // Categorias
                String[] categorias = {"Casa", "Alimentação", "Transporte", "Saúde", "Educação", "Lazer", "Pets", "Compras Online"};
                for (String categoria : categorias) {
                    stmt.execute("INSERT INTO categorias (nome) VALUES ('" + categoria + "')");
                    System.out.println("Categoria inserida: " + categoria);
                }
                
                // Verificar se as categorias foram inseridas
                rs.close();
                rs = stmt.executeQuery("SELECT * FROM categorias");
                System.out.println("Categorias após inserção:");
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " - " + rs.getString("nome"));
                }
                
                // Subcategorias
                String[][] subcategorias = {
                    {"Aluguel", "Água", "Luz", "Gás", "Internet", "TV", "Condomínio", "IPTU", "Manutenção"}, // Casa
                    {"Supermercado", "Restaurante", "Delivery", "Padaria", "Lanche"}, // Alimentação
                    {"Combustível", "Estacionamento", "Transporte Público", "Uber/99", "Manutenção", "IPVA", "Seguro"}, // Transporte
                    {"Plano de Saúde", "Consultas", "Exames", "Medicamentos", "Terapia"}, // Saúde
                    {"Mensalidade", "Material Escolar", "Cursos", "Livros"}, // Educação
                    {"Cinema", "Teatro", "Shows", "Viagens", "Assinaturas"}, // Lazer
                    {"Ração", "Veterinário", "Medicamentos", "Petshop", "Brinquedos"}, // Pets
                    {"Roupas", "Eletrônicos", "Presentes", "Livros", "Outros"} // Compras Online
                };
                
                // Obter IDs das categorias recém-inseridas
                rs.close();
                rs = stmt.executeQuery("SELECT id, nome FROM categorias ORDER BY id");
                int[] categoriaIds = new int[categorias.length];
                int index = 0;
                while (rs.next() && index < categorias.length) {
                    categoriaIds[index++] = rs.getInt("id");
                }
                
                // Inserir subcategorias com os IDs corretos
                for (int i = 0; i < categoriaIds.length; i++) {
                    int categoriaId = categoriaIds[i];
                    for (String subcategoria : subcategorias[i]) {
                        stmt.execute("INSERT INTO subcategorias (nome, categoria_id) VALUES ('" + subcategoria + "', " + categoriaId + ")");
                    }
                }
                
                // Meios de pagamento
                String[] meiosPagamento = {"Dinheiro", "Débito", "Crédito", "Pix", "Boleto", "Transferência"};
                boolean[] isCartaoCredito = {false, false, true, false, false, false};
                for (int i = 0; i < meiosPagamento.length; i++) {
                    stmt.execute("INSERT INTO meios_pagamento (nome, cartao_credito) VALUES ('" + meiosPagamento[i] + "', " + isCartaoCredito[i] + ")");
                }
                
                // Cartões de crédito de exemplo
                String[][] cartoes = {
                    {"Nubank", "Mastercard", "#8A05BE"}, 
                    {"Itaú", "Visa", "#EC7000"}, 
                    {"Inter", "Mastercard", "#FF7A00"},
                    {"Mercado Pago", "Mastercard", "#00B1EA"}
                };
                
                for (String[] cartao : cartoes) {
                    stmt.execute("INSERT INTO cartoes_credito (nome, bandeira, limite, dia_fechamento, dia_vencimento, cor) " +
                                 "VALUES ('" + cartao[0] + "', '" + cartao[1] + "', 5000.0, 20, 3, '" + cartao[2] + "')");
                }
                
                // Responsáveis de exemplo
                String[] responsaveis = {"João", "Maria"};
                for (String responsavel : responsaveis) {
                    stmt.execute("INSERT INTO responsaveis (nome) VALUES ('" + responsavel + "')");
                }
                
                System.out.println("Dados iniciais inseridos com sucesso!");
            } else {
                System.out.println("O banco de dados já contém dados. Pulando a inserção de dados iniciais.");
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
    /**
     * Método para forçar a reinicialização do banco de dados (para debug)
     */
    public static void resetarBancoDados() {
        try {
            // Fechar conexão existente
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                conexao = null;
            }
            
            // Excluir o arquivo do banco de dados
            File dbFile = new File("gerenciador_financeiro.db");
            if (dbFile.exists()) {
                if (dbFile.delete()) {
                    System.out.println("Arquivo do banco de dados excluído com sucesso.");
                } else {
                    System.out.println("Falha ao excluir o arquivo do banco de dados.");
                    // Tentar forçar a liberação de recursos
                    System.gc();
                    Thread.sleep(1000);
                    if (dbFile.delete()) {
                        System.out.println("Arquivo do banco de dados excluído com sucesso após forçar liberação.");
                    } else {
                        System.out.println("Falha persistente ao excluir o arquivo do banco de dados. Verifique se ele está em uso por outro processo.");
                    }
                }
            }
            
            // Reinicializar o banco de dados
            getConexao(); // Isso vai chamar inicializarBancoDados() para um novo banco
            
        } catch (SQLException e) {
            System.err.println("Erro ao resetar banco de dados: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Interrupção durante espera para exclusão de arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}