package com.auditoria.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Conexao {
    
    // Declaração do dataSource que estava faltando
    private static HikariDataSource dataSource;

    // Bloco executado na primeira vez que a classe é chamada
    static {
        String url = "jdbc:mysql://localhost:3306/malhafina?serverTimezone=America/Sao_Paulo&createDatabaseIfNotExist=true";
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "SUA_SENHA_LOCAL_AQUI");
        
        inicializarHikari(url, user, pass);
    }

    private static void inicializarHikari(String url, String user, String pass) {
        if (dataSource != null) dataSource.close();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url); 
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(10); 
        config.setMinimumIdle(2);      
        config.setConnectionTimeout(30000);
        
        dataSource = new HikariDataSource(config);
    }

    private Conexao() {}

    public static Connection getConexao() throws SQLException {
        return dataSource.getConnection();
    }
    
    // Método exclusivo para injeção de testes em memória (H2)
    public static void configurarParaTestes() {
        inicializarHikari("jdbc:h2:mem:malhafina;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }
}