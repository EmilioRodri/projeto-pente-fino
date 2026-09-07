package com.auditoria.dto;

public record ResultadoAuditoriaDTO(
    String cnpj,
    String razaoSocial,
    String diferencaFormatada,
    String status,
    String motivo
) {}