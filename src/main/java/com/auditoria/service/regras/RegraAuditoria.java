package com.auditoria.service.regras;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;

public interface RegraAuditoria {
    // Retorna um ResultadoAuditoria se achar infração, ou null se estiver tudo certo
    ResultadoAuditoria analisar(Dipj empresa, Dimof banco, Bem bem);
}