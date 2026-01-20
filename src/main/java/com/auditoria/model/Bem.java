package com.auditoria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Bem(
    String cnpj,
    String descricao,
    BigDecimal valorAquisicao
) {}