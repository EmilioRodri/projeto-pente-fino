package com.auditoria.service;

import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.util.Formatador;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RelatorioPdfService implements NotificacaoGateway {

    @Override
    public void gerarNotificacao(ResultadoAuditoria resultado) {
        Document documento = new Document();
        
        try {
            // 1. Verifica se a pasta existe. Se não, o Java cria na hora.
            String nomeDiretorio = "autos_infracao";
            File diretorio = new File(nomeDiretorio);
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }

            // 2. Monta o caminho completo do arquivo (Ex: autos_infracao/Auto_Infracao_111.pdf)
            String cnpjLimpo = resultado.cnpj().replaceAll("[^0-9]", "");
            String caminhoArquivo = nomeDiretorio + File.separator + "Auto_Infracao_" + cnpjLimpo + ".pdf";
            
            PdfWriter.getInstance(documento, new FileOutputStream(caminhoArquivo));
            documento.open();

            // 3. Inserção da Logo da Receita Federal
            try {
                URL urlImagem = getClass().getResource("/img/logo.png");
                if (urlImagem != null) {
                    Image logo = Image.getInstance(urlImagem);
                    logo.scaleToFit(100, 100);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    documento.add(logo);
                }
            } catch (Exception e) {
                System.err.println("Aviso: Logo não encontrada, gerando sem imagem no PDF.");
            }

            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            Font fonteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);
            Font fonteTexto = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font fonteAlerta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED);

            // Cabeçalho Oficial
            Paragraph orgao = new Paragraph("MINISTÉRIO DA FAZENDA\nSECRETARIA DA RECEITA FEDERAL DO BRASIL", fonteSubtitulo);
            orgao.setAlignment(Element.ALIGN_CENTER);
            documento.add(orgao);
            documento.add(new Paragraph("\n"));

            Paragraph titulo = new Paragraph("AUTO DE INFRAÇÃO E NOTIFICAÇÃO FISCAL", fonteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph("\n\n"));

            // Identificação do Contribuinte
            documento.add(new Paragraph("1. IDENTIFICAÇÃO DO CONTRIBUINTE", fonteSubtitulo));
            documento.add(new Paragraph("Razão Social: " + resultado.razaoSocial(), fonteTexto));
            documento.add(new Paragraph("CNPJ: " + resultado.cnpj(), fonteTexto));
            documento.add(new Paragraph("\n"));

            // Demonstrativo Matemático
            documento.add(new Paragraph("2. DEMONSTRATIVO DE APURAÇÃO", fonteSubtitulo));
            documento.add(new Paragraph("Receita Declarada (DIPJ): " + Formatador.moeda(resultado.declarado()), fonteTexto));
            documento.add(new Paragraph("Movimentação Apurada (Cruzamento): " + Formatador.moeda(resultado.movimentado()), fonteTexto));
            documento.add(new Paragraph("Divergência Detectada: " + Formatador.moeda(resultado.diferenca()), fonteAlerta));
            documento.add(new Paragraph("\n"));

            // Fundamentação Legal
            documento.add(new Paragraph("3. FUNDAMENTAÇÃO LEGAL E PENALIDADE", fonteSubtitulo));
            documento.add(new Paragraph(resultado.observacao(), fonteTexto));
            documento.add(new Paragraph("\n\n\n"));

            // Data e Assinatura
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Paragraph data = new Paragraph("Documento emitido digitalmente pelo Sistema Malha Fina em " + LocalDateTime.now().format(formatter), fonteTexto);
            data.setAlignment(Element.ALIGN_CENTER);
            documento.add(data);

            Paragraph assinatura = new Paragraph("___________________________________________________\nAuditor-Fiscal da Receita Federal do Brasil", fonteTexto);
            assinatura.setAlignment(Element.ALIGN_CENTER);
            assinatura.setSpacingBefore(40);
            documento.add(assinatura);

        } catch (Exception e) {
            System.err.println("Erro grave na emissão do Auto de Infração: " + e.getMessage());
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }
}