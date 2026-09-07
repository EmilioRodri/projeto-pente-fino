package com.auditoria.service.regras;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;

import java.math.BigDecimal;

public class RegraDistribuicaoDisfarcadaLucros implements RegraAuditoria {

    @Override
    public ResultadoAuditoria analisar(Dipj empresa, Dimof banco, Bem bem) {
        if (bem == null || bem.valorAquisicao().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // Usa o método gerado pelo record: despesasOperacionais()
        BigDecimal lucroDeclarado = empresa.receitaBrutaDeclarada().subtract(empresa.despesasOperacionais());

        if (lucroDeclarado.compareTo(bem.valorAquisicao()) < 0) {
            
            BigDecimal diferenca = bem.valorAquisicao().subtract(lucroDeclarado.max(BigDecimal.ZERO));

            return new ResultadoAuditoria(
                empresa.cnpj(),
                empresa.razaoSocial(),
                empresa.receitaBrutaDeclarada(),
                (banco != null) ? banco.getTotalMovimentado() : BigDecimal.ZERO,
                diferenca,
                StatusAuditoria.MALHA_FINA,
                "INDÍCIO DE DISTRIBUIÇÃO DISFARÇADA DE LUCROS (DDL): " +
                "Aquisição de bem patrimonial (" + bem.descricao() + " no valor de R$ " + bem.valorAquisicao() + 
                ") incompatível com o lucro declarado no exercício contábil (R$ " + lucroDeclarado + "). " +
                "Possível confusão patrimonial e uso da pessoa jurídica para custeio de despesas dos sócios."
            );
        }

        return null;
    }
}