package com.auditoria.dao;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO implements AuditoriaRepository {

    public List<Dipj> listarEmpresas() {
        List<Dipj> lista = new ArrayList<>();
        String sql = "SELECT * FROM empresas";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Dipj(
                    rs.getString("cnpj"),
                    rs.getString("razao_social"),
                    rs.getInt("ano_exercicio"),
                    rs.getBigDecimal("receita_declarada"),
                    rs.getBigDecimal("despesa_declarada")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar empresas: " + e.getMessage());
        }
        return lista;
    }

    public List<Dimof> listarBancos() {
        List<Dimof> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas_bancarias";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Dimof(
                    rs.getString("cnpj_empresa"),
                    rs.getString("nome_banco"),
                    rs.getBigDecimal("total_movimentado"), 
                    BigDecimal.ZERO                       
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar bancos: " + e.getMessage());
        }
        return lista;
    }

    public List<Bem> listarBens() {
        List<Bem> lista = new ArrayList<>();
        String sql = "SELECT * FROM bens";

        try (Connection conn = Conexao.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Bem(
                    rs.getString("cnpj_empresa"),
                    rs.getString("nome_bem"),
                    rs.getBigDecimal("valor_pago")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar bens: " + e.getMessage());
        }
        return lista;
    }

    public void salvarSimulacao(Dipj empresa, Dimof banco, Bem bem) {
        String sqlEmpresa = "INSERT INTO empresas (cnpj, razao_social, ano_exercicio, receita_declarada, despesa_declarada) VALUES (?, ?, ?, ?, ?)";
        String sqlBanco = "INSERT INTO contas_bancarias (cnpj_empresa, nome_banco, total_movimentado) VALUES (?, ?, ?)";
        String sqlBem = "INSERT INTO bens (cnpj_empresa, nome_bem, valor_pago) VALUES (?, ?, ?)";

        try (Connection conn = Conexao.getConexao()) {
            conn.setAutoCommit(false); 

            try (PreparedStatement stmt = conn.prepareStatement(sqlEmpresa)) {
                stmt.setString(1, empresa.cnpj());
                stmt.setString(2, empresa.razaoSocial());
                stmt.setInt(3, empresa.anoBase());
                stmt.setBigDecimal(4, empresa.receitaBrutaDeclarada());
                stmt.setBigDecimal(5, empresa.despesasOperacionais());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlBanco)) {
                stmt.setString(1, banco.cnpj());
                stmt.setString(2, banco.instituicaoFinanceira());
                stmt.setBigDecimal(3, banco.getTotalMovimentado());
                stmt.executeUpdate();
            }

            if (bem.valorAquisicao().compareTo(BigDecimal.ZERO) > 0) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlBem)) {
                    stmt.setString(1, bem.cnpj());
                    stmt.setString(2, bem.descricao());
                    stmt.setBigDecimal(3, bem.valorAquisicao());
                    stmt.executeUpdate();
                }
            }

            conn.commit(); 
            System.out.println("Simulação salva no banco com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar simulação: " + e.getMessage());
        }
    }
}