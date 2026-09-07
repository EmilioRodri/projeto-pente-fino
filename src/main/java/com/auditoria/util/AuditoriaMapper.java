package com.auditoria.util;

import com.auditoria.dto.ResultadoAuditoriaDTO;
import com.auditoria.model.ResultadoAuditoria;

public class AuditoriaMapper {
    public static ResultadoAuditoriaDTO paraDTO(ResultadoAuditoria resultado) {
        return new ResultadoAuditoriaDTO(
            resultado.cnpj(),
            resultado.razaoSocial(),
            Formatador.moeda(resultado.diferenca()),
            resultado.status().toString(),
            resultado.observacao()
        );
    }
}