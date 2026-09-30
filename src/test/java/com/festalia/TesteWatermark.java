package com.festalia;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;

public class TesteWatermark {
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE DE MARCA D'AGUA ===");
            File imgFile = new File("src/main/resources/festalia_logo.jpg");
            System.out.println("Imagem existe: " + imgFile.exists() + " (" + imgFile.getAbsolutePath() + ")");
            
            ImageData imgData = ImageDataFactory.create(imgFile.getAbsolutePath());
            System.out.println("ImageData carregada com sucesso! W=" + imgData.getWidth() + ", H=" + imgData.getHeight());

            File saida = new File("saida_testes/teste_wm.pdf");
            if (!saida.getParentFile().exists()) saida.getParentFile().mkdirs();

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new FileOutputStream(saida)));
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("TESTE DE CONTEUDO DO PDF COM MARCA D'AGUA").setFontSize(20).setBold());
            for (int i = 0; i < 20; i++) {
                doc.add(new Paragraph("Linha de teste numero " + i + " para verificar sobreposicao da marca d'agua."));
            }

            // Aplicar marca d'água em cada página existente
            int total = pdfDoc.getNumberOfPages();
            System.out.println("Total de paginas no documento: " + total);

            for (int p = 1; p <= total; p++) {
                PdfPage page = pdfDoc.getPage(p);
                Rectangle pageSize = page.getPageSize();
                System.out.println("Pagina " + p + ": W=" + pageSize.getWidth() + ", H=" + pageSize.getHeight());

                // Desenha no fundo (newContentStreamBefore)
                PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
                PdfExtGState gs = new PdfExtGState();
                gs.setFillOpacity(0.20f); // 20% de opacidade bem visível
                canvas.setExtGState(gs);

                float size = 400f;
                float x = (pageSize.getWidth() - size) / 2f;
                float y = (pageSize.getHeight() - size) / 2f;

                canvas.addImageFittedIntoRectangle(imgData, new Rectangle(x, y, size, size), false);
                canvas.release(); // ESSENCIAL!
                System.out.println("Marca d'agua desenhada e liberada na pagina " + p);
            }

            doc.close();
            pdfDoc.close();
            System.out.println("PDF gerado com sucesso! Tamanho final: " + saida.length() + " bytes");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
