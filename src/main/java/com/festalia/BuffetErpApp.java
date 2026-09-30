package com.festalia;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interface Gráfica Principal do Festalia ERP v2.0 com suporte a SQLite.
 * Organizada em abas:
 * 1. Nova Ordem de Serviço & Emissão PDF
 * 2. Histórico de Eventos & Faturamento
 * 3. Portfólio de Cardápios & Tabela de Preços
 */
public class BuffetErpApp extends JFrame {

    private static final NumberFormat MOEDA_BRL = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final Color COR_AZUL_FESTALIA = new Color(41, 128, 185);
    private static final Color COR_ESCURA = new Color(44, 62, 80);
    private static final Color COR_FUNDO = new Color(248, 249, 250);

    private final CardapioDAO cardapioDAO = new CardapioDAO();
    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    private final List<Cardapio> listaCardapios = new ArrayList<>();

    // Componentes da Aba 1 (Nova OS)
    private JTextField txtNomeBuffet;
    private JTextField txtSubtituloBuffet;
    private JTextField txtCaminhoLogoBuffet;
    private JLabel lblPreviewLogoBuffet;
    private JLabel lblMiniLogoBanner;
    private JTextField txtNumeroOS;
    private JTextField txtCliente;
    private JTextField txtTelefone;
    private JTextField txtDataEvento;
    private JTextField txtLocal;
    private JTextField txtConvidados;
    private JTextField txtPrecoPorPessoa;
    private JTextField txtTaxaServico;
    private JLabel lblValorTaxaServicoCalculado;
    private JTextField txtTaxaTransporte;
    private JLabel lblValorTotalCalculado;
    private JComboBox<String> cbStatusOS;
    private JComboBox<Cardapio> cbCardapios;
    private JTextArea txtDescricaoCardapio;
    private JTextArea txtObservacoes;

    // Componentes da Aba 2 (Histórico)
    private JTextField txtBuscaHistorico;
    private JTable tabelaHistorico;
    private DefaultTableModel modelHistorico;
    private JLabel lblTotalEventos;
    private JLabel lblTotalFaturamento;

    // Componentes da Aba 3 (Portfólio de Cardápios)
    private JTable tabelaCardapios;
    private DefaultTableModel modelCardapios;
    private JTextField txtPortNome;
    private JTextField txtPortPreco;
    private JTextField txtPortDescricao;
    private JTextArea txtPortItens;
    private JButton btnExcluirCardapio;
    private int idCardapioEmEdicao = 0;

    public BuffetErpApp() {
        super("Festalia ERP - Gestão Integrada de Buffets e Eventos (v2.0)");

        // 1. Inicializa o banco de dados SQLite e sincroniza assets
        DatabaseManager.inicializarBanco();
        garantirAssetsIniciais();

        // 2. Carrega dados do banco
        recarregarListaCardapios();

        // 3. Monta a interface
        configurarJanela();
        construirInterface();
        atualizarCalculoValorTotal();
        carregarTabelaHistorico();
        carregarTabelaPortfólio();
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 780);
        setMinimumSize(new Dimension(880, 680));
        setLocationRelativeTo(null);

