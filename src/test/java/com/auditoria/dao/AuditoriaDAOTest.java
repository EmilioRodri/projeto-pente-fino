package com.auditoria.dao;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditoriaDAOTest {

    private AuditoriaDAO dao;

    @BeforeAll
    static void setupGlobal() {
        // 1. Redireciona a conexão para o banco em memória (H2)
        Conexao.configurarParaTestes();
    }

    @BeforeEach
    void setupBancoDeDados() throws Exception {
        dao = new AuditoriaDAO();
        
        // 2. Cria as tabelas limpas antes de cada teste rodar
        try (Connection conn = Conexao.getConexao();
             Statement stmt = conn.createStatement()) {
             
            stmt.execute("DROP ALL OBJECTS"); 
            
            stmt.execute("CREATE TABLE empresas (cnpj VARCHAR(18) PRIMARY KEY, razao_social VARCHAR(100), ano_exercicio INT, receita_declarada DECIMAL(15,2), despesa_declarada DECIMAL(15,2))");
            stmt.execute("CREATE TABLE contas_bancarias (cnpj_empresa VARCHAR(18), nome_banco VARCHAR(50), total_movimentado DECIMAL(15,2))");
            stmt.execute("CREATE TABLE bens (cnpj_empresa VARCHAR(18), nome_bem VARCHAR(50), valor_pago DECIMAL(15,2))");
        }
    }

    @Test
    void deveSalvarERecuperarDadosDaEmpresaCorretamente() {
        // Cenário (Arrange)
        Dipj empresaFake = new Dipj("11.111.111/0001-11", "Empresa Sonegação LTDA", 2026, new BigDecimal("100000.00"), new BigDecimal("50000.00"));
        Dimof bancoFake = new Dimof("11.111.111/0001-11", "Banco Teste", new BigDecimal("150000.00"), BigDecimal.ZERO);
        Bem bemFake = new Bem("11.111.111/0001-11", "Carro de Luxo", new BigDecimal("200000.00"));

        // Ação (Act)
        dao.salvarSimulacao(empresaFake, bancoFake, bemFake);
        List<Dipj> listaSalva = dao.listarEmpresas();

        // Verificação (Assert)
        assertEquals(1, listaSalva.size(), "Deve retornar exatamente 1 empresa salva");
        assertEquals("11.111.111/0001-11", listaSalva.get(0).cnpj());
        
        // Compara ignorando diferenças de casas decimais (100000.00 vs 100000)
        assertEquals(0, new BigDecimal("100000.00").compareTo(listaSalva.get(0).receitaBrutaDeclarada()));
    }
}