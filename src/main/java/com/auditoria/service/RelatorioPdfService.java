package com.auditoria.service;

import com.auditoria.model.ResultadoAuditoria;
import com.auditoria.util.Formatador;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RelatorioPdfService {

    public void gerarNotificacao(ResultadoAuditoria auditoria) {
        String nomeArquivo = "Auto_Infracao_" + auditoria.cnpj().replaceAll("\\D", "") + ".pdf";
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(nomeArquivo));
            documento.open();

            try {
                URL urlImagem = getClass().getResource("/img/logo.png");
                if (urlImagem != null) {
                    Image logo = Image.getInstance(urlImagem);
                    logo.scaleToFit(100, 100);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    documento.add(logo);
                }
            } catch (Exception e) {
                System.err.println("Aviso: Logo não encontrada, gerando sem imagem.");
            }

            Paragraph orgao = new Paragraph("MINISTÉRIO DA FAZENDA\nSECRETARIA DA RECEITA FEDERAL DO BRASIL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            orgao.setAlignment(Element.ALIGN_CENTER);
            orgao.setSpacingAfter(20);
            documento.add(orgao);

            documento.add(new Paragraph("NOTIFICAÇÃO DE LANÇAMENTO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            documento.add(new Paragraph("Data de Emissão: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            documento.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));


            documento.add(new Paragraph("\nCONTRIBUINTE: " + auditoria.razaoSocial()));
            documento.add(new Paragraph("CNPJ: " + auditoria.cnpj()));
            documento.add(new Paragraph("\n"));

           
            PdfPTable tabela = new PdfPTable(2);
            tabela.setWidthPercentage(100);
            tabela.addCell(criarCelula("Receita Declarada:", true));
            tabela.addCell(criarCelula(Formatador.moeda(auditoria.declarado()), false));
            
            tabela.addCell(criarCelula("Valor Real Apurado:", true));
            tabela.addCell(criarCelula(Formatador.moeda(auditoria.movimentado()), false));
            
            tabela.addCell(criarCelula("Divergência / Omissão:", true));
            tabela.addCell(criarCelula(Formatador.moeda(auditoria.diferenca()), false));
            
            documento.add(tabela);

           
            Font fonteVermelha = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.RED);
            documento.add(new Paragraph("\nIRREGULARIDADE CONSTATADA:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            documento.add(new Paragraph(auditoria.observacao(), fonteVermelha));

            documento.add(new Paragraph("\n"));
            documento.add(new Paragraph("CÁLCULO DA MULTA (75% - Art. 44 Lei 9.430/96)", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            
            java.math.BigDecimal multa = auditoria.diferenca().multiply(new java.math.BigDecimal("0.75"));
            documento.add(new Paragraph("Valor Total a Pagar: " + Formatador.moeda(multa), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

            documento.add(new Paragraph("\n\nDocumento gerado eletronicamente pelo Sistema Pente Fino.", FontFactory.getFont(FontFactory.COURIER, 9)));

            documento.close();
            System.out.println("PDF gerado com sucesso: " + nomeArquivo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private com.itextpdf.text.pdf.PdfPCell criarCelula(String texto, boolean negrito) {
        Font font = negrito ? FontFactory.getFont(FontFactory.HELVETICA_BOLD) : FontFactory.getFont(FontFactory.HELVETICA);
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new Phrase(texto, font));
        cell.setPadding(8);
        return cell;
    }
}