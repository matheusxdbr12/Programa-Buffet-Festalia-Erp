package com.festalia;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;

public class PDFGenerator {

    public static void gerarOrdemServicoPDF(OrdemServico os, String caminhoDestino) throws Exception {
        PdfWriter writer = new PdfWriter(new File(caminhoDestino));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Cabeçalho da Ordem de Serviço
        document.add(new Paragraph("FESTALIA ERP - ORDEM DE SERVIÇO")
                .setFontSize(18)
                .setBold());
        document.add(new Paragraph("OS Nº: " + os.getNumeroOS()));
        document.add(new Paragraph("--------------------------------------------------------------------------------------------------"));

        // Dados do Evento e Cliente
        document.add(new Paragraph("Cliente: " + os.getCliente()));
        document.add(new Paragraph("Telefone: " + os.getTelefone()));
        document.add(new Paragraph("Data do Evento: " + os.getDataEvento()));
        document.add(new Paragraph("Local: " + os.getLocal()));
        document.add(new Paragraph("Qtd. Convidados: " + os.getQuantidadeConvidados()));
        document.add(new Paragraph("\n"));

        // Detalhes do Menu/Cardápio
        document.add(new Paragraph("CARDÁPIO SELECIONADO: " + os.getCardapioSelecionado().getNome())
                .setBold());
        document.add(new Paragraph("Descrição: " + os.getCardapioSelecionado().getDescricao()));

        document.add(new Paragraph("\nItens do Menu:"));
        Table tabelaItens = new Table(1);
        tabelaItens.setWidth(UnitValue.createPercentValue(100));

        for (String item : os.getCardapioSelecionado().getItens()) {
            tabelaItens.addCell(item);
        }
        document.add(tabelaItens);

        // Observações Gerais
        if (os.getObservacoes() != null && !os.getObservacoes().isBlank()) {
            document.add(new Paragraph("\nObservações do Evento:").setBold());
            document.add(new Paragraph(os.getObservacoes()));
        }

        document.close();
    }
}