        Image logoImg = carregarImagemLogoRaw();
        if (logoImg != null) {
            setIconImage(logoImg);
        }
    }

    private void recarregarListaCardapios() {
        listaCardapios.clear();
        listaCardapios.addAll(cardapioDAO.listarTodos());
    }

    private void construirInterface() {
        JPanel painelGeral = new JPanel(new BorderLayout(10, 10));
        painelGeral.setBackground(COR_FUNDO);
        painelGeral.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Topo da aplicação
        painelGeral.add(criarCabecalhoPrincipal(), BorderLayout.NORTH);

        // Abas principais
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("Segoe UI", Font.BOLD, 13));

        abas.addTab("  Nova Ordem de Serviço  ", criarAbaNovaOS());
        abas.addTab("  Histórico & Faturamento  ", criarAbaHistorico());
        abas.addTab("  Portfólio de Cardápios & Preços  ", criarAbaPortfólio());

        painelGeral.add(abas, BorderLayout.CENTER);

        setContentPane(painelGeral);
    }

    private JPanel criarCabecalhoPrincipal() {
        JPanel cabecalho = new JPanel(new BorderLayout(14, 4));
        cabecalho.setOpaque(false);
        cabecalho.setBorder(new EmptyBorder(4, 4, 10, 4));

        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        painelEsquerda.setOpaque(false);

        // Logo oficial Festalia ERP visível no sistema
        ImageIcon logoIcon = carregarIconeLogo(52, 52);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setBorder(new CompoundBorder(
                    new LineBorder(new Color(218, 224, 233), 1, true),
                    new EmptyBorder(2, 2, 2, 2)
            ));
            lblLogo.setToolTipText("Festalia ERP - Gestão Profissional de Buffets e Eventos");
            painelEsquerda.add(lblLogo);
        }

        JPanel painelTextos = new JPanel(new GridLayout(2, 1, 2, 2));
        painelTextos.setOpaque(false);

        JLabel lblTitulo = new JLabel("FESTALIA ERP • Sistema de Gestão para Buffets");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(COR_AZUL_FESTALIA);

        JLabel lblSub = new JLabel("Controle financeiro, emissão de ordens de serviço em PDF e repertório gastronômico.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.DARK_GRAY);

        painelTextos.add(lblTitulo);
        painelTextos.add(lblSub);
        painelEsquerda.add(painelTextos);

        // Badge estilizado e compacto para o status do banco de dados SQLite
        JPanel badgeDb = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        badgeDb.setBackground(new Color(235, 247, 238));
        badgeDb.setBorder(new CompoundBorder(
                new LineBorder(new Color(163, 228, 180), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        badgeDb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        badgeDb.setToolTipText("Banco SQLite ativo em: " + DatabaseManager.getCaminhoBanco());

        JLabel lblDot = new JLabel("●");
        lblDot.setForeground(new Color(39, 174, 96));
        lblDot.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblStatusTexto = new JLabel("SQLite Conectado");
        lblStatusTexto.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusTexto.setForeground(new Color(30, 130, 76));

        badgeDb.add(lblDot);
        badgeDb.add(lblStatusTexto);

        JPanel painelDireita = new JPanel(new GridBagLayout());
        painelDireita.setOpaque(false);
        painelDireita.add(badgeDb);

        cabecalho.add(painelEsquerda, BorderLayout.WEST);
        cabecalho.add(painelDireita, BorderLayout.EAST);
        return cabecalho;
    }

    // =========================================================================
    // ABA 1: NOVA ORDEM DE SERVIÇO
    // =========================================================================
    private JPanel criarAbaNovaOS() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Formulário Superior
        painel.add(criarFormularioEvento(), BorderLayout.NORTH);

        // Centro: Cardápio Editável + Observações
        JPanel painelCentro = new JPanel(new GridLayout(1, 2, 10, 10));
        painelCentro.setOpaque(false);
        painelCentro.add(criarPainelCardapioEditavel());
        painelCentro.add(criarPainelObservacoes());
        painel.add(painelCentro, BorderLayout.CENTER);

        // Rodapé: Botões de Ação
        painel.add(criarBarraBotoesNovaOS(), BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarFormularioEvento() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(218, 224, 233), 1, true), " Dados do Evento e Condições Financeiras ",
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COR_ESCURA),
                new EmptyBorder(8, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Linha 0: Identificação do Buffet Emissor (Campos Editáveis do Cabeçalho do PDF)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.12;
        painel.add(new JLabel("Nome do Buffet (PDF): *"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.38;
        String buffetSalvo = DatabaseManager.obterConfiguracao("buffet.nome", "Festalia Buffet & Eventos");
        txtNomeBuffet = new JTextField(buffetSalvo);
        txtNomeBuffet.putClientProperty("JTextField.placeholderText", "Nome da sua empresa no cabeçalho do PDF");
        painel.add(txtNomeBuffet, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        painel.add(new JLabel("Slogan / Subtítulo (PDF):"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        String subtituloSalvo = DatabaseManager.obterConfiguracao("buffet.subtitulo", "Gestão Profissional de Buffets e Eventos");
        txtSubtituloBuffet = new JTextField(subtituloSalvo);
        txtSubtituloBuffet.putClientProperty("JTextField.placeholderText", "Slogan no cabeçalho do PDF");
        painel.add(txtSubtituloBuffet, gbc);

        // Linha 1: Logomarca do Buffet (Marca D'Água de toda a OS gerada em PDF)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.12;
        painel.add(new JLabel("Logo Buffet (Marca D'Água):"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 0.88;
        JPanel painelLogo = new JPanel(new BorderLayout(8, 0));
        painelLogo.setOpaque(false);

        String logoSalva = DatabaseManager.obterConfiguracao("buffet.logo_path", "");
        txtCaminhoLogoBuffet = new JTextField(logoSalva);
        txtCaminhoLogoBuffet.putClientProperty("JTextField.placeholderText", "Padrão do Sistema (ou clique em 'Selecionar Imagem...' para customizar)");
        txtCaminhoLogoBuffet.setEditable(false);
        txtCaminhoLogoBuffet.setBackground(new Color(250, 252, 255));

        JPanel painelControlesLogo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painelControlesLogo.setOpaque(false);

        lblPreviewLogoBuffet = new JLabel();
        lblPreviewLogoBuffet.setPreferredSize(new Dimension(54, 28));
        lblPreviewLogoBuffet.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreviewLogoBuffet.setBorder(new CompoundBorder(
                new LineBorder(new Color(214, 226, 251), 1, true),
                new EmptyBorder(1, 4, 1, 4)
        ));
        lblPreviewLogoBuffet.setOpaque(true);
        lblPreviewLogoBuffet.setBackground(Color.WHITE);
        lblPreviewLogoBuffet.setToolTipText("Pré-visualização da Logomarca que será inserida como Marca D'Água no PDF");

        JButton btnSelecionarLogo = new JButton("📁 Selecionar Imagem...");
        btnSelecionarLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSelecionarLogo.setToolTipText("Escolher logomarca da sua empresa (PNG, JPG) para a Marca D'Água da OS");
        btnSelecionarLogo.addActionListener(e -> selecionarArquivoLogo());

        JButton btnResetarLogo = new JButton("↺ Padrão");
        btnResetarLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnResetarLogo.setToolTipText("Restaurar para a logomarca padrão do sistema");
        btnResetarLogo.addActionListener(e -> resetarArquivoLogo());

        painelControlesLogo.add(lblPreviewLogoBuffet);
        painelControlesLogo.add(btnSelecionarLogo);
        painelControlesLogo.add(btnResetarLogo);

        painelLogo.add(txtCaminhoLogoBuffet, BorderLayout.CENTER);
        painelLogo.add(painelControlesLogo, BorderLayout.EAST);
        painel.add(painelLogo, gbc);

        // Linha 2: Banner explicativo sobre a personalização e marca d'água da OS
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 6, 6, 6);
        JPanel painelDicaWatermark = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        painelDicaWatermark.setBackground(new Color(244, 247, 254));
        painelDicaWatermark.setBorder(new CompoundBorder(
                new LineBorder(new Color(214, 226, 251), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        lblMiniLogoBanner = new JLabel();
        painelDicaWatermark.add(lblMiniLogoBanner);

        JLabel lblDica = new JLabel("Personalização: O Nome e Slogan definem o cabeçalho. A Logomarca selecionada é aplicada como Marca D'Água no fundo de toda OS.");
        lblDica.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDica.setForeground(new Color(41, 128, 185));
        painelDicaWatermark.add(lblDica);
        painel.add(painelDicaWatermark, gbc);

        atualizarPreviewLogo(logoSalva);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Linha 3: Número da OS e Status
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.12;
        painel.add(new JLabel("Número da OS:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.38;
        txtNumeroOS = new JTextField(String.valueOf(DatabaseManager.gerarProximoNumeroOS()));
        txtNumeroOS.setEditable(false);
        txtNumeroOS.setBackground(new Color(248, 249, 250));
        painel.add(txtNumeroOS, gbc);

        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0.15;
        painel.add(new JLabel("Status da Reserva:"), gbc);

        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.35;
        cbStatusOS = new JComboBox<>(new String[]{"Orçamento", "Confirmado", "Realizado", "Cancelado"});
        painel.add(cbStatusOS, gbc);

        // Linha 4: Cliente e Telefone
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.12;
        painel.add(new JLabel("Cliente / Contratante: *"), gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.38;
        txtCliente = new JTextField();
        txtCliente.putClientProperty("JTextField.placeholderText", "Nome do cliente ou empresa");
        painel.add(txtCliente, gbc);

        gbc.gridx = 2; gbc.gridy = 4; gbc.weightx = 0.15;
        painel.add(new JLabel("Telefone / WhatsApp:"), gbc);

        gbc.gridx = 3; gbc.gridy = 4; gbc.weightx = 0.35;
        txtTelefone = new JTextField();
        txtTelefone.putClientProperty("JTextField.placeholderText", "(00) 00000-0000");
        painel.add(txtTelefone, gbc);

        // Linha 5: Data do Evento e Local
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.12;
        painel.add(new JLabel("Data do Evento (dd/MM/yyyy): *"), gbc);

        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.38;
        txtDataEvento = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtDataEvento.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
        painel.add(txtDataEvento, gbc);

        gbc.gridx = 2; gbc.gridy = 5; gbc.weightx = 0.15;
        painel.add(new JLabel("Local do Evento:"), gbc);

        gbc.gridx = 3; gbc.gridy = 5; gbc.weightx = 0.35;
        txtLocal = new JTextField("Espaço Festalia Garden - Salão Nobre");
        txtLocal.putClientProperty("JTextField.placeholderText", "Nome do salão ou endereço");
        painel.add(txtLocal, gbc);

        // Linha 6: Convidados e Preço/Pessoa
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.12;
        painel.add(new JLabel("Qtd. Convidados: *"), gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.38;
        txtConvidados = new JTextField("100");
        txtConvidados.putClientProperty("JTextField.placeholderText", "Ex: 100");
        painel.add(txtConvidados, gbc);

        gbc.gridx = 2; gbc.gridy = 6; gbc.weightx = 0.15;
        painel.add(new JLabel("Valor / Pessoa (R$): *"), gbc);

        gbc.gridx = 3; gbc.gridy = 6; gbc.weightx = 0.35;
        txtPrecoPorPessoa = new JTextField("125.00");
        txtPrecoPorPessoa.putClientProperty("JTextField.placeholderText", "0.00");
        painel.add(txtPrecoPorPessoa, gbc);

        // Linha 7: Taxa de Serviço (%) e Taxa de Transporte (R$)
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.12;
        painel.add(new JLabel("Taxa de Serviço (%):"), gbc);

        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 0.38;
        JPanel painelTaxaServico = new JPanel(new BorderLayout(8, 0));
        painelTaxaServico.setOpaque(false);

        txtTaxaServico = new JTextField("0.00");
        txtTaxaServico.putClientProperty("JTextField.placeholderText", "Ex: 10%");
        txtTaxaServico.setToolTipText("Percentual sobre o valor por pessoa (ex: 10% s/ R$ 185,00 = R$ 18,50 por convidado)");

        lblValorTaxaServicoCalculado = new JLabel("= R$ 0,00");
        lblValorTaxaServicoCalculado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValorTaxaServicoCalculado.setForeground(COR_AZUL_FESTALIA);
        lblValorTaxaServicoCalculado.setToolTipText("Valor total calculado da taxa de serviço para todos os convidados");

        painelTaxaServico.add(txtTaxaServico, BorderLayout.CENTER);
        painelTaxaServico.add(lblValorTaxaServicoCalculado, BorderLayout.EAST);
        painel.add(painelTaxaServico, gbc);

        gbc.gridx = 2; gbc.gridy = 7; gbc.weightx = 0.15;
        painel.add(new JLabel("Taxa Transporte (R$):"), gbc);

        gbc.gridx = 3; gbc.gridy = 7; gbc.weightx = 0.35;
        txtTaxaTransporte = new JTextField("0.00");
        txtTaxaTransporte.putClientProperty("JTextField.placeholderText", "0.00");
        painel.add(txtTaxaTransporte, gbc);

        // Linha 8: Card Financeiro de Destaque para o Valor Total
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4; gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 6, 2, 6);

        JPanel cardTotal = new JPanel(new BorderLayout(10, 2));
        cardTotal.setBackground(new Color(240, 253, 244));
        cardTotal.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        JLabel lblTagTotal = new JLabel("TOTAL ESTIMADO (BUFFET + TAXA DE SERVIÇO & TRANSPORTE):");
        lblTagTotal.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTagTotal.setForeground(new Color(22, 101, 52));

        lblValorTotalCalculado = new JLabel("R$ 0,00");
        lblValorTotalCalculado.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValorTotalCalculado.setForeground(new Color(21, 128, 61));

        cardTotal.add(lblTagTotal, BorderLayout.WEST);
        cardTotal.add(lblValorTotalCalculado, BorderLayout.EAST);
        painel.add(cardTotal, gbc);

        // Listeners para cálculo dinâmico do orçamento em tempo real
        DocumentListener calculoListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizarCalculoValorTotal(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarCalculoValorTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarCalculoValorTotal(); }
        };
        txtConvidados.getDocument().addDocumentListener(calculoListener);
        txtPrecoPorPessoa.getDocument().addDocumentListener(calculoListener);
        txtTaxaServico.getDocument().addDocumentListener(calculoListener);
        txtTaxaTransporte.getDocument().addDocumentListener(calculoListener);

        return painel;
    }

    private JPanel criarPainelCardapioEditavel() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(218, 224, 233), 1, true), " Seleção e Personalização do Cardápio ",
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COR_ESCURA),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel topo = new JPanel(new BorderLayout(6, 4));
        topo.setOpaque(false);

        cbCardapios = new JComboBox<>(listaCardapios.toArray(new Cardapio[0]));
        cbCardapios.addActionListener(e -> aoSelecionarCardapioModelo());

        JButton btnRestaurar = new JButton("Restaurar Padrão");
        btnRestaurar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRestaurar.setToolTipText("Restaura o modelo original deste cardápio");
        btnRestaurar.addActionListener(e -> carregarTextoCardapioPadrao());

        JPanel linhaCombo = new JPanel(new BorderLayout(6, 0));
        linhaCombo.setOpaque(false);
        linhaCombo.add(cbCardapios, BorderLayout.CENTER);
        linhaCombo.add(btnRestaurar, BorderLayout.EAST);

        JLabel lblDica = new JLabel("Você pode editar os itens abaixo para personalizar este evento:");
        lblDica.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDica.setForeground(Color.GRAY);

        topo.add(linhaCombo, BorderLayout.NORTH);
        topo.add(lblDica, BorderLayout.SOUTH);

        txtDescricaoCardapio = new JTextArea();
        txtDescricaoCardapio.setLineWrap(true);
        txtDescricaoCardapio.setWrapStyleWord(true);
        txtDescricaoCardapio.setMargin(new Insets(8, 10, 8, 10));
        txtDescricaoCardapio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtDescricaoCardapio);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);

        aoSelecionarCardapioModelo();
        return painel;
    }

    private JPanel criarPainelObservacoes() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBackground(Color.WHITE);
        painel.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(218, 224, 233), 1, true), " Observações e Diretrizes do Buffet ",
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COR_ESCURA),
                new EmptyBorder(8, 10, 8, 10)
        ));

        txtObservacoes = new JTextArea("Ex: Clientes solicitaram 10 opções vegetarianas e montagem do salão 3 horas antes.");
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setMargin(new Insets(8, 10, 8, 10));
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtObservacoes);

        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarBarraBotoesNovaOS() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        painel.setOpaque(false);

        JButton btnLimpar = new JButton("Limpar Formulário");
        btnLimpar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpar.addActionListener(e -> limparCamposNovaOS());

        JButton btnSalvarHistorico = new JButton("Salvar no Histórico");
        btnSalvarHistorico.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvarHistorico.putClientProperty("JButton.buttonType", "default");
        btnSalvarHistorico.addActionListener(e -> salvarOrdemServico(false));

        JButton btnGerarPdf = new JButton("Salvar & Gerar PDF da OS");
        btnGerarPdf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGerarPdf.putClientProperty("JButton.buttonType", "primary");
        btnGerarPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGerarPdf.addActionListener(e -> salvarOrdemServico(true));

        painel.add(btnLimpar);
        painel.add(btnSalvarHistorico);
        painel.add(btnGerarPdf);
        return painel;
    }

    private void aoSelecionarCardapioModelo() {
        Cardapio selecionado = (Cardapio) cbCardapios.getSelectedItem();
        if (selecionado != null) {
            txtPrecoPorPessoa.setText(String.format(Locale.US, "%.2f", selecionado.getPrecoPorPessoa()));
            carregarTextoCardapioPadrao();
        }
    }

    private void carregarTextoCardapioPadrao() {
        Cardapio selecionado = (Cardapio) cbCardapios.getSelectedItem();
        if (selecionado == null) {
            txtDescricaoCardapio.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (selecionado.getDescricao() != null && !selecionado.getDescricao().isBlank()) {
            sb.append(selecionado.getDescricao().trim()).append("\n\n");
        }
        sb.append("ITENS INCLUSOS NO MENU:\n");
        for (String item : selecionado.getItens()) {
            sb.append("• ").append(item).append("\n");
        }
        txtDescricaoCardapio.setText(sb.toString().trim());
        txtDescricaoCardapio.setCaretPosition(0);
    }

    private void atualizarCalculoValorTotal() {
        try {
            int convidados = Integer.parseInt(txtConvidados.getText().trim());
            double preco = Double.parseDouble(txtPrecoPorPessoa.getText().trim().replace(",", "."));
            double taxaServicoPerc = 0.0;
            if (txtTaxaServico != null && !txtTaxaServico.getText().trim().isEmpty()) {
                String strTaxa = txtTaxaServico.getText().trim().replace("%", "").replace(",", ".");
                taxaServicoPerc = Double.parseDouble(strTaxa);
            }
            double taxaTransporte = 0.0;
            if (txtTaxaTransporte != null && !txtTaxaTransporte.getText().trim().isEmpty()) {
                taxaTransporte = Double.parseDouble(txtTaxaTransporte.getText().trim().replace(",", "."));
            }

            double subtotalBuffet = Math.max(0, convidados) * Math.max(0.0, preco);
            double valorTaxaServico = subtotalBuffet * (Math.max(0.0, taxaServicoPerc) / 100.0);
            double total = subtotalBuffet + valorTaxaServico + Math.max(0.0, taxaTransporte);

            lblValorTotalCalculado.setText(MOEDA_BRL.format(total));

            if (lblValorTaxaServicoCalculado != null) {
                if (taxaServicoPerc > 0) {
                    lblValorTaxaServicoCalculado.setText("= " + MOEDA_BRL.format(valorTaxaServico));
                } else {
                    lblValorTaxaServicoCalculado.setText("= R$ 0,00");
                }
            }
        } catch (Exception e) {
            lblValorTotalCalculado.setText("R$ 0,00");
            if (lblValorTaxaServicoCalculado != null) {
                lblValorTaxaServicoCalculado.setText("= R$ 0,00");
            }
        }
    }

    private void salvarOrdemServico(boolean gerarPdf) {
        String cliente = txtCliente.getText().trim();
        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente / contratante.", "Aviso", JOptionPane.WARNING_MESSAGE);
            txtCliente.requestFocus();
            return;
        }

        LocalDate dataEvento;
        try {
            dataEvento = LocalDate.parse(txtDataEvento.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Data do evento inválida! Use o formato dd/MM/yyyy.", "Erro", JOptionPane.ERROR_MESSAGE);
            txtDataEvento.requestFocus();
            return;
        }

        int convidados;
        double precoPorPessoa;
        try {
            convidados = Integer.parseInt(txtConvidados.getText().trim());
            precoPorPessoa = Double.parseDouble(txtPrecoPorPessoa.getText().trim().replace(",", "."));
            if (convidados <= 0 || precoPorPessoa < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe números válidos para convidados e preço por pessoa.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double taxaServico = 0.0;
        double taxaTransporte = 0.0;
        try {
            if (txtTaxaServico != null && !txtTaxaServico.getText().trim().isEmpty()) {
                String strTaxa = txtTaxaServico.getText().trim().replace("%", "").replace(",", ".");
                taxaServico = Double.parseDouble(strTaxa);
                if (taxaServico < 0) throw new NumberFormatException();
            }
            if (txtTaxaTransporte != null && !txtTaxaTransporte.getText().trim().isEmpty()) {
                taxaTransporte = Double.parseDouble(txtTaxaTransporte.getText().trim().replace(",", "."));
                if (taxaTransporte < 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe valores válidos para as taxas de serviço (%) e transporte (R$).", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Cardapio cardapio = obterCardapioEditado();
        cardapio.setPrecoPorPessoa(precoPorPessoa);

        int numeroOS = Integer.parseInt(txtNumeroOS.getText().trim());
        String nomeBuffet = (txtNomeBuffet != null && !txtNomeBuffet.getText().trim().isEmpty())
                ? txtNomeBuffet.getText().trim() : "Festalia Buffet & Eventos";
        String subtituloBuffet = (txtSubtituloBuffet != null && !txtSubtituloBuffet.getText().trim().isEmpty())
                ? txtSubtituloBuffet.getText().trim() : "Gestão Profissional de Buffets e Eventos";
        String caminhoLogoBuffet = (txtCaminhoLogoBuffet != null && !txtCaminhoLogoBuffet.getText().trim().isEmpty())
                ? txtCaminhoLogoBuffet.getText().trim() : null;

        // Salva as preferências de identificação do buffet para persistir em futuras ordens
        DatabaseManager.salvarConfiguracao("buffet.nome", nomeBuffet);
        DatabaseManager.salvarConfiguracao("buffet.subtitulo", subtituloBuffet);
        if (caminhoLogoBuffet != null) {
            DatabaseManager.salvarConfiguracao("buffet.logo_path", caminhoLogoBuffet);
        }

        String telefone = txtTelefone.getText().trim();
        String local = txtLocal.getText().trim();
        String status = (String) cbStatusOS.getSelectedItem();
        String observacoes = txtObservacoes.getText().trim();

        double subtotalBuffet = convidados * precoPorPessoa;
        double valorTaxaServico = subtotalBuffet * (taxaServico / 100.0);
        double valorTotal = subtotalBuffet + valorTaxaServico + taxaTransporte;

        OrdemServico os = new OrdemServico(0, numeroOS, nomeBuffet, subtituloBuffet, caminhoLogoBuffet, cliente, telefone, dataEvento, local,
                convidados, precoPorPessoa, taxaServico, taxaTransporte, valorTotal, status, cardapio, observacoes, null);

        try {
            ordemServicoDAO.salvar(os);
            carregarTabelaHistorico();

            if (gerarPdf) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Salvar Ordem de Serviço em PDF");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf"));
                String nomeArquivo = String.format("Festalia_OS_%d_%s.pdf", os.getNumeroOS(), cliente.replaceAll("[^a-zA-Z0-9_-]", "_"));
                fileChooser.setSelectedFile(new File(nomeArquivo));

                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File destino = fileChooser.getSelectedFile();
                    if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                        destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
                    }
                    PDFGenerator.gerarOrdemServicoPDF(os, destino.getAbsolutePath());

                    int resp = JOptionPane.showConfirmDialog(this,
                            "OS Nº " + os.getNumeroOS() + " salva no banco e PDF gerado com sucesso!\n" +
                                    "Arquivo: " + destino.getAbsolutePath() + "\n\nDeseja abrir o PDF agora?",
                            "Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                    if (resp == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(destino);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Não foi possível abrir o PDF: " + ex.getMessage());
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ordem de Serviço Nº " + os.getNumeroOS() + " salva com sucesso no histórico!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }

            limparCamposNovaOS();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no banco SQLite: " + e.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Cardapio obterCardapioEditado() {
        Cardapio base = (Cardapio) cbCardapios.getSelectedItem();
        String nome = (base != null) ? base.getNome() : "Cardápio Personalizado";
        String texto = txtDescricaoCardapio.getText();

        Cardapio cardapio = new Cardapio(nome, base != null ? base.getDescricao() : "");
        cardapio.carregarItensDeTexto(texto);
        return cardapio;
    }

    private void limparCamposNovaOS() {
        txtNumeroOS.setText(String.valueOf(DatabaseManager.gerarProximoNumeroOS()));
        txtCliente.setText("");
        txtTelefone.setText("");
        txtDataEvento.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtConvidados.setText("100");
        txtLocal.setText("Espaço Festalia Garden - Salão Nobre");
        txtTaxaServico.setText("0.00");
        txtTaxaTransporte.setText("0.00");
        txtObservacoes.setText("");
        cbStatusOS.setSelectedIndex(0);
        if (cbCardapios.getItemCount() > 0) {
            cbCardapios.setSelectedIndex(0);
        }
        aoSelecionarCardapioModelo();
        atualizarCalculoValorTotal();
    }

    // =========================================================================
    // ABA 2: HISTÓRICO DE EVENTOS & FATURAMENTO
    // =========================================================================
    private JPanel criarAbaHistorico() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Topo: Barra de Pesquisa e Botões de Ação
        JPanel topo = new JPanel(new BorderLayout(10, 0));
        topo.setOpaque(false);

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelBusca.setOpaque(false);
        painelBusca.add(new JLabel("Pesquisar:"));
        txtBuscaHistorico = new JTextField(24);
        txtBuscaHistorico.putClientProperty("JTextField.placeholderText", "Filtrar por cliente, OS, local ou status...");
        txtBuscaHistorico.putClientProperty("JTextField.showClearButton", true);
        txtBuscaHistorico.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrarHistorico(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrarHistorico(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrarHistorico(); }
        });
        painelBusca.add(txtBuscaHistorico);

        JButton btnAtualizar = new JButton("Atualizar Lista");
        btnAtualizar.addActionListener(e -> carregarTabelaHistorico());
        painelBusca.add(btnAtualizar);

        topo.add(painelBusca, BorderLayout.WEST);

        // Ações da Tabela
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painelAcoes.setOpaque(false);

        JButton btnReemitirPdf = new JButton("Reemitir PDF da OS");
        btnReemitirPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReemitirPdf.putClientProperty("JButton.buttonType", "default");
        btnReemitirPdf.addActionListener(e -> reemitirPdfSelecionado());

        JButton btnMudarStatus = new JButton("Alterar Status");
        btnMudarStatus.putClientProperty("JButton.buttonType", "default");
        btnMudarStatus.addActionListener(e -> alterarStatusSelecionado());

        JButton btnExcluir = new JButton("Excluir OS");
        btnExcluir.setForeground(new Color(192, 57, 43));
        btnExcluir.addActionListener(e -> excluirOsSelecionada());

        painelAcoes.add(btnReemitirPdf);
        painelAcoes.add(btnMudarStatus);
        painelAcoes.add(btnExcluir);

        topo.add(painelAcoes, BorderLayout.EAST);
        painel.add(topo, BorderLayout.NORTH);

        // Tabela de Histórico
        String[] colunas = {"ID", "OS Nº", "Data Evento", "Cliente", "Telefone", "Local", "Convidados", "Cardápio", "Valor Total", "Status"};
        modelHistorico = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaHistorico = new JTable(modelHistorico);
        tabelaHistorico.setRowHeight(28);
        tabelaHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaHistorico.putClientProperty("JTable.alternateRowColor", true);

        // Esconde coluna interna ID
        tabelaHistorico.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaHistorico.getColumnModel().getColumn(0).setMaxWidth(0);
        tabelaHistorico.getColumnModel().getColumn(0).setWidth(0);

        // Larguras calibradas para evitar truncamento com reticências
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(75);   // OS Nº
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(95);   // Data Evento
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(170);  // Cliente
        tabelaHistorico.getColumnModel().getColumn(4).setPreferredWidth(115);  // Telefone
        tabelaHistorico.getColumnModel().getColumn(5).setPreferredWidth(200);  // Local
        tabelaHistorico.getColumnModel().getColumn(6).setPreferredWidth(85);   // Convidados
        tabelaHistorico.getColumnModel().getColumn(7).setPreferredWidth(165);  // Cardápio
        tabelaHistorico.getColumnModel().getColumn(8).setPreferredWidth(115);  // Valor Total
        tabelaHistorico.getColumnModel().getColumn(9).setPreferredWidth(100);  // Status

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);
        tabelaHistorico.getColumnModel().getColumn(1).setCellRenderer(centro);
        tabelaHistorico.getColumnModel().getColumn(2).setCellRenderer(centro);
        tabelaHistorico.getColumnModel().getColumn(4).setCellRenderer(centro);
        tabelaHistorico.getColumnModel().getColumn(6).setCellRenderer(centro);

        // Renderizador alinhado à direita para o valor financeiro
        DefaultTableCellRenderer valorTotalRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return c;
            }
        };
        tabelaHistorico.getColumnModel().getColumn(8).setCellRenderer(valorTotalRenderer);

        // Renderizador elegante de status com cores semafóricas
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (!isSelected && value != null) {
                    String st = value.toString();
                    if ("Confirmado".equalsIgnoreCase(st)) {
                        lbl.setForeground(new Color(21, 128, 61));      // Verde
                    } else if ("Orçamento".equalsIgnoreCase(st)) {
                        lbl.setForeground(new Color(29, 78, 216));     // Azul
                    } else if ("Realizado".equalsIgnoreCase(st)) {
                        lbl.setForeground(new Color(107, 114, 128));   // Cinza
                    } else if ("Cancelado".equalsIgnoreCase(st)) {
                        lbl.setForeground(new Color(185, 28, 28));     // Vermelho
                    }
                }
                return lbl;
            }
        };
        tabelaHistorico.getColumnModel().getColumn(9).setCellRenderer(statusRenderer);

        painel.add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);

        // Rodapé: Estatísticas de Faturamento
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE);
        rodape.setBorder(new CompoundBorder(new LineBorder(new Color(218, 224, 233)), new EmptyBorder(10, 14, 10, 14)));

        lblTotalEventos = new JLabel("Eventos listados: 0");
        lblTotalEventos.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblTotalFaturamento = new JLabel("Faturamento Ativo: R$ 0,00");
        lblTotalFaturamento.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalFaturamento.setForeground(new Color(21, 128, 61));

        rodape.add(lblTotalEventos, BorderLayout.WEST);
        rodape.add(lblTotalFaturamento, BorderLayout.EAST);
        painel.add(rodape, BorderLayout.SOUTH);

        return painel;
    }

    private void carregarTabelaHistorico() {
        filtrarHistorico();
    }

    private void filtrarHistorico() {
        String termo = (txtBuscaHistorico != null) ? txtBuscaHistorico.getText().trim() : "";
        List<OrdemServico> lista = ordemServicoDAO.buscarPorTermo(termo);

        modelHistorico.setRowCount(0);
        double faturamento = 0.0;

        for (OrdemServico os : lista) {
            modelHistorico.addRow(new Object[]{
                    os.getId(),
                    os.getNumeroOS(),
                    os.getDataEventoFormatada(),
                    os.getCliente(),
                    os.getTelefone(),
                    os.getLocal(),
                    os.getQuantidadeConvidados(),
                    os.getCardapioSelecionado() != null ? os.getCardapioSelecionado().getNome() : "—",
                    os.getValorTotalFormatado(),
                    os.getStatus()
            });
            if (!"Cancelado".equalsIgnoreCase(os.getStatus())) {
                faturamento += os.getValorTotal();
            }
        }

        if (lblTotalEventos != null) {
            lblTotalEventos.setText("Eventos listados: " + lista.size());
            lblTotalFaturamento.setText("Faturamento Ativo: " + MOEDA_BRL.format(faturamento));
        }
    }

    private void reemitirPdfSelecionado() {
        int row = tabelaHistorico.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Serviço na tabela.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int osId = (int) modelHistorico.getValueAt(row, 0);
        List<OrdemServico> todas = ordemServicoDAO.listarTodas();
        OrdemServico os = todas.stream().filter(o -> o.getId() == osId).findFirst().orElse(null);

        if (os == null) {
            JOptionPane.showMessageDialog(this, "Não foi possível carregar os dados desta OS.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Reemitir PDF da Ordem de Serviço");
        fileChooser.setSelectedFile(new File("Festalia_OS_" + os.getNumeroOS() + ".pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fileChooser.getSelectedFile();
            try {
                PDFGenerator.gerarOrdemServicoPDF(os, destino.getAbsolutePath());
                int resp = JOptionPane.showConfirmDialog(this, "PDF reemitido com sucesso!\nDeseja abrir o arquivo agora?", "Sucesso", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(destino);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void alterarStatusSelecionado() {
        int row = tabelaHistorico.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS para alterar o status.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) modelHistorico.getValueAt(row, 0);
        String statusAtual = (String) modelHistorico.getValueAt(row, 9);

        String[] opcoes = {"Orçamento", "Confirmado", "Realizado", "Cancelado"};
        String novoStatus = (String) JOptionPane.showInputDialog(this,
                "Selecione o novo status da Ordem de Serviço:",
                "Alterar Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                statusAtual);

        if (novoStatus != null && !novoStatus.equals(statusAtual)) {
            try {
                ordemServicoDAO.atualizarStatus(id, novoStatus);
                carregarTabelaHistorico();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar status: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirOsSelecionada() {
        int row = tabelaHistorico.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma OS para excluir.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int id = (int) modelHistorico.getValueAt(row, 0);
        int numeroOS = (int) modelHistorico.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir permanentemente a OS Nº " + numeroOS + " do histórico?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ordemServicoDAO.excluir(id);
                carregarTabelaHistorico();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir OS: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // ABA 3: PORTFÓLIO DE CARDÁPIOS & PREÇOS
    // =========================================================================
    private JPanel criarAbaPortfólio() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Divisão: Tabela à esquerda, Formulário à direita
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);
        split.setContinuousLayout(true);

        // Painel Esquerdo: Lista de Cardápios
        JPanel painelEsquerdo = new JPanel(new BorderLayout(6, 6));
        painelEsquerdo.setBackground(Color.WHITE);
        painelEsquerdo.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(218, 224, 233)), " Repertório de Cardápios Cadastrados ",
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COR_ESCURA),
                new EmptyBorder(8, 8, 8, 8)
        ));

        String[] cols = {"ID", "Cardápio", "Preço Base / Pessoa"};
        modelCardapios = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCardapios = new JTable(modelCardapios);
        tabelaCardapios.setRowHeight(28);
        tabelaCardapios.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaCardapios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaCardapios.putClientProperty("JTable.alternateRowColor", true);
        tabelaCardapios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) carregarCardapioParaEdicao();
        });

        // Oculta coluna ID
        tabelaCardapios.getColumnModel().getColumn(0).setMinWidth(0);
        tabelaCardapios.getColumnModel().getColumn(0).setMaxWidth(0);

        tabelaCardapios.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabelaCardapios.getColumnModel().getColumn(2).setPreferredWidth(120);

        DefaultTableCellRenderer precoRenderer = new DefaultTableCellRenderer();
        precoRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tabelaCardapios.getColumnModel().getColumn(2).setCellRenderer(precoRenderer);

        painelEsquerdo.add(new JScrollPane(tabelaCardapios), BorderLayout.CENTER);

        JButton btnNovo = new JButton("+ Novo Cardápio");
        btnNovo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNovo.putClientProperty("JButton.buttonType", "default");
        btnNovo.addActionListener(e -> limparFormularioPortfólio());
        painelEsquerdo.add(btnNovo, BorderLayout.SOUTH);

        // Painel Direito: Formulário de Edição
        JPanel painelDireito = new JPanel(new BorderLayout(8, 8));
        painelDireito.setBackground(Color.WHITE);
        painelDireito.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(218, 224, 233)), " Detalhes do Cardápio & Tabela de Preço ",
                        TitledBorder.LEADING, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), COR_ESCURA),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel formCampos = new JPanel(new GridLayout(3, 2, 8, 8));
        formCampos.setOpaque(false);

        formCampos.add(new JLabel("Nome do Cardápio: *"));
        txtPortNome = new JTextField();
        txtPortNome.putClientProperty("JTextField.placeholderText", "Ex: Churrasco Prime Especial");
        formCampos.add(txtPortNome);

        formCampos.add(new JLabel("Preço Base por Pessoa (R$): *"));
        txtPortPreco = new JTextField();
        txtPortPreco.putClientProperty("JTextField.placeholderText", "0.00");
        formCampos.add(txtPortPreco);

        formCampos.add(new JLabel("Descrição Comercial:"));
        txtPortDescricao = new JTextField();
        txtPortDescricao.putClientProperty("JTextField.placeholderText", "Resumo das características do menu");
        formCampos.add(txtPortDescricao);

        painelDireito.add(formCampos, BorderLayout.NORTH);

        JPanel painelItens = new JPanel(new BorderLayout(4, 4));
        painelItens.setOpaque(false);
        painelItens.add(new JLabel("Itens do Cardápio (um por linha):"), BorderLayout.NORTH);
        txtPortItens = new JTextArea();
        txtPortItens.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPortItens.setMargin(new Insets(8, 10, 8, 10));
        painelItens.add(new JScrollPane(txtPortItens), BorderLayout.CENTER);

        painelDireito.add(painelItens, BorderLayout.CENTER);

        // Botões Salvar e Excluir
        JPanel botoesPort = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoesPort.setOpaque(false);

        btnExcluirCardapio = new JButton("Excluir Cardápio");
        btnExcluirCardapio.setForeground(new Color(192, 57, 43));
        btnExcluirCardapio.setEnabled(false);
        btnExcluirCardapio.addActionListener(e -> excluirCardapioPortfólio());

        JButton btnSalvarCardapio = new JButton("Salvar no Banco SQLite");
        btnSalvarCardapio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvarCardapio.putClientProperty("JButton.buttonType", "primary");
        btnSalvarCardapio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvarCardapio.addActionListener(e -> salvarCardapioPortfólio());

        botoesPort.add(btnExcluirCardapio);
        botoesPort.add(btnSalvarCardapio);
        painelDireito.add(botoesPort, BorderLayout.SOUTH);

        split.setLeftComponent(painelEsquerdo);
        split.setRightComponent(painelDireito);
        painel.add(split, BorderLayout.CENTER);

        return painel;
    }

    private void carregarTabelaPortfólio() {
        modelCardapios.setRowCount(0);
        recarregarListaCardapios();

        for (Cardapio c : listaCardapios) {
            modelCardapios.addRow(new Object[]{c.getId(), c.getNome(), c.getPrecoPorPessoaFormatado()});
        }

        // Atualiza também o combo da Aba 1
        if (cbCardapios != null) {
            cbCardapios.setModel(new DefaultComboBoxModel<>(listaCardapios.toArray(new Cardapio[0])));
        }
    }

    private void carregarCardapioParaEdicao() {
        int row = tabelaCardapios.getSelectedRow();
        if (row >= 0) {
            int id = (int) modelCardapios.getValueAt(row, 0);
            Cardapio c = cardapioDAO.buscarPorId(id);
            if (c != null) {
                idCardapioEmEdicao = c.getId();
                txtPortNome.setText(c.getNome());
                txtPortPreco.setText(String.format(Locale.US, "%.2f", c.getPrecoPorPessoa()));
                txtPortDescricao.setText(c.getDescricao());
                txtPortItens.setText(c.getItensComoTexto());
                btnExcluirCardapio.setEnabled(true);
            }
        }
    }

    private void limparFormularioPortfólio() {
        idCardapioEmEdicao = 0;
        txtPortNome.setText("");
        txtPortPreco.setText("");
        txtPortDescricao.setText("");
        txtPortItens.setText("");
        btnExcluirCardapio.setEnabled(false);
        tabelaCardapios.clearSelection();
    }

    private void salvarCardapioPortfólio() {
        String nome = txtPortNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cardápio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(txtPortPreco.getText().trim().replace(",", "."));
            if (preco < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um preço válido por pessoa.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String desc = txtPortDescricao.getText().trim();
        Cardapio c = new Cardapio(idCardapioEmEdicao, nome, desc, preco, new ArrayList<>());
        c.carregarItensDeTexto(txtPortItens.getText());

        try {
            cardapioDAO.salvar(c);
            JOptionPane.showMessageDialog(this, "Cardápio '" + c.getNome() + "' salvo com sucesso no banco!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            carregarTabelaPortfólio();
            limparFormularioPortfólio();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar cardápio no SQLite: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirCardapioPortfólio() {
        if (idCardapioEmEdicao <= 0) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir este cardápio?", "Excluir Cardápio", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                cardapioDAO.excluir(idCardapioEmEdicao);
                carregarTabelaPortfólio();
                limparFormularioPortfólio();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir cardápio: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // UTILITÁRIOS VISUAIS E GERENCIAMENTO DA LOGOMARCA / MARCA D'ÁGUA
    // =========================================================================

    private void selecionarArquivoLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar Logomarca do Buffet para Marca D'Água");
        chooser.setFileFilter(new FileNameExtensionFilter("Imagens (*.png, *.jpg, *.jpeg, *.bmp)", "png", "jpg", "jpeg", "bmp"));
        
        File dirInicial = new File(".");
        chooser.setCurrentDirectory(dirInicial);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            if (arquivo != null && arquivo.exists() && arquivo.isFile()) {
                String caminho = arquivo.getAbsolutePath();
                txtCaminhoLogoBuffet.setText(caminho);
                DatabaseManager.salvarConfiguracao("buffet.logo_path", caminho);
                atualizarPreviewLogo(caminho);
                JOptionPane.showMessageDialog(this,
                        "Logomarca do buffet definida com sucesso!\n" +
                        "Arquivo: " + arquivo.getName() + "\n\n" +
                        "Ela será aplicada como Marca D'Água translúcida ao fundo de todas as Ordens de Serviço geradas.",
                        "Logomarca Atualizada", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void resetarArquivoLogo() {
        txtCaminhoLogoBuffet.setText("");
        DatabaseManager.salvarConfiguracao("buffet.logo_path", "");
        atualizarPreviewLogo(null);
        JOptionPane.showMessageDialog(this,
                "Logomarca restaurada para o modelo padrão do sistema.\n" +
                "Caso deseje personalizar, clique novamente em 'Selecionar Imagem...'.",
                "Logomarca Restaurada", JOptionPane.INFORMATION_MESSAGE);
    }

    private void atualizarPreviewLogo(String caminho) {
        ImageIcon iconePreview = carregarIconeLogo(caminho, 48, 24);
        if (lblPreviewLogoBuffet != null) {
            if (iconePreview != null) {
                lblPreviewLogoBuffet.setIcon(iconePreview);
                lblPreviewLogoBuffet.setText("");
            } else {
                lblPreviewLogoBuffet.setIcon(null);
                lblPreviewLogoBuffet.setText("Logo");
                lblPreviewLogoBuffet.setFont(new Font("Segoe UI", Font.BOLD, 10));
                lblPreviewLogoBuffet.setForeground(new Color(120, 140, 160));
            }
        }

        if (lblMiniLogoBanner != null) {
            ImageIcon mini = carregarIconeLogo(caminho, 22, 22);
            if (mini != null) {
                lblMiniLogoBanner.setIcon(mini);
            }
        }
    }

    public static void garantirAssetsIniciais() {
        try {
            // Cria pastas se não existirem
            File resDir = new File("src/main/resources");
            if (!resDir.exists()) {
                resDir.mkdirs();
            }

            // Origem da logo Mendez Buffet enviada pelo usuário no artifact storage
            String origemMendez = "C:/Users/matheus.souza/.gemini/antigravity-ide/brain/6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61/.user_uploaded/media_1790705846854.jpg";
            File fOrigem = new File(origemMendez);
            if (fOrigem.exists() && fOrigem.isFile() && fOrigem.length() > 0) {
                File dest1 = new File("mendez_logo.png");
                if (!dest1.exists() || dest1.length() == 0) {
                    Files.copy(fOrigem.toPath(), dest1.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                File dest2 = new File("src/main/resources/mendez_logo.png");
                if (!dest2.exists() || dest2.length() == 0) {
                    Files.copy(fOrigem.toPath(), dest2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso ao sincronizar assets: " + e.getMessage());
        }
    }

    public static Image carregarImagemLogoRaw() {
        return carregarImagemLogoRaw(null);
    }

    public static Image carregarImagemLogoRaw(String caminhoCustomizado) {
        // 1. Tenta carregar caminho explicitamente informado
        if (caminhoCustomizado != null && !caminhoCustomizado.trim().isEmpty()) {
            try {
                File f = new File(caminhoCustomizado.trim());
                if (f.exists() && f.isFile() && f.length() > 0) {
                    return new ImageIcon(f.getAbsolutePath()).getImage();
                }
            } catch (Exception ignored) {}
        }

        // 2. Tenta carregar o caminho salvo nas configurações do buffet
        try {
            String configLogo = DatabaseManager.obterConfiguracao("buffet.logo_path", null);
            if (configLogo != null && !configLogo.trim().isEmpty()) {
                File fConfig = new File(configLogo.trim());
                if (fConfig.exists() && fConfig.isFile() && fConfig.length() > 0) {
                    return new ImageIcon(fConfig.getAbsolutePath()).getImage();
                }
            }
        } catch (Exception ignored) {}

        // 3. Tenta carregar do classpath (mendez_logo.png ou festalia_logo.jpg)
        String[] recursos = {"/mendez_logo.png", "/festalia_logo.jpg", "mendez_logo.png", "festalia_logo.jpg"};
        for (String rec : recursos) {
            try {
                java.net.URL url = BuffetErpApp.class.getResource(rec.startsWith("/") ? rec : "/" + rec);
                if (url == null) {
                    url = BuffetErpApp.class.getClassLoader().getResource(rec);
                }
                if (url != null) {
                    return new ImageIcon(url).getImage();
                }
            } catch (Exception ignored) {}
        }

        // 4. Múltiplos caminhos de arquivos locais de desenvolvimento
        String[] caminhos = {
            "mendez_logo.png",
            "src/main/resources/mendez_logo.png",
            "target/classes/mendez_logo.png",
            "C:/Users/matheus.souza/.gemini/antigravity-ide/brain/6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61/.user_uploaded/media_1790705846854.jpg",
            "festalia_logo.jpg",
            "src/main/resources/festalia_logo.jpg",
            "target/classes/festalia_logo.jpg",
            "C:/Users/matheus.souza/Documents/Programa do BUFFET GUI/mendez_logo.png",
            "C:/Users/matheus.souza/Documents/Programa do BUFFET GUI/festalia_logo.jpg",
            "C:/Users/matheus.souza/.gemini/antigravity-ide/brain/6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61/.user_uploaded/media_1790699283882.jpg"
        };

        for (String caminho : caminhos) {
            try {
                File f = new File(caminho);
                if (f.exists() && f.isFile() && f.length() > 0) {
                    return new ImageIcon(f.getAbsolutePath()).getImage();
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    public static ImageIcon carregarIconeLogo(int width, int height) {
        return carregarIconeLogo(null, width, height);
    }

    public static ImageIcon carregarIconeLogo(String caminhoCustomizado, int width, int height) {
        Image raw = carregarImagemLogoRaw(caminhoCustomizado);
        if (raw == null) {
            return null;
        }
        java.awt.image.BufferedImage resized = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(raw, 0, 0, width, height, null);
        g2.dispose();
        return new ImageIcon(resized);
    }

    // =========================================================================
    // MÉTODO MAIN
    // =========================================================================
    public static void main(String[] args) {
        try {
            // Tenta inicializar o Look and Feel moderno FlatLaf
            com.formdev.flatlaf.FlatLightLaf.setup();

            // Configurações e arredondamentos visuais do FlatLaf
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabHeight", 34);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("Table.rowHeight", 26);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        } catch (Throwable t) {
            // Fallback caso a biblioteca FlatLaf não esteja disponível
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            BuffetErpApp app = new BuffetErpApp();
            app.setVisible(true);
        });
    }
}
