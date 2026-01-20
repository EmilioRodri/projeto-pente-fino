package com.auditoria.service;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MalhaFinaService {

    public List<ResultadoAuditoria> processar(List<Dipj> declaracoes, 
                                              List<Dimof> financeiros, 
                                              List<Bem> listaBens) {
        
        List<ResultadoAuditoria> relatorio = new ArrayList<>();

        Map<String, Dimof> mapFinanceiro = financeiros.stream()
            .collect(Collectors.toMap(Dimof::cnpj, d -> d));

        // 2. Indexar Bens (Map<String, List<Bem>> porque uma empresa pode comprar vários bens)
        Map<String, List<Bem>> mapBens = listaBens.stream()
            .collect(Collectors.groupingBy(Bem::cnpj));

        // 3. Cruzar tudo baseando-se na declaração (DIPJ)
        for (Dipj empresa : declaracoes) {
            
            Dimof banco = mapFinanceiro.get(empresa.cnpj());
            List<Bem> bensAdquiridos = mapBens.getOrDefault(empresa.cnpj(), List.of());

            relatorio.add(auditarEmpresa(empresa, banco, bensAdquiridos));
        }

        return relatorio;
    }

    private ResultadoAuditoria auditarEmpresa(Dipj empresa, Dimof banco, List<Bem> bens) {
        BigDecimal declarado = empresa.receitaBrutaDeclarada();
        BigDecimal despesas = empresa.despesasOperacionais();
        
        BigDecimal movimentado = (banco != null) ? banco.getTotalMovimentado() : BigDecimal.ZERO;
        if (movimentado.compareTo(declarado) > 0) {
            return criarMalha(empresa, declarado, movimentado, 
                "OMISSÃO DE RECEITA: Movimentação bancária superior ao declarado.");
        }

        BigDecimal lucroDisponivel = declarado.subtract(despesas);
        
        BigDecimal totalGastoBens = bens.stream()
            .map(Bem::valorAquisicao)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalGastoBens.compareTo(lucroDisponivel) > 0) {
            String listaDosBens = bens.stream().map(Bem::descricao).collect(Collectors.joining(", "));
            BigDecimal diferenca = totalGastoBens.subtract(lucroDisponivel);
            
            return criarMalha(empresa, declarado, totalGastoBens, 
                "VARIAÇÃO PATRIMONIAL A DESCOBERTO: Adquiriu bens (" + listaDosBens + 
                ") sem lastro financeiro (Lucro Insuficiente). Diferença: R$ " + diferenca);
        }

        return new ResultadoAuditoria(
            empresa.cnpj(), empresa.razaoSocial(), declarado, movimentado, BigDecimal.ZERO,
            StatusAuditoria.REGULAR, "Auditoria Concluída sem divergências."
        );
    }

    private ResultadoAuditoria criarMalha(Dipj emp, BigDecimal decl, BigDecimal mov, String motivo) {
        return new ResultadoAuditoria(
            emp.cnpj(), emp.razaoSocial(), decl, mov, mov.subtract(decl),
            StatusAuditoria.MALHA_FINA, motivo
        );
    }
}