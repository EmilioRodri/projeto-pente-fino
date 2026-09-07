package com.auditoria.service;

import com.auditoria.model.ResultadoAuditoria;

public interface NotificacaoGateway {
    void gerarNotificacao(ResultadoAuditoria resultado);
}