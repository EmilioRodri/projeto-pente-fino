package com.auditoria.dao;

import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import java.util.List;

public interface AuditoriaRepository {
    List<Dipj> listarEmpresas();
    List<Dimof> listarBancos();
    List<Bem> listarBens();
    void salvarSimulacao(Dipj empresa, Dimof banco, Bem bem);
}