package com.festalia;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

public class TesteMendezLogoWatermark {

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando teste de geração de OS com logomarca Mendez Buffet...");

            File dirSaida = new File("saida_testes");
            if (!dirSaida.exists()) {
                dirSaida.mkdirs();
            }

            // Garante cópia do asset Mendez Logo
            BuffetErpApp.garantirAssetsIniciais();

            Cardapio cardapio = new Cardapio(1, "Churrasco Prime & Chopp", "Serviço completo de churrasco nobre e acompanhamentos", 145.00,
                    Arrays.asList(
                            "Picanha nobre maturada ao sal grosso",
                            "Costela bovina prime de 12 horas",
                            "Fraldinha na mostarda e ervas finas",
                            "Linguiça artesanal cuiabana com queijo coalho",
                            "Arroz carreteiro gaúcho e farofa de ovos caipiras",
                            "Mandioca cozida com manteiga de garrafa",
                            "Chopp artesanal pilsen e chopp ipa à vontade",
                            "Refrigerantes linha premium e água mineral"
                    ));

            String caminhoLogoMendez = "mendez_logo.png";
            File fLogo = new File(caminhoLogoMendez);
            if (!fLogo.exists()) {
                caminhoLogoMendez = "C:/Users/matheus.souza/.gemini/antigravity-ide/brain/6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61/.user_uploaded/media_1790705846854.jpg";
            }

            OrdemServico os = new OrdemServico(
                    1,
                    2001,
                    "Mendez Buffet",
                    "Gastronomia e Eventos de Alto Padrão",
                    caminhoLogoMendez,
                    "Empresa Exemplo & Associados",
                    "(11) 98765-4321",
                    LocalDate.now().plusDays(15),
                    "Espaço Vila dos Ipês - Salão Nobre",
                    120,
                    145.00,
                    10.0, // 10% de taxa de serviço
                    350.00, // R$ 350 de transporte
                    0.0, // cálculo automático
                    "Confirmado",
                    cardapio,
                    "Montagem das mesas às 16h00. Evento corporativo de confraternização anual com chopp gelado.",
                    LocalDateTime.now()
            );

            String pdfDestino = "saida_testes/OS_2001_Mendez_Buffet_MarcaDagua.pdf";
            PDFGenerator.gerarOrdemServicoPDF(os, pdfDestino);

            File arquivoGerado = new File(pdfDestino);
            if (arquivoGerado.exists() && arquivoGerado.length() > 0) {
                System.out.println("SUCESSO: PDF gerado com sucesso!");
                System.out.println("Caminho: " + arquivoGerado.getAbsolutePath());
                System.out.println("Tamanho: " + arquivoGerado.length() + " bytes");
            } else {
                System.err.println("ERRO: O arquivo PDF não foi gerado ou está vazio.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
