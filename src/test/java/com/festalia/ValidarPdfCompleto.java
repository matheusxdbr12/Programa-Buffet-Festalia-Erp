package com.festalia;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfDictionary;

public class ValidarPdfCompleto {
    public static void main(String[] args) {
        try {
            System.out.println("--- INICIANDO VALIDAÇÃO DO PDF COM MARCA D'ÁGUA ---");
            Cardapio cardapio = new Cardapio(1, "Casamento Real & Nobre", "Buffet sofisticado para grandes celebrações", 185.00,
                    List.of("Canapés finos", "Tournedor de Filet Mignon", "Mesa de Sobremesas artesanais"));

            OrdemServico os = new OrdemServico(
                    1,
                    1003,
                    "Banco Master",
                    "Roubar na amizade",
                    "Gilmar Mender",
                    "81 969696969",
                    LocalDate.of(2026, 9, 29),
                    "Espaço Festalia Garden - Salão Nobre",
                    100,
                    185.00,
                    10.0, // 10% de taxa de serviço sobre o valor por pessoa
                    1000.00, // taxa de transporte
                    0.0,
                    "Orçamento",
                    cardapio,
                    "Evento corporativo exclusivo.",
                    null
            );

            File pasta = new File("saida_pdf_valido");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File pdfSaida = new File(pasta, "OS_1003_Validado.pdf");
            PDFGenerator.gerarOrdemServicoPDF(os, pdfSaida.getAbsolutePath());

            System.out.println("PDF gerado em: " + pdfSaida.getAbsolutePath());
            System.out.println("Tamanho do arquivo: " + pdfSaida.length() + " bytes");

            // Inspeção dos streams e objetos do PDF
            PdfReader reader = new PdfReader(pdfSaida);
            PdfDocument doc = new PdfDocument(reader);
            System.out.println("Total de páginas: " + doc.getNumberOfPages());

            boolean encontrouImagem = false;
            for (int p = 1; p <= doc.getNumberOfPages(); p++) {
                PdfPage page = doc.getPage(p);
                PdfDictionary pageDict = page.getPdfObject();
                PdfDictionary res = pageDict.getAsDictionary(PdfName.Resources);
                if (res != null) {
                    PdfDictionary xobjects = res.getAsDictionary(PdfName.XObject);
                    if (xobjects != null) {
                        for (PdfName key : xobjects.keySet()) {
                            PdfDictionary xobj = xobjects.getAsDictionary(key);
                            if (xobj != null && PdfName.Image.equals(xobj.getAsName(PdfName.Subtype))) {
                                encontrouImagem = true;
                                System.out.println("Página " + p + ": Encontrou /XObject /Image -> " + key.getValue());
                            }
                        }
                    }
                }
            }
            doc.close();
            reader.close();

            if (encontrouImagem) {
                System.out.println(">>> SUCESSO ABSOLUTO: A MARCA D'ÁGUA FOI INSERIDA E DETECTADA NO PDF! <<<");
            } else {
                System.err.println(">>> FALHA: A MARCA D'ÁGUA NÃO FOI ENCONTRADA! <<<");
                System.exit(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
