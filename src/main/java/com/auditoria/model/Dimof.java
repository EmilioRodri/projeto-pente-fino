package com.auditoria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Dimof(
    String cnpj,
    String instituicaoFinanceira,
    BigDecimal totalVendasCredito,
    BigDecimal totalVendasDebito
) {
    public BigDecimal getTotalMovimentado() {
        return totalVendasCredito.add(totalVendasDebito);
    }
}