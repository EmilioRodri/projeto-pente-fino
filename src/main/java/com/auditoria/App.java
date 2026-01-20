package com.auditoria;

import com.auditoria.dao.AuditoriaDAO; 
import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.model.ResultadoAuditoria.StatusAuditoria;
import com.auditoria.service.MalhaFinaService;
import com.auditoria.service.RelatorioPdfService;
import com.auditoria.util.Formatador;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("=== MOTOR DE MALHA FINA (CONECTADO AO MYSQL) ===");
        
        AuditoriaDAO dao = new AuditoriaDAO();

        System.out.println(">> Buscando dados no banco de dados...");
        List<Dipj> empresas = dao.listarEmpresas();
        List<Dimof> cartoes = dao.listarBancos();
        List<Bem> bens = dao.listarBens();

        MalhaFinaService auditoria = new MalhaFinaService();
        List<ResultadoAuditoria> resultados = auditoria.processar(empresas, cartoes, bens);
        
        RelatorioPdfService pdfService = new RelatorioPdfService();

        System.out.println("\n--- RELATÓRIO DE AUDITORIA ---");
        
        for (ResultadoAuditoria res : resultados) {
            if (res.status() == StatusAuditoria.MALHA_FINA) {
                System.out.println("------------------------------------------------");
                System.out.println(" [ALERTA] SONEGAÇÃO DETECTADA!");
                System.out.println("   Empresa: " + res.razaoSocial());
                System.out.println("   CNPJ: " + res.cnpj());
                System.out.println("   Motivo: " + res.observacao());
                System.out.println("   Divergência Apurada: " + Formatador.moeda(res.diferenca()));
                
                // Gera o PDF
                pdfService.gerarNotificacao(res);
                System.out.println("Auto de Infração gerado com sucesso.");
                
            } else {
                System.out.println(" [REGULAR] " + res.razaoSocial() + " - Nada consta.");
            }
        }
        System.out.println("------------------------------------------------");
        System.out.println("Fim do processamento.");
    }
}