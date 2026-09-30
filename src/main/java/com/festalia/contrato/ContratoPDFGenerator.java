package com.festalia.contrato;

import com.festalia.OrdemServico;
import com.festalia.PDFGenerator;
import com.festalia.financeiro.ParcelaOS;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador de Contrato de Prestação de Serviços em formato PDF profissional (iText 7).
 * Padronizado com a identidade visual da empresa, paginação dinâmica e marca d'água discreta.
 */
public class ContratoPDFGenerator {

    private static final DeviceRgb COR_AZUL_FESTALIA = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb COR_ESCURA = new DeviceRgb(44, 62, 80);
    private static final DeviceRgb COR_CINZA_FUNDO = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb COR_BORDA_SUAVE = new DeviceRgb(226, 232, 240);

    private static String ultimoErro = null;

    public static String getUltimoErro() {
        return ultimoErro;
    }

    public static String strSafe(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s.trim() : fallback;
    }

    public static String strSafe(String s) {
        return strSafe(s, "—");
    }

    /**
     * Sobrecarga estática conveniente que prepara a OS, parâmetros e gera o PDF em contratos_pdf/.
     */
    public static String gerarPdfContrato(Contrato contrato, List<?> parcelasObj, boolean abrirAposGerar) {
        ultimoErro = null;
        try {
            File pastaSaida = new File("contratos_pdf");
            if (!pastaSaida.exists()) {
                pastaSaida.mkdirs();
            }

            if (contrato.getDataEmissao() == null) {
                contrato.setDataEmissao(LocalDate.now());
            }

            String nomeLimpo = contrato.getContratanteNome() != null && !contrato.getContratanteNome().isBlank()
                    ? contrato.getContratanteNome().replaceAll("[^a-zA-Z0-9_-]", "_") : "Cliente";
            String numContratoLimpo = contrato.getNumeroContrato() != null
                    ? contrato.getNumeroContrato().replaceAll("[^a-zA-Z0-9_-]", "_") : "NOVO";
            File arquivoDestino = new File(pastaSaida, "Contrato_" + numContratoLimpo + "_" + nomeLimpo + "_v" + contrato.getVersaoAtual() + ".pdf");

            // Se o arquivo já existir e estiver bloqueado/aberto por leitor de PDF/navegador, usa sufixo para não travar
            if (arquivoDestino.exists()) {
                boolean bloqueado = false;
                try (FileOutputStream testFos = new FileOutputStream(arquivoDestino, true)) {
                    // Arquivo disponível para gravação
                } catch (Exception exLock) {
                    bloqueado = true;
                }

                if (bloqueado) {
                    int counter = 1;
                    while (counter <= 99) {
                        File alt = new File(pastaSaida, "Contrato_" + numContratoLimpo + "_" + nomeLimpo + "_v" + contrato.getVersaoAtual() + "_(" + counter + ").pdf");
                        boolean altLivre = false;
                        try (FileOutputStream testFos = new FileOutputStream(alt, true)) {
                            altLivre = true;
                        } catch (Exception ignored) {}

                        if (altLivre) {
                            arquivoDestino = alt;
                            break;
                        }
                        counter++;
                    }
                }
            }

            com.festalia.OrdemServicoDAO osDAO = new com.festalia.OrdemServicoDAO();
            OrdemServico os = null;
            if (contrato.getOrdemServicoId() > 0) {
                os = osDAO.buscarPorId(contrato.getOrdemServicoId());
            }
            if (os == null && contrato.getNumeroOS() > 0) {
                os = osDAO.buscarPorNumeroOS(contrato.getNumeroOS());
            }

            if (os == null) {
                os = new OrdemServico(
                        contrato.getOrdemServicoId(),
                        contrato.getNumeroOS() > 0 ? contrato.getNumeroOS() : Math.max(1, contrato.getOrdemServicoId()),
                        strSafe(contrato.getEmpresaRazaoSocial(), "Festalia Buffet & Eventos Ltda"),
                        strSafe(contrato.getEmpresaNomeFantasia(), "Festalia Buffet"),
                        null,
                        strSafe(contrato.getContratanteNome(), "Cliente"),
                        strSafe(contrato.getContratanteTelefone(), ""),
                        contrato.getDataEvento() != null ? contrato.getDataEvento() : LocalDate.now(),
                        strSafe(contrato.getLocalEvento(), "Espaço Festalia / Conforme OS"),
                        contrato.getNumeroConvidados() > 0 ? contrato.getNumeroConvidados() : 100,
                        contrato.getValorTotal() / Math.max(1, contrato.getNumeroConvidados()),
                        0.0, 0.0,
                        contrato.getValorTotal(),
                        "Confirmado",
                        null,
                        strSafe(contrato.getObservacoesEspeciais(), ""),
                        null,
                        java.time.LocalTime.of(18, 0),
                        java.time.LocalTime.of(22, 0),
                        strSafe(contrato.getTipoEvento(), "Casamento"),
                        false
                );
            }

            List<ParcelaOS> parcelas = new ArrayList<>();
            if (parcelasObj != null) {
                for (Object item : parcelasObj) {
                    if (item instanceof ParcelaOS) {
                        parcelas.add((ParcelaOS) item);
                    }
                }
            }
            if (parcelas.isEmpty() && contrato.getOrdemServicoId() > 0) {
                com.festalia.financeiro.FinanceiroDAO finDao = new com.festalia.financeiro.FinanceiroDAO();
                parcelas = finDao.listarParcelasPorOS(contrato.getOrdemServicoId());
            }

            ContratoTemplateEngine.ParametrosContrato params = new ContratoTemplateEngine.ParametrosContrato();
            params.observacoesEspeciais = contrato.getObservacoesEspeciais();

            List<ClausulaContrato> clausulas = contrato.getClausulas();
            if (clausulas == null || clausulas.isEmpty()) {
                clausulas = ContratoTemplateEngine.gerarClausulas(os, contrato, parcelas, params);
            }

            gerarContratoPDF(os, contrato, clausulas, parcelas, params, arquivoDestino.getAbsolutePath());

            contrato.setCaminhoPdf(arquivoDestino.getAbsolutePath());
            return arquivoDestino.getAbsolutePath();
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            if (ex instanceof java.io.FileNotFoundException && (msg.contains("usado por outro processo") || msg.contains("Access is denied") || msg.contains("Acesso negado"))) {
                ultimoErro = "O arquivo PDF já está aberto em outro programa (como o visualizador de PDF ou navegador).\nPor favor, feche o arquivo antes de gerar.";
            } else {
                ultimoErro = ex.getClass().getSimpleName() + ": " + msg;
            }
            System.err.println("Erro ao gerar PDF do contrato: " + ultimoErro);
            ex.printStackTrace();
            return null;
        }
    }

