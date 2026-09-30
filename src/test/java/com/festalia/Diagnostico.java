package com.festalia;

import java.io.File;
import java.io.FileInputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfPage;

public class Diagnostico {
    public static void main(String[] args) {
        try {
            File f = new File("saida_testes/OS_1001_Teste_MarcaDagua.pdf");
            System.out.println("Arquivo existe: " + f.exists() + ", tamanho: " + f.length());
            
            PdfReader reader = new PdfReader(f);
            PdfDocument doc = new PdfDocument(reader);
            System.out.println("Total de paginas: " + doc.getNumberOfPages());
            PdfPage page = doc.getPage(1);
            System.out.println("Recursos da pagina: " + page.getResources().getResourceNames());
            System.out.println("XObjects: " + page.getResources().getResource(com.itextpdf.kernel.pdf.PdfName.XObject));
            doc.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
