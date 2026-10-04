package com.auditoria.service.regras;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;

public class RegraOmissaoReceitaTest {

    @Test
    public void deveDetetarInfracaoQuandoMovimentacaoForMaiorQueReceitaDeclarada() {
        // 1. Arrange
        RegraAuditoria regra = new RegraOmissaoReceita();
        
        Dipj empresa = new Dipj(
            "12345678000199", 
            "Empresa Teste LTDA", 
            2026, 
            new BigDecimal("50000.00"),  // Declaração menor (50k)
            new BigDecimal("0.00")
        );

        // Somando crédito (40k) e débito (40k), o Total Movimentado será 80.000.00
        Dimof banco = new Dimof(
            "12345678000199",
            "Banco Santander",
            new BigDecimal("40000.00"), 
            new BigDecimal("40000.00")  
        ); 

        Bem bem = null;

        // 2. Act
        ResultadoAuditoria resultado = regra.analisar(empresa, banco, bem);

        // 3. Assert
        assertNotNull(resultado, "A regra deve detetar a omissão de receita e devolver infração.");
    }

    @Test
    public void naoDeveDetetarInfracaoQuandoValoresEstiveremCorretos() {
        // 1. Arrange
        RegraAuditoria regra = new RegraOmissaoReceita();
        
        Dipj empresa = new Dipj(
            "12345678000199", 
            "Empresa Teste LTDA", 
            2026, 
            new BigDecimal("80000.00"), // Declaração correta (80k)
            new BigDecimal("0.00")
        );

        Dimof banco = new Dimof(
            "12345678000199",
            "Banco Santander",
            new BigDecimal("40000.00"),
            new BigDecimal("40000.00")
        );

        Bem bem = null;

        // 2. Act
        ResultadoAuditoria resultado = regra.analisar(empresa, banco, bem);

        // 3. Assert
        assertNull(resultado, "O resultado deve ser null quando não há infração.");
    }
}