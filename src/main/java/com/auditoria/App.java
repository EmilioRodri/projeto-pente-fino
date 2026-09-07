package com.auditoria;

import com.auditoria.dao.AuditoriaDAO;
import com.auditoria.dao.AuditoriaRepository;
import com.auditoria.model.Bem;
import com.auditoria.model.Dimof;
import com.auditoria.model.Dipj;
import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.service.MalhaFinaService;
import com.auditoria.service.regras.RegraOmissaoReceita;
import com.auditoria.service.regras.RegraVariacaoPatrimonial;

import java.util.List;

public class App {
    public static void main(String[] args) {
        // 1. Composition Root: Monta as dependências e injeta as regras
        AuditoriaRepository dao = new AuditoriaDAO();
        MalhaFinaService auditoriaService = new MalhaFinaService(
            List.of(new RegraOmissaoReceita(), new RegraVariacaoPatrimonial())
        );

        System.out.println("Iniciando processamento em lote (Modo Terminal)...");

        // 2. Busca os dados no MySQL (via HikariCP)
        List<Dipj> empresas = dao.listarEmpresas();
        List<Dimof> bancos = dao.listarBancos();
        List<Bem> bens = dao.listarBens();

        // 3. Processa a malha fina
        List<ResultadoAuditoria> resultados = auditoriaService.processar(empresas, bancos, bens);

        // 4. Exibe os resultados no console
        System.out.println("\n--- RESULTADO DA AUDITORIA ---");
        for (ResultadoAuditoria resultado : resultados) {
            System.out.println("CNPJ: " + resultado.cnpj() + " | Status: " + resultado.status() + " | Motivo: " + resultado.observacao());
        }
        
        System.out.println("\nProcessamento concluído com sucesso.");
    }
}