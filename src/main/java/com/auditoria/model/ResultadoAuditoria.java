package com.auditoria.model;

import java.math.BigDecimal;

public record ResultadoAuditoria(
    String cnpj,
    String razaoSocial,
    BigDecimal declarado,
    BigDecimal movimentado,
    BigDecimal diferenca,
    StatusAuditoria status,
    String observacao
) {
    public enum StatusAuditoria {
        REGULAR,
        MALHA_FINA 
    }
}