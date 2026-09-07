package com.auditoria.service.regras;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import java.math.BigDecimal;

public class RegraOmissaoReceita implements RegraAuditoria {

    @Override
    public ResultadoAuditoria analisar(Dipj empresa, Dimof banco, Bem bem) {
        BigDecimal receitaDeclarada = empresa.receitaBrutaDeclarada();
        BigDecimal movimentadoBanco = (banco != null) ? banco.getTotalMovimentado() : BigDecimal.ZERO;

        // Se o banco for MAIOR que o declarado, é malha fina
        if (movimentadoBanco.compareTo(receitaDeclarada) > 0) {
            BigDecimal diferenca = movimentadoBanco.subtract(receitaDeclarada);
            
            return new ResultadoAuditoria(
                empresa.cnpj(),
                empresa.razaoSocial(),
                receitaDeclarada,
                movimentadoBanco,
                diferenca,
                StatusAuditoria.MALHA_FINA,
                "Infração detectada: Omissão de Receita. A movimentação financeira via DIMOF (Bancos) superou a receita declarada na DIPJ."
            );
        }
        
        // Passou no teste, retorna null para o motor seguir para a próxima regra
        return null; 
    }
}