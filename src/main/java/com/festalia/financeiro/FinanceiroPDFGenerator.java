package com.festalia.financeiro;

import com.festalia.OrdemServico;
import com.festalia.PDFGenerator;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gerador de Documentos Financeiros Oficiais em PDF (Recibos e Termos de Quitação).
 * Utiliza a mesma identidade visual, cabeçalho e marca d'água translúcida da Ordem de Serviço.
 */
public class FinanceiroPDFGenerator {

    private static final DeviceRgb COR_PRIMARIA = new DeviceRgb(41, 128, 185);      // Azul Festalia
    private static final DeviceRgb COR_SECUNDARIA = new DeviceRgb(52, 73, 94);     // Cinza azulado
    private static final DeviceRgb COR_VERDE_PAGO = new DeviceRgb(22, 101, 52);     // Verde elegante
    private static final DeviceRgb COR_FUNDO_DESTAQUE = new DeviceRgb(240, 253, 244); // Verde sutil
    private static final DeviceRgb COR_FUNDO_CINZA = new DeviceRgb(248, 250, 252);

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Emite o Recibo de Pagamento individual de uma parcela.
     */
    public static void gerarReciboPagamentoPDF(OrdemServico os, ParcelaOS parcela,
                                              PagamentoParcela pagamento,
                                              double saldoRestanteParcela,
                                              double saldoRestanteOS,
                                              String caminhoDestino) throws IOException {
        if (os == null || parcela == null || pagamento == null) {
            throw new IllegalArgumentException("Dados da OS, parcela e pagamento são obrigatórios.");
        }

        File arquivoDestino = new File(caminhoDestino);
        if (arquivoDestino.getParentFile() != null && !arquivoDestino.getParentFile().exists()) {
            arquivoDestino.getParentFile().mkdirs();
        }

        ImageData logoWatermark = PDFGenerator.carregarImageDataLogo(os.getCaminhoLogoBuffet());

        try (FileOutputStream fos = new FileOutputStream(arquivoDestino);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document document = new Document(pdfDoc);
            document.setMargins(36, 40, 36, 40);

            // 1. Cabeçalho com Logo e Título Oficial do Buffet
            adicionarCabecalhoBuffet(document, os, "RECIBO DE PAGAMENTO", "COMPROVANTE OFICIAL DE ADIMPLEMENTO");

            // Divisor
            SolidLine divisor = new SolidLine(1.5f);
            divisor.setColor(COR_PRIMARIA);
            document.add(new LineSeparator(divisor).setMarginTop(6).setMarginBottom(14));

            // 2. Card de Destaque com o Valor Recebido e Extenso
            Table cardValor = new Table(UnitValue.createPercentArray(new float[]{100})).useAllAvailableWidth();
            cardValor.setMarginBottom(14);

            Cell cellValor = new Cell()
                    .setBackgroundColor(COR_FUNDO_DESTAQUE)
                    .setBorder(new SolidBorder(new DeviceRgb(187, 247, 208), 1.2f))
                    .setPadding(12);

            cellValor.add(new Paragraph("VALOR RECEBIDO:")
                    .setFontSize(10).setBold().setFontColor(COR_VERDE_PAGO));
            cellValor.add(new Paragraph(String.format("R$ %,.2f", pagamento.getValorPago()))
                    .setFontSize(22).setBold().setFontColor(COR_VERDE_PAGO));

            String extenso = NumeroPorExtensoUtil.converterParaExtenso(pagamento.getValorPago());
            cellValor.add(new Paragraph("(" + extenso + ")")
                    .setFontSize(10).setItalic().setFontColor(new DeviceRgb(21, 128, 61)));

            cardValor.addCell(cellValor);
            document.add(cardValor);

            // 3. Tabela com Dados do Pagador, Evento e Transação
            Table dadosTabela = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            dadosTabela.setMarginBottom(14);

            dadosTabela.addCell(criarCelulaInfo("CLIENTE / CONTRATANTE:", os.getCliente()));
            dadosTabela.addCell(criarCelulaInfo("ORDEM DE SERVIÇO Nº:", String.valueOf(os.getNumeroOS())));

            dadosTabela.addCell(criarCelulaInfo("TELEFONE / CONTATO:", (os.getTelefone() != null && !os.getTelefone().isBlank()) ? os.getTelefone() : "Não informado"));
            dadosTabela.addCell(criarCelulaInfo("DATA DO EVENTO:", os.getDataEventoFormatada() + " (" + os.getTipoEvento() + ")"));

            dadosTabela.addCell(criarCelulaInfo("LOCAL DO EVENTO:", (os.getLocal() != null && !os.getLocal().isBlank()) ? os.getLocal() : "Espaço Festalia"));
            dadosTabela.addCell(criarCelulaInfo("PARCELA DE REFERÊNCIA:", "Parcela #" + parcela.getNumeroParcela() + " - " + parcela.getDescricao()));

            dadosTabela.addCell(criarCelulaInfo("DATA DO PAGAMENTO:", pagamento.getDataPagamentoFormatada()));
            dadosTabela.addCell(criarCelulaInfo("FORMA DE PAGAMENTO:", pagamento.getFormaPagamento()));

            if (pagamento.getIdentificacaoTransacao() != null && !pagamento.getIdentificacaoTransacao().isBlank()) {
                dadosTabela.addCell(criarCelulaInfo("AUTENTICAÇÃO / NSU / COMPROVANTE:", pagamento.getIdentificacaoTransacao()));
            } else {
                dadosTabela.addCell(criarCelulaInfo("AUTENTICAÇÃO / COMPROVANTE:", "Transação validada internamente"));
            }
            dadosTabela.addCell(criarCelulaInfo("REGISTRADO POR:", pagamento.getUsuarioResponsavel() + " em " + pagamento.getDataRegistroFormatada()));

            document.add(dadosTabela);

            // 4. Resumo da Situação da Parcela e Saldo Restante
            Table situacaoTab = new Table(UnitValue.createPercentArray(new float[]{33.3f, 33.3f, 33.4f})).useAllAvailableWidth();
            situacaoTab.setMarginBottom(20);

            situacaoTab.addCell(criarCelulaMetrica("VALOR TOTAL DA PARCELA", parcela.getValorFormatado(), COR_SECUNDARIA));
            situacaoTab.addCell(criarCelulaMetrica("SALDO RESTANTE DA PARCELA", String.format("R$ %,.2f", saldoRestanteParcela),
                    saldoRestanteParcela <= 0.001 ? COR_VERDE_PAGO : new DeviceRgb(180, 83, 9)));
            situacaoTab.addCell(criarCelulaMetrica("SALDO TOTAL PENDENTE DA OS", String.format("R$ %,.2f", saldoRestanteOS),
                    saldoRestanteOS <= 0.001 ? COR_VERDE_PAGO : new DeviceRgb(220, 38, 38)));

            document.add(situacaoTab);

            // Observações adicionais
            if (pagamento.getObservacoes() != null && !pagamento.getObservacoes().isBlank()) {
                Paragraph pObs = new Paragraph()
                        .add(new Paragraph("Observações do Pagamento: ").setBold().setFontSize(9))
                        .add(new Paragraph(pagamento.getObservacoes()).setFontSize(9))
                        .setMarginBottom(16);
                document.add(pObs);
            }

            // 5. Bloco de Assinaturas e Data de Emissão
            adicionarBlocoAssinaturas(document, os, "Recebido por:", "Assinatura do Contratante:");

            // Marca d'água ao fundo
            if (logoWatermark != null) {
                aplicarMarcaDaguaNoFundo(pdfDoc, logoWatermark);
            }

            document.close();
        }
    }

