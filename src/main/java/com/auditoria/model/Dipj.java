package com.auditoria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Dipj(
    String cnpj,
    String razaoSocial,
    Integer anoBase,
    BigDecimal receitaBrutaDeclarada,
    BigDecimal despesasOperacionais
) {
    public boolean isPrejuizo() {
        return despesasOperacionais.compareTo(receitaBrutaDeclarada) > 0;
    }
}