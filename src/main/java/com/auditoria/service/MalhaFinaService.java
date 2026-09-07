package com.auditoria.service;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import com.auditoria.service.regras.RegraAuditoria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MalhaFinaService {

    private final List<RegraAuditoria> regras;

    // É exatamente este construtor que resolve o seu erro no DashboardApp
    public MalhaFinaService(List<RegraAuditoria> regras) {
        this.regras = regras;
    }

    public List<ResultadoAuditoria> processar(List<Dipj> empresas, List<Dimof> bancos, List<Bem> bens) {
        List<ResultadoAuditoria> resultados = new ArrayList<>();

        for (Dipj empresa : empresas) {
            // Busca o banco e o bem correspondentes ao CNPJ da empresa atual
            Dimof banco = bancos.stream()
                    .filter(b -> b.cnpj().equals(empresa.cnpj()))
                    .findFirst()
                    .orElse(null);

            Bem bem = bens.stream()
                    .filter(b -> b.cnpj().equals(empresa.cnpj()))
                    .findFirst()
                    .orElse(null);

            boolean caiuNaMalha = false;

            // Motor de Regras (Strategy): percorre todas as infrações cadastradas
            for (RegraAuditoria regra : regras) {
                ResultadoAuditoria autuacao = regra.analisar(empresa, banco, bem);
                
                if (autuacao != null) {
                    resultados.add(autuacao);
                    caiuNaMalha = true;
                    break; // Para na primeira infração encontrada
                }
            }

            // Se passou ileso por todas as regras, recebe o status REGULAR
            if (!caiuNaMalha) {
                BigDecimal movimentado = (banco != null) ? banco.getTotalMovimentado() : BigDecimal.ZERO;
                
                resultados.add(new ResultadoAuditoria(
                    empresa.cnpj(),
                    empresa.razaoSocial(),
                    empresa.receitaBrutaDeclarada(),
                    movimentado,
                    BigDecimal.ZERO,
                    StatusAuditoria.REGULAR,
                    "Auditoria Concluída sem divergências."
                ));
            }
        }

        return resultados;
    }
}