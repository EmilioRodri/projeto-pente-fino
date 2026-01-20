package com.auditoria.service;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class MalhaFinaServiceTest {

    private final MalhaFinaService service = new MalhaFinaService();

    @Test
    @DisplayName("Deve detectar Omissão de Receita (Banco > Declaração)")
    void deveDetectarSonegacaoBancaria() {
        Dipj empresa = new Dipj("11.111/0001", "Sonega Ltda", 2024, new BigDecimal("100000"), BigDecimal.ZERO);
        Dimof banco = new Dimof("11.111/0001", "Banco X", new BigDecimal("150000"), BigDecimal.ZERO);

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), Collections.emptyList()
        );

        Assertions.assertEquals(StatusAuditoria.MALHA_FINA, resultado.get(0).status());
        Assertions.assertEquals(new BigDecimal("50000"), resultado.get(0).diferenca());
    }

    @Test
    @DisplayName("Deve detectar Variação Patrimonial a Descoberto")
    void deveDetectarVariacaoPatrimonial() {
        // Lucro de 10k
        Dipj empresa = new Dipj("22.222/0002", "Laranja Ltda", 2024, new BigDecimal("20000"), new BigDecimal("10000"));
        // Banco ok
        Dimof banco = new Dimof("22.222/0002", "Banco Y", new BigDecimal("20000"), BigDecimal.ZERO);
        // Comprou Ferrari de 100k
        Bem carro = new Bem("22.222/0002", "Ferrari", new BigDecimal("100000"));

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), List.of(carro)
        );

        Assertions.assertEquals(StatusAuditoria.MALHA_FINA, resultado.get(0).status());
        Assertions.assertTrue(resultado.get(0).observacao().contains("VARIAÇÃO PATRIMONIAL"));
    }

    @Test
    @DisplayName("Deve aprovar empresa regular (Sem divergências)")
    void deveAprovarEmpresaRegular() {
        // Lucro de 30k (Receita 50k - Despesa 20k)
        Dipj empresa = new Dipj("33.333/0003", "Certinha Ltda", 2024, new BigDecimal("50000"), new BigDecimal("20000"));
        Dimof banco = new Dimof("33.333/0003", "Banco Z", new BigDecimal("50000"), BigDecimal.ZERO);
        // Comprou bem de 10k (compatível com o lucro de 30k)
        Bem mesa = new Bem("33.333/0003", "Mesa Escritório", new BigDecimal("10000"));

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), List.of(mesa)
        );

        Assertions.assertEquals(StatusAuditoria.REGULAR, resultado.get(0).status());
        Assertions.assertEquals(BigDecimal.ZERO, resultado.get(0).diferenca());
    }
}