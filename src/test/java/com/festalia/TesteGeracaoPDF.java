package com.festalia;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class TesteGeracaoPDF {
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando teste de geração de PDF...");
            Cardapio cardapio = new Cardapio(1, "Casamento Nobre & Glamour", "Menu completo para recepção", 190.00,
                    List.of("Canapés de brie com geleia de pimenta", "Filet Mignon ao molho madeira com risoto trufado", "Mesa de doces finos"));

            // 100 convidados a R$ 185,00 por pessoa, Taxa de Serviço de 10% sobre valor/pessoa, Taxa de Transporte R$ 500,00
            double precoPorPessoa = 185.00;
            int convidados = 100;
            double taxaServicoPercentual = 10.0;
            double taxaTransporte = 500.00;
            double subtotal = convidados * precoPorPessoa; // 18.500,00
            double valorTaxaServico = subtotal * (taxaServicoPercentual / 100.0); // 1.850,00
            double total = subtotal + valorTaxaServico + taxaTransporte; // 20.850,00

            OrdemServico os = new OrdemServico(
                    1,
                    1001,
                    "Buffet Imperial & Eventos",
                    "Excelência em Gastronomia e Recepções Sociais",
                    "Mariana & Lucas Siqueira",
                    "(11) 98765-4321",
                    LocalDate.now().plusDays(30),
                    "Villa Real Eventos - Salão Cristal",
                    convidados,
                    precoPorPessoa,
                    taxaServicoPercentual,
                    taxaTransporte,
                    total,
                    "Confirmado",
                    cardapio,
                    "Necessário fornecer 10 opções vegetarianas. Montagem com 4h de antecedência.",
                    null
            );

            File pastaSaida = new File("saida_testes");
            if (!pastaSaida.exists()) {
                pastaSaida.mkdirs();
            }

            String destino = "saida_testes/OS_1001_Teste_MarcaDagua.pdf";
            PDFGenerator.gerarOrdemServicoPDF(os, destino);

            File pdfGerado = new File(destino);
            System.out.println("PDF gerado com sucesso! Tamanho: " + pdfGerado.length() + " bytes. Caminho: " + pdfGerado.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
