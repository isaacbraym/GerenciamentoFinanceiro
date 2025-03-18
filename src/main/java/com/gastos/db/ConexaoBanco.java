package com.gastos.db;

import java.sql.*;
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
    public static synchronized Connection getConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            inicializarBancoDeDados();
        }
        return conexao;
    }

    /**
     * Fecha a conexão com o banco de dados.
     */
    public static synchronized void fecharConexao() {
        if (conexao != null) {
            try {
                if (!conexao.isClosed()) {
                    if (!conexao.getAutoCommit()) {
                        conexao.commit();
                        conexao.setAutoCommit(true);
                    }
                    conexao.close();
                    System.out.println("Conexão com o banco de dados fechada.");
                }
                conexao = null;
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    /**
     * Inicializa o banco de dados criando as tabelas necessárias.
     */
    public static void inicializarBancoDeDados() throws SQLException {
        // Verificar se o banco de dados já existe
        File dbFile = new File("gerenciador_financeiro.db");
        boolean dbExistia = dbFile.exists();

        try {
            // Registrar driver JDBC do SQLite
            Class.forName("org.sqlite.JDBC");
            conexao = DriverManager.getConnection(URL);
            conexao.setAutoCommit(false);

            // Ativar PRAGMAs para melhorar o desempenho e integridade
            ativarPragmas();

            // Log de status
            System.out.println(dbExistia ? "Usando banco existente." : "Criando novo banco.");

            // Se o banco for novo, inicialize o banco de dados com as tabelas e dados iniciais
            if (!dbExistia) {
                inicializarTabelas();
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
            throw new SQLException("Erro ao inicializar a conexão com o banco de dados.", e);
        }
    }

    /**
     * Ativa configurações do banco (PRAGMAs) para otimizar o desempenho.
     */
    private static void ativarPragmas() throws SQLException {
        try (Statement stmt = conexao.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA busy_timeout = 30000");
        }
    }

    /**
     * Inicializa as tabelas no banco de dados, caso sejam necessárias.
     */
    private static void inicializarTabelas() throws SQLException {
        String[] tabelas = {
            "CREATE TABLE IF NOT EXISTS categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS subcategorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, categoria_id INTEGER NOT NULL, FOREIGN KEY (categoria_id) REFERENCES categorias(id))",
            "CREATE TABLE IF NOT EXISTS responsaveis (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS meios_pagamento (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, cartao_credito BOOLEAN NOT NULL)",
            "CREATE TABLE IF NOT EXISTS cartoes_credito (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL, bandeira TEXT NOT NULL, limite REAL NOT NULL, dia_fechamento INTEGER NOT NULL, dia_vencimento INTEGER NOT NULL, cor TEXT)",
            "CREATE TABLE IF NOT EXISTS parcelamentos (id INTEGER PRIMARY KEY AUTOINCREMENT, valor_total REAL NOT NULL, total_parcelas INTEGER NOT NULL, parcelas_restantes INTEGER NOT NULL, data_inicio TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS parcelas (id INTEGER PRIMARY KEY AUTOINCREMENT, parcelamento_id INTEGER NOT NULL, numero_parcela INTEGER NOT NULL, valor REAL NOT NULL, data_vencimento TEXT NOT NULL, paga BOOLEAN NOT NULL, FOREIGN KEY (parcelamento_id) REFERENCES parcelamentos(id))",
            "CREATE TABLE IF NOT EXISTS despesas (id INTEGER PRIMARY KEY AUTOINCREMENT, descricao TEXT NOT NULL, valor REAL NOT NULL, data_compra TEXT NOT NULL, data_vencimento TEXT, pago BOOLEAN NOT NULL, fixo BOOLEAN NOT NULL, categoria_id INTEGER NOT NULL, subcategoria_id INTEGER, responsavel_id INTEGER, meio_pagamento_id INTEGER, cartao_id INTEGER, parcelamento_id INTEGER, FOREIGN KEY (categoria_id) REFERENCES categorias(id), FOREIGN KEY (subcategoria_id) REFERENCES subcategorias(id), FOREIGN KEY (responsavel_id) REFERENCES responsaveis(id), FOREIGN KEY (meio_pagamento_id) REFERENCES meios_pagamento(id), FOREIGN KEY (cartao_id) REFERENCES cartoes_credito(id), FOREIGN KEY (parcelamento_id) REFERENCES parcelamentos(id))"
        };

        try (Statement stmt = conexao.createStatement()) {
            for (String tabela : tabelas) {
                stmt.execute(tabela);
            }
            conexao.commit();
            System.out.println("Tabelas criadas/verificadas com sucesso!");
        }
    }

    /**
     * Reseta o banco de dados (para fins de depuração ou reinicialização).
     */
    public static void resetarBancoDados() {
        try {
            fecharConexao();
            File dbFile = new File("gerenciador_financeiro.db");

            if (dbFile.exists()) {
                if (dbFile.delete()) {
                    System.out.println("Arquivo de banco de dados excluído com sucesso.");
                } else {
                    System.out.println("Falha ao excluir o arquivo do banco de dados.");
                }
            }

            getConexao(); // Reinicializa a conexão com um banco vazio
        } catch (SQLException e) {
            System.err.println("Erro ao reiniciar o banco de dados: " + e.getMessage());
        }
    }

    /**
     * Verifica a integridade do banco de dados.
     */
    public static void verificarIntegridadeBanco() {
        try (Statement stmt = conexao.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
            System.out.println("Tabelas no banco de dados:");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("name"));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar integridade: " + e.getMessage());
        }
    }
}
