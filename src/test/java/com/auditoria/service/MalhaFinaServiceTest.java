package com.auditoria.service;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import com.auditoria.service.regras.RegraDistribuicaoDisfarcadaLucros;
import com.auditoria.service.regras.RegraOmissaoReceita;
import com.auditoria.service.regras.RegraVariacaoPatrimonial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class MalhaFinaServiceTest {

    private MalhaFinaService service;

    @BeforeEach
    void setup() {
        // AQUI ESTÁ A CHAVE: As TRÊS regras sendo injetadas no motor para os testes
        service = new MalhaFinaService(List.of(
            new RegraOmissaoReceita(),
            new RegraVariacaoPatrimonial(),
            new RegraDistribuicaoDisfarcadaLucros()
        ));
    }

    @Test
    @DisplayName("Deve detectar Omissão de Receita (Banco > Declaração)")
    void deveDetectarSonegacaoBancaria() {
        Dipj empresa = new Dipj("11.111/0001", "Sonega Ltda", 2026, new BigDecimal("100000"), BigDecimal.ZERO);
        Dimof banco = new Dimof("11.111/0001", "Banco X", new BigDecimal("150000"), BigDecimal.ZERO);

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), Collections.emptyList()
        );

        Assertions.assertEquals(StatusAuditoria.MALHA_FINA, resultado.get(0).status());
        Assertions.assertEquals(0, new BigDecimal("50000").compareTo(resultado.get(0).diferenca()));
    }

    @Test
    @DisplayName("Deve detectar Variação Patrimonial a Descoberto")
    void deveDetectarVariacaoPatrimonial() {
        Dipj empresa = new Dipj("22.222/0002", "Laranja Ltda", 2026, new BigDecimal("20000"), new BigDecimal("10000"));
        Dimof banco = new Dimof("22.222/0002", "Banco Y", new BigDecimal("20000"), BigDecimal.ZERO);
        Bem carro = new Bem("22.222/0002", "Ferrari", new BigDecimal("100000"));

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), List.of(carro)
        );

        Assertions.assertEquals(StatusAuditoria.MALHA_FINA, resultado.get(0).status());
        Assertions.assertTrue(resultado.get(0).observacao().contains("Variação Patrimonial"));
    }

    @Test
    @DisplayName("Deve aprovar empresa regular (Sem divergências)")
    void deveAprovarEmpresaRegular() {
        Dipj empresa = new Dipj("33.333/0003", "Certinha Ltda", 2026, new BigDecimal("50000"), new BigDecimal("20000"));
        Dimof banco = new Dimof("33.333/0003", "Banco Z", new BigDecimal("50000"), BigDecimal.ZERO);
        Bem mesa = new Bem("33.333/0003", "Mesa Escritório", new BigDecimal("10000"));

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), List.of(mesa)
        );

        Assertions.assertEquals(StatusAuditoria.REGULAR, resultado.get(0).status());
        Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(resultado.get(0).diferenca()));
    }

    @Test
    @DisplayName("Deve detectar Distribuição Disfarçada de Lucros (DDL)")
    void deveDetectarDistribuicaoDisfarcadaDeLucros() {
        // Cenário isolado para DDL: Receita alta cobre o bem, mas o lucro é zero (Receita == Despesa).
        Dipj empresa = new Dipj("44.444/0004", "Sócio Esperto LTDA", 2026, new BigDecimal("150000"), new BigDecimal("150000"));
        Dimof banco = new Dimof("44.444/0004", "Banco W", new BigDecimal("150000"), BigDecimal.ZERO);
        
        // A empresa teve lucro zero, mas comprou um bem de 50 mil. Confusão patrimonial detectada.
        Bem lancha = new Bem("44.444/0004", "Lancha Offshore", new BigDecimal("50000"));

        List<ResultadoAuditoria> resultado = service.processar(
            List.of(empresa), List.of(banco), List.of(lancha)
        );

        Assertions.assertFalse(resultado.isEmpty(), "Deveria ter gerado autuação");

        boolean ddlAcionado = resultado.stream()
            .anyMatch(r -> r.observacao().contains("DDL"));

        Assertions.assertTrue(ddlAcionado, "O motor falhou ao detectar a Distribuição Disfarçada de Lucros");
    }
}