    public static String gerarPdfContrato(Contrato contrato) {
        return gerarPdfContrato(contrato, null, false);
    }

    public static void gerarContratoPDF(OrdemServico os, Contrato contrato, List<ClausulaContrato> clausulas,
                                        List<ParcelaOS> parcelas, ContratoTemplateEngine.ParametrosContrato params,
                                        String caminhoDestino) throws IOException {

        File arquivoDestino = new File(caminhoDestino);
        if (arquivoDestino.getParentFile() != null && !arquivoDestino.getParentFile().exists()) {
            arquivoDestino.getParentFile().mkdirs();
        }

        ImageData logoWatermark = os != null ? PDFGenerator.carregarImageDataLogo(os.getCaminhoLogoBuffet()) : null;

        try (FileOutputStream fos = new FileOutputStream(arquivoDestino);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            // immediateFlush = false garante que todas as páginas permaneçam acessíveis para marca d'água e rodapé
            Document document = new Document(pdfDoc, PageSize.A4, false);
            document.setMargins(40, 44, 45, 44);

            // Handler para paginação no rodapé
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new NumeracaoPaginaHandler(contrato));

            // 1. Cabeçalho Institucional
            adicionarCabecalho(document, os, contrato);

            // 2. Qualificação das Partes (Contratada e Contratante)
            adicionarQualificacaoPartes(document, contrato);

            // 3. Resumo Operacional do Evento (Card de Contexto)
            adicionarResumoEvento(document, os, contrato);

            // 4. Cláusulas Jurídicas (1ª a 14ª)
            adicionarClausulas(document, clausulas, parcelas, params);

            // 5. Bloco de Assinaturas (Contratada, Contratante, Testemunha 1 e 2)
            adicionarBlocoAssinaturas(document, contrato, params.incluirTestemunhas);

            // Aplica marca d'água antes de fechar o documento
            if (logoWatermark != null) {
                aplicarMarcaDaguaNoFundo(pdfDoc, logoWatermark);
            }

            document.close();
        }
    }

    private static void adicionarCabecalho(Document doc, OrdemServico os, Contrato contrato) {
        Table tabCabecalho = new Table(UnitValue.createPercentArray(new float[]{22, 53, 25})).useAllAvailableWidth();
        tabCabecalho.setBorder(Border.NO_BORDER);
        tabCabecalho.setMarginBottom(8);

        // Logo
        Cell cellLogo = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        ImageData logoData = os != null ? PDFGenerator.carregarImageDataLogo(os.getCaminhoLogoBuffet()) : null;
        if (logoData != null) {
            Image logo = new Image(logoData).scaleToFit(85, 48);
            cellLogo.add(logo);
        }
        tabCabecalho.addCell(cellLogo);

        // Nomes e Título
        Cell cellTexto = new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        String nomeEmpresa = strSafe(contrato.getEmpresaNomeFantasia(), os != null ? strSafe(os.getNomeBuffet(), "FESTALIA BUFFET") : "FESTALIA BUFFET");
        String tituloModelo = contrato.getModeloTipo() != null ? strSafe(contrato.getModeloTipo().getTitulo(), "CONTRATO DE PRESTAÇÃO DE SERVIÇOS") : "CONTRATO DE PRESTAÇÃO DE SERVIÇOS";
        cellTexto.add(new Paragraph(nomeEmpresa.toUpperCase())
                .setFontSize(13).setBold().setFontColor(COR_AZUL_FESTALIA));
        cellTexto.add(new Paragraph(tituloModelo.toUpperCase())
                .setFontSize(9).setBold().setFontColor(COR_ESCURA));
        cellTexto.add(new Paragraph("Instrumento Particular de Prestação de Serviços Gastronômicos e Infraestrutura")
                .setFontSize(7.5f).setFontColor(ColorConstants.GRAY));
        tabCabecalho.addCell(cellTexto);

        // Box de Identificação e Versão
        Cell cellIdent = new Cell().setBorder(new SolidBorder(COR_BORDA_SUAVE, 1))
                .setBackgroundColor(COR_CINZA_FUNDO)
                .setPadding(6)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT);
        cellIdent.add(new Paragraph(strSafe(contrato.getNumeroContrato(), "CONTRATO")).setFontSize(10).setBold().setFontColor(COR_AZUL_FESTALIA));
        int numOsIdent = os != null ? os.getNumeroOS() : contrato.getNumeroOS();
        cellIdent.add(new Paragraph("OS Vinculada: #" + numOsIdent).setFontSize(8).setBold());
        String statusDesc = contrato.getStatus() != null ? contrato.getStatus().getDescricao() : "Gerado";
        cellIdent.add(new Paragraph("Versão: " + strSafe(contrato.getVersaoAtual(), "1.0") + " • Status: " + statusDesc).setFontSize(7.5f).setFontColor(COR_ESCURA));
        tabCabecalho.addCell(cellIdent);

        doc.add(tabCabecalho);

        SolidLine linha = new SolidLine(1.5f);
        linha.setColor(COR_AZUL_FESTALIA);
        doc.add(new LineSeparator(linha).setMarginBottom(12));
    }

    private static void adicionarQualificacaoPartes(Document doc, Contrato c) {
        Table tabPartes = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        tabPartes.setBorder(Border.NO_BORDER);
        tabPartes.setMarginBottom(12);

        // Box Contratada
        Cell cellContratada = new Cell()
                .setBackgroundColor(COR_CINZA_FUNDO)
                .setBorder(new SolidBorder(COR_BORDA_SUAVE, 1))
                .setPadding(8);
        cellContratada.add(new Paragraph("CONTRATADA (PRESTADORA DE SERVIÇOS)").setFontSize(8.5f).setBold().setFontColor(COR_AZUL_FESTALIA));
        cellContratada.add(new Paragraph(strSafe(c.getEmpresaRazaoSocial(), "Festalia Buffet & Eventos Ltda")).setFontSize(9.5f).setBold());
        cellContratada.add(new Paragraph("CNPJ: " + strSafe(c.getEmpresaCnpj(), "—")).setFontSize(8));
        cellContratada.add(new Paragraph("Endereço: " + strSafe(c.getEmpresaEndereco(), "—")).setFontSize(8));
        if (c.getEmpresaTelefone() != null && !c.getEmpresaTelefone().isBlank()) {
            cellContratada.add(new Paragraph("Tel: " + strSafe(c.getEmpresaTelefone()) + " | E-mail: " + strSafe(c.getEmpresaEmail())).setFontSize(7.5f));
        }
        tabPartes.addCell(cellContratada);

        // Box Contratante
        Cell cellContratante = new Cell()
                .setBackgroundColor(COR_CINZA_FUNDO)
                .setBorder(new SolidBorder(COR_BORDA_SUAVE, 1))
                .setPadding(8);
        cellContratante.add(new Paragraph("CONTRATANTE (TOMADOR DOS SERVIÇOS)").setFontSize(8.5f).setBold().setFontColor(COR_AZUL_FESTALIA));
        cellContratante.add(new Paragraph(strSafe(c.getContratanteNome(), "—")).setFontSize(9.5f).setBold());
        cellContratante.add(new Paragraph("CPF/CNPJ: " + strSafe(c.getContratanteCpfCnpj(), "—") + (c.getContratanteRg() != null && !c.getContratanteRg().isBlank() ? " | RG: " + c.getContratanteRg() : "")).setFontSize(8));
        cellContratante.add(new Paragraph("Endereço: " + strSafe(c.getContratanteEndereco(), "Conforme cadastro na OS")).setFontSize(8));
        if (c.getContratanteTelefone() != null && !c.getContratanteTelefone().isBlank()) {
            cellContratante.add(new Paragraph("Tel: " + strSafe(c.getContratanteTelefone()) + (c.getContratanteEmail() != null && !c.getContratanteEmail().isBlank() ? " | E-mail: " + strSafe(c.getContratanteEmail()) : "")).setFontSize(7.5f));
        }
        tabPartes.addCell(cellContratante);

        doc.add(tabPartes);
    }

    private static void adicionarResumoEvento(Document doc, OrdemServico os, Contrato c) {
        Table tab = new Table(UnitValue.createPercentArray(new float[]{25, 25, 30, 20})).useAllAvailableWidth();
        tab.setMarginBottom(12);

        tab.addCell(criarCelulaResumo("Tipo do Evento", strSafe(os != null ? os.getTipoEvento() : c.getTipoEvento(), "Evento")));
        tab.addCell(criarCelulaResumo("Data da Realização", strSafe(os != null ? os.getDataEventoFormatada() : c.getDataEventoFormatada(), "—")));
        String hor = (os != null ? (strSafe(os.getHorarioInicioFormatado(), "18:00") + " às " + strSafe(os.getHorarioFimFormatado(), "22:00")) : strSafe(c.getHorarioEvento(), "Conforme OS"));
        tab.addCell(criarCelulaResumo("Horário Contratado", hor));
        int convidados = os != null ? os.getQuantidadeConvidados() : c.getNumeroConvidados();
        tab.addCell(criarCelulaResumo("Nº de Convidados", convidados + " pessoas"));

        doc.add(tab);
    }

    private static Cell criarCelulaResumo(String label, String valor) {
        Cell cell = new Cell()
                .setBackgroundColor(new DeviceRgb(241, 245, 249))
                .setBorder(new SolidBorder(COR_BORDA_SUAVE, 0.8f))
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER);
        cell.add(new Paragraph(strSafe(label, "Informação")).setFontSize(7).setBold().setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(strSafe(valor, "A definir")).setFontSize(8.5f).setBold().setFontColor(COR_ESCURA));
        return cell;
    }

    private static void adicionarClausulas(Document doc, List<ClausulaContrato> clausulas,
                                           List<ParcelaOS> parcelas, ContratoTemplateEngine.ParametrosContrato params) {
        int idx = 1;
        for (ClausulaContrato cl : clausulas) {
            if (cl == null || !cl.isAtiva()) continue;

            Paragraph pTitulo = new Paragraph(String.format("CLÁUSULA %dª — %s", idx++, strSafe(cl.getTitulo(), "DISPOSIÇÃO").toUpperCase()))
                    .setFontSize(9f)
                    .setBold()
                    .setFontColor(COR_AZUL_FESTALIA)
                    .setMarginTop(8)
                    .setMarginBottom(3)
                    .setKeepWithNext(true);
            doc.add(pTitulo);

            Paragraph pConteudo = new Paragraph(strSafe(cl.getConteudo(), ""))
                    .setFontSize(8.5f)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setFontColor(COR_ESCURA)
                    .setMarginBottom(6);
            doc.add(pConteudo);

            // Se for a cláusula de preço e pagamento, insere a tabela do cronograma
            if ("VALOR_PAGAMENTO".equalsIgnoreCase(cl.getChave()) && params != null && params.incluirCronogramaDetalhado && parcelas != null && !parcelas.isEmpty()) {
                adicionarTabelaCronograma(doc, parcelas);
            }
        }
    }

    private static void adicionarTabelaCronograma(Document doc, List<ParcelaOS> parcelas) {
        Table tab = new Table(UnitValue.createPercentArray(new float[]{10, 35, 20, 15, 20})).useAllAvailableWidth();
        tab.setMarginTop(4).setMarginBottom(8);

        tab.addHeaderCell(criarHeaderTabela("Nº"));
        tab.addHeaderCell(criarHeaderTabela("Descrição"));
        tab.addHeaderCell(criarHeaderTabela("Vencimento"));
        tab.addHeaderCell(criarHeaderTabela("Forma"));
        tab.addHeaderCell(criarHeaderTabela("Valor"));

        for (ParcelaOS p : parcelas) {
            if (p == null) continue;
            Cell cNum = new Cell().setPadding(4).setTextAlignment(TextAlignment.CENTER).setFontSize(8).add(new Paragraph("#" + p.getNumeroParcela()));
            Cell cDesc = new Cell().setPadding(4).setFontSize(8).add(new Paragraph(strSafe(p.getDescricao(), "Parcela")));
            Cell cVenc = new Cell().setPadding(4).setTextAlignment(TextAlignment.CENTER).setFontSize(8).add(new Paragraph(strSafe(p.getDataVencimentoFormatada(), "—")));
            Cell cForma = new Cell().setPadding(4).setTextAlignment(TextAlignment.CENTER).setFontSize(8).add(new Paragraph(strSafe(p.getFormaPagamentoSugerida(), "A combinar")));
            Cell cVal = new Cell().setPadding(4).setTextAlignment(TextAlignment.RIGHT).setFontSize(8).setBold().add(new Paragraph(strSafe(p.getValorFormatado(), "R$ 0,00")));

            tab.addCell(cNum);
            tab.addCell(cDesc);
            tab.addCell(cVenc);
            tab.addCell(cForma);
            tab.addCell(cVal);
        }

        doc.add(tab);
    }

    private static Cell criarHeaderTabela(String texto) {
        return new Cell()
                .setBackgroundColor(COR_AZUL_FESTALIA)
                .setBorder(Border.NO_BORDER)
                .setPadding(4)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8)
                .setBold()
                .setFontColor(ColorConstants.WHITE)
                .add(new Paragraph(strSafe(texto, "—")));
    }

    private static void adicionarBlocoAssinaturas(Document doc, Contrato c, boolean incluirTestemunhas) {
        String foro = strSafe(c.getEmpresaCidadeForo(), "São Paulo - SP");
        String dataEmissao = strSafe(c.getDataEmissaoFormatada(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Paragraph pEncerramento = new Paragraph(String.format(
                "\nE, por estarem assim plenamente justas, acordadas e contratadas, as partes firmam o presente instrumento em 2 (duas) vias de igual teor e forma.\n%s, %s.",
                foro, dataEmissao
        )).setFontSize(8.5f).setTextAlignment(TextAlignment.JUSTIFIED).setMarginBottom(18).setKeepWithNext(true);
        doc.add(pEncerramento);

        // Assinaturas Principais: Contratada e Contratante
        Table tabAss = new Table(UnitValue.createPercentArray(new float[]{46, 8, 46})).useAllAvailableWidth();
        tabAss.setBorder(Border.NO_BORDER);
        tabAss.setKeepWithNext(true);

        Cell assContratada = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        assContratada.add(new Paragraph("_____________________________________________").setFontSize(9));
        String nomeContratada = strSafe(c.getEmpresaNomeFantasia(), strSafe(c.getEmpresaRazaoSocial(), "CONTRATADA"));
        assContratada.add(new Paragraph(nomeContratada).setFontSize(8.5f).setBold());
        assContratada.add(new Paragraph("CONTRATADA • CNPJ: " + strSafe(c.getEmpresaCnpj(), "—")).setFontSize(7.5f).setFontColor(ColorConstants.GRAY));

        Cell meio = new Cell().setBorder(Border.NO_BORDER);

        Cell assContratante = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        assContratante.add(new Paragraph("_____________________________________________").setFontSize(9));
        assContratante.add(new Paragraph(strSafe(c.getContratanteNome(), "CONTRATANTE")).setFontSize(8.5f).setBold());
        assContratante.add(new Paragraph("CONTRATANTE • CPF/CNPJ: " + strSafe(c.getContratanteCpfCnpj(), "—")).setFontSize(7.5f).setFontColor(ColorConstants.GRAY));

        tabAss.addCell(assContratada);
        tabAss.addCell(meio);
        tabAss.addCell(assContratante);
        doc.add(tabAss);

        // Bloco de Testemunhas
        if (incluirTestemunhas) {
            doc.add(new Paragraph("\n"));
            Table tabTest = new Table(UnitValue.createPercentArray(new float[]{46, 8, 46})).useAllAvailableWidth();
            tabTest.setBorder(Border.NO_BORDER);

            Cell test1 = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            test1.add(new Paragraph("_____________________________________________").setFontSize(9));
            test1.add(new Paragraph("TESTEMUNHA 1").setFontSize(8).setBold());
            test1.add(new Paragraph("Nome: _________________  CPF: _________________").setFontSize(7.5f).setFontColor(ColorConstants.GRAY));

            Cell test2 = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            test2.add(new Paragraph("_____________________________________________").setFontSize(9));
            test2.add(new Paragraph("TESTEMUNHA 2").setFontSize(8).setBold());
            test2.add(new Paragraph("Nome: _________________  CPF: _________________").setFontSize(7.5f).setFontColor(ColorConstants.GRAY));

            tabTest.addCell(test1);
            tabTest.addCell(new Cell().setBorder(Border.NO_BORDER));
            tabTest.addCell(test2);
            doc.add(tabTest);
        }
    }

    private static void aplicarMarcaDaguaNoFundo(PdfDocument pdfDoc, ImageData logoWatermark) {
        if (logoWatermark == null) return;
        try {
            int totalPaginas = pdfDoc.getNumberOfPages();
            for (int p = 1; p <= totalPaginas; p++) {
                PdfPage page = pdfDoc.getPage(p);
                if (page == null || page.isFlushed()) continue;
                Rectangle pageSize = page.getPageSize();

                PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
                canvas.saveState();

                PdfExtGState gs = new PdfExtGState();
                gs.setFillOpacity(0.06f); // 6% sutil e elegante
                canvas.setExtGState(gs);

                float imgLargura = 420f;
                float imgAltura = 340f;
                float x = (pageSize.getWidth() - imgLargura) / 2f;
                float y = (pageSize.getHeight() - imgAltura) / 2f;

                canvas.addImageFittedIntoRectangle(logoWatermark, new Rectangle(x, y, imgLargura, imgAltura), false);
                canvas.restoreState();
                canvas.release();
            }
        } catch (Exception exWatermark) {
            System.err.println("Aviso: Falha ao desenhar marca d'água no contrato: " + exWatermark.getMessage());
        }
    }

    /**
     * Handler de rodapé para numeração de páginas ("Página X de Y") e número do contrato.
     */
    private static class NumeracaoPaginaHandler implements IEventHandler {
        private final Contrato contrato;

        public NumeracaoPaginaHandler(Contrato contrato) {
            this.contrato = contrato;
        }

        @Override
        public void handleEvent(Event event) {
            try {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfDocument pdfDoc = docEvent.getDocument();
                PdfPage page = docEvent.getPage();
                if (page == null || page.isFlushed()) return;
                int pageNum = pdfDoc.getPageNumber(page);

                PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
                Canvas c = new Canvas(canvas, page.getPageSize());

                float y = 20;
                float margin = 44;
                String numContrato = strSafe(contrato.getNumeroContrato(), "CONTRATO");
                String versao = strSafe(contrato.getVersaoAtual(), "1.0");

                c.showTextAligned(new Paragraph(numContrato + " (Versão " + versao + ")").setFontSize(7.5f).setFontColor(ColorConstants.GRAY), margin, y, TextAlignment.LEFT);
                c.showTextAligned(new Paragraph("Página " + pageNum).setFontSize(7.5f).setFontColor(ColorConstants.GRAY), page.getPageSize().getWidth() - margin, y, TextAlignment.RIGHT);
                c.close();
            } catch (Exception exFooter) {
                System.err.println("Aviso no rodapé do contrato: " + exFooter.getMessage());
            }
        }
    }
}