    /**
     * Emite o Termo de Quitação Financeira Integral quando a OS não possui saldo devedor.
     */
    public static void gerarTermoQuitacaoPDF(OrdemServico os, ResumoFinanceiroOS resumo, String caminhoDestino) throws IOException {
        if (os == null || resumo == null) {
            throw new IllegalArgumentException("Dados da OS e resumo financeiro são obrigatórios.");
        }

        File arquivoDestino = new File(caminhoDestino);
        if (arquivoDestino.getParentFile() != null && !arquivoDestino.getParentFile().exists()) {
            arquivoDestino.getParentFile().mkdirs();
        }

        ImageData logoWatermark = PDFGenerator.carregarImageDataLogo(os.getCaminhoLogoBuffet());

        try (FileOutputStream fos = new FileOutputStream(arquivoDestino);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document document = new Document(pdfDoc);
            document.setMargins(36, 40, 36, 40);

            // 1. Cabeçalho Oficial
            adicionarCabecalhoBuffet(document, os, "TERMO DE QUITAÇÃO FINANCEIRA INTEGRAL", "DOCUMENTO DE ADIMPLEMENTO TOTAL CONTRATUAL");

            // Divisor
            SolidLine divisor = new SolidLine(1.5f);
            divisor.setColor(COR_PRIMARIA);
            document.add(new LineSeparator(divisor).setMarginTop(6).setMarginBottom(16));

            // 2. Declaração Jurídica Formal de Quitação
            Paragraph declaracao = new Paragraph()
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setFontSize(11)
                    .setMarginBottom(18);

            String textoDeclaracao = String.format(
                    "Declaramos para os devidos fins de direito que a empresa %s, inscrita no âmbito da prestação de serviços " +
                    "para eventos e buffet sob a Ordem de Serviço Nº %d, reconhece o recebimento integral de todos os valores " +
                    "pactuados com o(a) contratante %s, referente à realização do evento %s, previsto/realizado na data de %s, no local %s.",
                    os.getNomeBuffet(), os.getNumeroOS(), os.getCliente(), os.getTipoEvento(),
                    os.getDataEventoFormatada(), (os.getLocal() != null && !os.getLocal().isBlank() ? os.getLocal() : "Espaço Festalia")
            );

            declaracao.add(textoDeclaracao);
            document.add(declaracao);

            Paragraph declaracaoFim = new Paragraph()
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setFontSize(11)
                    .setMarginBottom(18)
                    .add("Por ser verdade, outorgamos ao(à) contratante a mais ampla, geral, rasa e irrevogável QUITAÇÃO com relação a todas as " +
                            "obrigações financeiras pactuadas para a referida contratação, nada mais havendo a reclamar ou exigir a qualquer título.");
            document.add(declaracaoFim);

            // 3. Tabela Consolidada de Liquidação
            Table cardLiquidacao = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25})).useAllAvailableWidth();
            cardLiquidacao.setMarginBottom(20);

            cardLiquidacao.addCell(criarCelulaMetrica("VALOR CONTRATADO", resumo.getValorTotalOSFormatado(), COR_SECUNDARIA));
            cardLiquidacao.addCell(criarCelulaMetrica("TOTAL EFETIVAMENTE PAGO", resumo.getTotalPagoFormatado(), COR_VERDE_PAGO));
            cardLiquidacao.addCell(criarCelulaMetrica("SALDO DEVEDOR", "R$ 0,00", COR_VERDE_PAGO));
            cardLiquidacao.addCell(criarCelulaMetrica("SITUAÇÃO CONTRATUAL", "QUITADO", COR_VERDE_PAGO));

            document.add(cardLiquidacao);

            // 4. Detalhes Adicionais
            Table detalhesTab = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            detalhesTab.setMarginBottom(24);

            detalhesTab.addCell(criarCelulaInfo("CONTRATANTE / CLIENTE:", os.getCliente()));
            detalhesTab.addCell(criarCelulaInfo("CONTRATADA (EMPRESA):", os.getNomeBuffet()));
            detalhesTab.addCell(criarCelulaInfo("QUANTIDADE DE CONVIDADOS:", os.getQuantidadeConvidados() + " pessoas"));
            detalhesTab.addCell(criarCelulaInfo("CARDÁPIO CONTRATADO:", os.getCardapioSelecionado() != null ? os.getCardapioSelecionado().getNome() : "Personalizado"));
            detalhesTab.addCell(criarCelulaInfo("DATA DE EMISSÃO DA QUITAÇÃO:", LocalDate.now().format(FORMATO_DATA)));
            detalhesTab.addCell(criarCelulaInfo("CÓDIGO DE REGISTRO DA OS:", "OS #" + os.getNumeroOS()));

            document.add(detalhesTab);

            // 5. Assinaturas
            adicionarBlocoAssinaturas(document, os, "Pela Contratada (Empresa):", "Pelo(a) Contratante:");

            // Marca d'água ao fundo
            if (logoWatermark != null) {
                aplicarMarcaDaguaNoFundo(pdfDoc, logoWatermark);
            }

            document.close();
        }
    }

    private static void adicionarCabecalhoBuffet(Document doc, OrdemServico os, String tituloDoc, String subtituloDoc) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{18, 52, 30})).useAllAvailableWidth();
        header.setBorder(Border.NO_BORDER);

        // Logo
        Cell cellLogo = new Cell().setBorder(Border.NO_BORDER);
        ImageData logoImg = PDFGenerator.carregarImageDataLogo(os.getCaminhoLogoBuffet());
        if (logoImg != null) {
            Image img = new Image(logoImg).setAutoScale(true).setMaxHeight(52);
            cellLogo.add(img);
        }
        header.addCell(cellLogo);

        // Dados da Empresa
        Cell cellEmpresa = new Cell().setBorder(Border.NO_BORDER);
        cellEmpresa.add(new Paragraph(os.getNomeBuffet()).setFontSize(14).setBold().setFontColor(COR_PRIMARIA));
        cellEmpresa.add(new Paragraph(os.getSubtituloBuffet()).setFontSize(9).setFontColor(COR_SECUNDARIA));
        header.addCell(cellEmpresa);

        // Título do Documento
        Cell cellTitulo = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        cellTitulo.add(new Paragraph(tituloDoc).setFontSize(13).setBold().setFontColor(COR_PRIMARIA));
        cellTitulo.add(new Paragraph(subtituloDoc).setFontSize(8).setFontColor(ColorConstants.GRAY));
        cellTitulo.add(new Paragraph("Emissão: " + LocalDateTime.now().format(FORMATO_DATA_HORA)).setFontSize(8).setFontColor(ColorConstants.GRAY));
        header.addCell(cellTitulo);

        doc.add(header);
    }

    private static Cell criarCelulaInfo(String rotulo, String valor) {
        Cell cell = new Cell().setPadding(6).setBorder(new SolidBorder(new DeviceRgb(226, 232, 240), 0.75f));
        Paragraph p = new Paragraph();
        p.add(new Paragraph(rotulo + "\n").setFontSize(8).setBold().setFontColor(COR_SECUNDARIA));
        p.add(new Paragraph(valor != null && !valor.isBlank() ? valor : "—").setFontSize(10));
        cell.add(p);
        return cell;
    }

    private static Cell criarCelulaMetrica(String rotulo, String valor, DeviceRgb corValor) {
        Cell cell = new Cell()
                .setBackgroundColor(COR_FUNDO_CINZA)
                .setBorder(new SolidBorder(new DeviceRgb(203, 213, 225), 1f))
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);

        cell.add(new Paragraph(rotulo).setFontSize(8).setBold().setFontColor(COR_SECUNDARIA));
        cell.add(new Paragraph(valor).setFontSize(13).setBold().setFontColor(corValor));
        return cell;
    }

    private static void adicionarBlocoAssinaturas(Document doc, OrdemServico os, String rotuloEsquerda, String rotuloDireita) {
        doc.add(new Paragraph("\n"));
        Table tabAss = new Table(UnitValue.createPercentArray(new float[]{45, 10, 45})).useAllAvailableWidth();
        tabAss.setBorder(Border.NO_BORDER);

        Cell assEsq = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        assEsq.add(new Paragraph("_________________________________________").setFontSize(10));
        assEsq.add(new Paragraph(os.getNomeBuffet()).setFontSize(9).setBold());
        assEsq.add(new Paragraph(rotuloEsquerda).setFontSize(8).setFontColor(ColorConstants.GRAY));

        Cell meio = new Cell().setBorder(Border.NO_BORDER);

        Cell assDir = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        assDir.add(new Paragraph("_________________________________________").setFontSize(10));
        assDir.add(new Paragraph(os.getCliente()).setFontSize(9).setBold());
        assDir.add(new Paragraph(rotuloDireita).setFontSize(8).setFontColor(ColorConstants.GRAY));

        tabAss.addCell(assEsq);
        tabAss.addCell(meio);
        tabAss.addCell(assDir);
        doc.add(tabAss);
    }

    private static void aplicarMarcaDaguaNoFundo(PdfDocument pdfDoc, ImageData logoWatermark) {
        int totalPaginas = pdfDoc.getNumberOfPages();
        for (int p = 1; p <= totalPaginas; p++) {
            PdfPage page = pdfDoc.getPage(p);
            Rectangle pageSize = page.getPageSize();

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            canvas.saveState();

            PdfExtGState gs = new PdfExtGState();
            gs.setFillOpacity(0.16f); // 16% translúcida: visível e legível
            canvas.setExtGState(gs);

            float imgLargura = 400f;
            float imgAltura = 320f;
            float x = (pageSize.getWidth() - imgLargura) / 2f;
            float y = (pageSize.getHeight() - imgAltura) / 2f;

            canvas.addImageFittedIntoRectangle(logoWatermark, new Rectangle(x, y, imgLargura, imgAltura), false);
            canvas.restoreState();
            canvas.release();
        }
    }
}
