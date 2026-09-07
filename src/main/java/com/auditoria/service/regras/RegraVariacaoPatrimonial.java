package com.auditoria.service.regras;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import java.math.BigDecimal;

public class RegraVariacaoPatrimonial implements RegraAuditoria {

    @Override
    public ResultadoAuditoria analisar(Dipj empresa, Dimof banco, Bem bem) {
        BigDecimal receitaDeclarada = empresa.receitaBrutaDeclarada();
        BigDecimal valorBens = (bem != null) ? bem.valorAquisicao() : BigDecimal.ZERO;

        // Se o valor de compra do bem for MAIOR que a receita do ano, é malha fina
        if (valorBens.compareTo(receitaDeclarada) > 0) {
            BigDecimal diferenca = valorBens.subtract(receitaDeclarada);
            
            return new ResultadoAuditoria(
                empresa.cnpj(),
                empresa.razaoSocial(),
                receitaDeclarada,
                valorBens,
                diferenca,
                StatusAuditoria.MALHA_FINA,
                "Infração detectada: Variação Patrimonial a Descoberto. A aquisição de bens superou o lastro da receita declarada na DIPJ."
            );
        }
        
        // Passou no teste
        return null;
    }
}