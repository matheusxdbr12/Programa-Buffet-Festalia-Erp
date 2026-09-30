package com.festalia;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
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
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por gerar Ordens de Serviço formatadas em PDF utilizando a biblioteca iText 7.
 * Inclui cabeçalho personalizável do Buffet emissor e marca d'água translúcida do Festalia ERP ao fundo.
 */
public class PDFGenerator {

    private static final DeviceRgb COR_PRIMARIA = new DeviceRgb(41, 128, 185);      // Azul elegante
    private static final DeviceRgb COR_SECUNDARIA = new DeviceRgb(52, 73, 94);     // Cinza azulado escuro
    private static final DeviceRgb COR_FUNDO_CABECALHO = new DeviceRgb(240, 244, 248);
    private static final DeviceRgb COR_ZEBRA = new DeviceRgb(249, 250, 251);

    private static final DateTimeFormatter FORMATO_HORA_EMISSAO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gera a Ordem de Serviço em PDF no arquivo de destino informado.
     *
     * @param os            a ordem de serviço com os dados preenchidos.
     * @param caminhoDestino caminho absoluto ou relativo para salvar o arquivo PDF.
     * @throws IOException se houver erro ao criar ou escrever no arquivo.
     */
    public static void gerarOrdemServicoPDF(OrdemServico os, String caminhoDestino) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("A Ordem de Serviço não pode ser nula.");
        }
        if (caminhoDestino == null || caminhoDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("O caminho de destino do PDF não pode ser vazio.");
        }

        File arquivoDestino = new File(caminhoDestino);
        File parentDir = arquivoDestino.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ImageData logoWatermark = carregarImageDataLogo(os.getCaminhoLogoBuffet());

        try (FileOutputStream fos = new FileOutputStream(arquivoDestino);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36);

            // 1. Cabeçalho Personalizável do Buffet
            adicionarCabecalho(document, os);

            // Linha divisória sutil
            SolidLine divisor = new SolidLine(1.5f);
            divisor.setColor(COR_PRIMARIA);
            document.add(new LineSeparator(divisor).setMarginTop(5).setMarginBottom(15));

            // 2. Tabela de Informações do Evento e Cliente
            adicionarSecaoDadosEvento(document, os);

            // 3. Seção do Cardápio Selecionado
            adicionarSecaoCardapio(document, os.getCardapioSelecionado());

            // 4. Seção de Observações / Requisitos Especiais
            adicionarSecaoObservacoes(document, os.getObservacoes());

            // 5. Aplicação da Marca D'Água da Logomarca do Buffet ao fundo de todas as páginas geradas
            if (logoWatermark != null) {
                aplicarMarcaDaguaNoFundo(pdfDoc, logoWatermark);
            }

            document.close();
        }
    }

    private static void adicionarCabecalho(Document document, OrdemServico os) {
        Table tableHeader = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        tableHeader.setWidth(UnitValue.createPercentValue(100));

        String nomeBuffet = (os.getNomeBuffet() != null && !os.getNomeBuffet().trim().isEmpty())
                ? os.getNomeBuffet().trim() : "Festalia Buffet & Eventos";
        String subtitulo = (os.getSubtituloBuffet() != null && !os.getSubtituloBuffet().trim().isEmpty())
                ? os.getSubtituloBuffet().trim() : "Gestão Profissional de Buffets e Eventos";

        Cell celulaEsquerda = new Cell().setBorder(Border.NO_BORDER);
        celulaEsquerda.add(new Paragraph(nomeBuffet)
                .setFontSize(21)
                .setBold()
                .setFontColor(COR_PRIMARIA));
        celulaEsquerda.add(new Paragraph(subtitulo)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));

        Cell celulaDireita = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        celulaDireita.add(new Paragraph("ORDEM DE SERVIÇO")
                .setFontSize(12)
                .setBold()
                .setFontColor(COR_SECUNDARIA));
        celulaDireita.add(new Paragraph("Nº: " + os.getNumeroOS())
                .setFontSize(16)
                .setBold()
                .setFontColor(COR_PRIMARIA));

        tableHeader.addCell(celulaEsquerda);
        tableHeader.addCell(celulaDireita);
        document.add(tableHeader);
    }

    private static void adicionarSecaoDadosEvento(Document document, OrdemServico os) {
        Paragraph tituloSecao = new Paragraph("DADOS DO CLIENTE E DO EVENTO")
                .setFontSize(12)
                .setBold()
                .setFontColor(COR_SECUNDARIA)
                .setMarginBottom(6);
        document.add(tituloSecao);

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addCell(criarCelulaCampo("Cliente / Contratante:", os.getCliente()));
        table.addCell(criarCelulaCampo("Data do Evento:", os.getDataEventoFormatada()));
        table.addCell(criarCelulaCampo("Telefone / Contato:", os.getTelefone()));
        table.addCell(criarCelulaCampo("Qtd. de Convidados:", String.valueOf(os.getQuantidadeConvidados())));

        table.addCell(criarCelulaCampo("Local do Evento:", os.getLocal()));
        table.addCell(criarCelulaCampo("Status da OS:", os.getStatus()));

        table.addCell(criarCelulaCampo("Subtotal Buffet (" + os.getQuantidadeConvidados() + " pess.):", os.getSubtotalBuffetFormatado()));
        String rotuloTaxaServico = os.getTaxaServico() > 0 
                ? "Taxa de Serviço (" + os.getTaxaServicoPercentualFormatada() + " s/ valor/pessoa):"
                : "Taxa de Serviço (0%):";
        table.addCell(criarCelulaCampo(rotuloTaxaServico, os.getValorTaxaServicoFormatado()));

        table.addCell(criarCelulaCampo("Taxa de Transporte / Logística:", os.getTaxaTransporteFormatada()));
        table.addCell(criarCelulaCampo("VALOR TOTAL ESTIMADO:", os.getValorTotalFormatado()));

        table.addCell(criarCelulaCampo("Emissão do Documento:", LocalDateTime.now().format(FORMATO_HORA_EMISSAO)));
        table.addCell(criarCelulaCampo("Código Identificador:", "FEST-" + os.getNumeroOS()));

        document.add(table);
        document.add(new Paragraph().setMarginBottom(10));
    }

    private static void adicionarSecaoCardapio(Document document, Cardapio cardapio) {
        Paragraph tituloSecao = new Paragraph("DETALHES DO CARDÁPIO")
                .setFontSize(12)
                .setBold()
                .setFontColor(COR_SECUNDARIA)
                .setMarginBottom(6);
        document.add(tituloSecao);

        if (cardapio == null) {
            document.add(new Paragraph("Nenhum cardápio selecionado para este evento.")
                    .setItalic()
                    .setFontColor(ColorConstants.DARK_GRAY));
            return;
        }

        // Tabela de Apresentação do Menu
        Table tabelaMenu = new Table(UnitValue.createPercentArray(new float[]{100}));
        tabelaMenu.setWidth(UnitValue.createPercentValue(100));

        Cell celulaHeader = new Cell()
                .setBackgroundColor(COR_FUNDO_CABECALHO)
                .setBorder(new SolidBorder(COR_PRIMARIA, 1f))
                .setPadding(8);
        celulaHeader.add(new Paragraph("Menu: " + cardapio.getNome())
                .setFontSize(12)
                .setBold()
                .setFontColor(COR_PRIMARIA));
        if (cardapio.getDescricao() != null && !cardapio.getDescricao().trim().isEmpty()) {
            celulaHeader.add(new Paragraph(cardapio.getDescricao())
                    .setFontSize(9)
                    .setItalic()
                    .setFontColor(COR_SECUNDARIA));
        }
        tabelaMenu.addCell(celulaHeader);

        List<String> itens = cardapio.getItens();
        if (itens == null || itens.isEmpty()) {
            Cell celulaVazia = new Cell().setPadding(6)
                    .add(new Paragraph("Nenhum item cadastrado neste cardápio.").setItalic());
            tabelaMenu.addCell(celulaVazia);
        } else {
            boolean zebra = false;
            for (String item : itens) {
                Cell celulaItem = new Cell().setPadding(6);
                if (zebra) {
                    celulaItem.setBackgroundColor(COR_ZEBRA);
                }
                celulaItem.add(new Paragraph("•  " + item).setFontSize(10));
                tabelaMenu.addCell(celulaItem);
                zebra = !zebra;
            }
        }

        document.add(tabelaMenu);
        document.add(new Paragraph().setMarginBottom(10));
    }

    private static void adicionarSecaoObservacoes(Document document, String observacoes) {
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            Paragraph tituloSecao = new Paragraph("OBSERVAÇÕES E DIRETRIZES DO BUFFET")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(COR_SECUNDARIA)
                    .setMarginBottom(6);
            document.add(tituloSecao);

            Table obsTable = new Table(UnitValue.createPercentArray(new float[]{100}));
            obsTable.setWidth(UnitValue.createPercentValue(100));

            Cell obsCell = new Cell()
                    .setBackgroundColor(COR_ZEBRA)
                    .setPadding(8)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            obsCell.add(new Paragraph(observacoes).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));

            obsTable.addCell(obsCell);
            document.add(obsTable);
            document.add(new Paragraph().setMarginBottom(15));
        }
    }


    private static Cell criarCelulaCampo(String rotulo, String valor) {
        Cell cell = new Cell().setPadding(6).setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        Paragraph p = new Paragraph();
        p.add(new Paragraph(rotulo + " ").setFontSize(9).setBold().setFontColor(COR_SECUNDARIA));
        p.add(new Paragraph(valor != null && !valor.trim().isEmpty() ? valor : "—").setFontSize(10));
        cell.add(p);
        return cell;
    }

    /**
     * Aplica a logo do Buffet como Marca D'Água semitransparente ao fundo de todas as páginas da OS.
     */
    private static void aplicarMarcaDaguaNoFundo(PdfDocument pdfDoc, ImageData logoWatermark) {
        int totalPaginas = pdfDoc.getNumberOfPages();
        for (int p = 1; p <= totalPaginas; p++) {
            PdfPage page = pdfDoc.getPage(p);
            Rectangle pageSize = page.getPageSize();

            // newContentStreamBefore insere o stream gráfico no plano de fundo, atrás das tabelas e textos
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            canvas.saveState();

            // Opacidade calibrada em 18% (0.18f): translúcida, nítida e visível sem prejudicar a leitura
            PdfExtGState gs = new PdfExtGState();
            gs.setFillOpacity(0.18f);
            canvas.setExtGState(gs);

            float imgLargura = 420f;
            float imgAltura = 340f;
            float x = (pageSize.getWidth() - imgLargura) / 2f;
            float y = (pageSize.getHeight() - imgAltura) / 2f;

            canvas.addImageFittedIntoRectangle(logoWatermark, new Rectangle(x, y, imgLargura, imgAltura), false);
            canvas.restoreState();
            canvas.release(); // Libera e descarrega os bytes no stream do documento PDF
        }
    }

    public static ImageData carregarImageDataLogo(String caminhoCustomizado) {
        // 1. Tenta carregar o caminho customizado indicado na OS
        if (caminhoCustomizado != null && !caminhoCustomizado.trim().isEmpty()) {
            try {
                File f = new File(caminhoCustomizado.trim());
                if (f.exists() && f.isFile() && f.length() > 0) {
                    return ImageDataFactory.create(f.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Aviso: Falha ao carregar logo customizada (" + caminhoCustomizado + "): " + e.getMessage());
            }
        }

        // 2. Tenta carregar o caminho salvo nas configurações do buffet (ignora mendez se padrão)
        try {
            String configLogo = DatabaseManager.obterConfiguracao("buffet.logo_path", null);
            if (configLogo != null && !configLogo.trim().isEmpty() && !configLogo.toLowerCase().contains("mendez")) {
                File fConfig = new File(configLogo.trim());
                if (fConfig.exists() && fConfig.isFile() && fConfig.length() > 0) {
                    return ImageDataFactory.create(fConfig.getAbsolutePath());
                }
            }
        } catch (Exception ignored) {}

        // 3. Tenta carregar do classpath (festalia_logo.jpg primeiro)
        String[] recursosClasspath = {
            "/festalia_logo.jpg",
            "festalia_logo.jpg",
            "/mendez_logo.png",
            "mendez_logo.png"
        };
        for (String recurso : recursosClasspath) {
            try (InputStream is = PDFGenerator.class.getResourceAsStream(recurso.startsWith("/") ? recurso : "/" + recurso)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes.length > 0) {
                        return ImageDataFactory.create(bytes);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 4. Múltiplos caminhos de arquivos locais de desenvolvimento e execução (Festalia primeiro)
        String[] caminhos = {
            "festalia_logo.jpg",
            "src/main/resources/festalia_logo.jpg",
            "target/classes/festalia_logo.jpg",
            "C:/Users/matheus.souza/.gemini/antigravity-ide/brain/6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61/.user_uploaded/media_1790699283882.jpg",
            "C:/Users/matheus.souza/Documents/Programa do BUFFET GUI/festalia_logo.jpg",
            "mendez_logo.png",
            "src/main/resources/mendez_logo.png",
            "target/classes/mendez_logo.png"
        };

        for (String caminho : caminhos) {
            try {
                File f = new File(caminho);
                if (f.exists() && f.isFile() && f.length() > 0) {
                    return ImageDataFactory.create(f.getAbsolutePath());
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}
