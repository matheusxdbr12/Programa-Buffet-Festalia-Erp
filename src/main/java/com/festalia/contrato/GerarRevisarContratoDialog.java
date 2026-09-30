package com.festalia.contrato;

import com.festalia.DatabaseManager;
import com.festalia.OrdemServico;
import com.festalia.OrdemServicoDAO;
import com.festalia.financeiro.FinanceiroDAO;
import com.festalia.financeiro.ParcelaOS;
import com.festalia.util.IconesFestalia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Diálogo Modal para Revisão, Customização de Cláusulas e Emissão de Contrato de Prestação de Serviços.
 * Conecta Ordem de Serviço, Dados do Cliente, Dados da Empresa, Cronograma Financeiro e PDF Generator.
 */
public class GerarRevisarContratoDialog extends JDialog {

    private final Window parentWindow;
    private final int osId;
    private Contrato contrato;
    private boolean contratoGeradoComSucesso = false;

    // Componentes do Cabeçalho e Validação
    private JPanel painelAlertaValidacao;
    private JLabel lblStatusBadge;
    private JLabel lblNumeroContrato;
    private JLabel lblNumeroOs;

    // Alocação e Seleção de Ordem de Serviço (OS)
    private JComboBox<ItemOS> cbOrdemServico;
    private JLabel lblStatusOsVinculada;
    private JTextField txtBuscarNumeroOs;

    // Seletor de Modelo e Customização
    private JComboBox<ModeloContrato> cbModelo;
    private JCheckBox chkCancelamento;
    private JCheckBox chkArrependimentoCdc;
    private JCheckBox chkDanosLoucas;
    private JCheckBox chkLgpd;
    private JCheckBox chkCronogramaFinanceiro;
    private JCheckBox chkTestemunhas;

    // Campos de Qualificação Editáveis do Contratante
    private JTextField txtClienteNome;
    private JTextField txtClienteCpfCnpj;
    private JTextField txtClienteRg;
    private JTextField txtClienteEndereco;
    private JTextField txtClienteEmail;
    private JTextField txtClienteTelefone;

    // Campos de Qualificação Editáveis da Contratada (Buffet)
    private JTextField txtEmpresaRazaoSocial;
    private JTextField txtEmpresaCnpj;
    private JTextField txtEmpresaEndereco;
    private JTextField txtEmpresaRepresentante;
    private JTextField txtEmpresaForo;
    private JTextField txtEmpresaTelefone;

    // Campos do Evento (visualização e edição leve)
    private JTextField txtTipoEvento;
    private JTextField txtDataEvento;
    private JTextField txtHorarioEvento;
    private JTextField txtLocalEvento;
    private JTextField txtNumConvidados;
    private JTextField txtValorTotal;
    private JTextArea txtServicosCardapio;
    private JTextArea txtObservacoesEspecificas;

    // Visualização da Minuta
    private JTextArea txtPreviaMinuta;

    // Botões
    private JButton btnSalvarRascunho;
    private JButton btnGerarPdf;
    private JButton btnAtualizarPrevia;

    public GerarRevisarContratoDialog(Window parent, int osId) {
        super(parent, "Minuta de Contrato de Prestação de Serviços — Festalia ERP", ModalityType.APPLICATION_MODAL);
        this.parentWindow = parent;
        this.osId = osId;

        carregarDadosContrato();
        construirInterface();
        atualizarPreviaTexto();
        executarValidacaoVisual();

        setSize(1150, 780);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(parent);
    }

    private void carregarDadosContrato() {
        // Tenta carregar contrato já existente para esta OS
        this.contrato = ContratoDAO.obterPorOsId(osId);

        if (this.contrato == null) {
            // Cria um novo rascunho de contrato a partir da OS e da Empresa
            this.contrato = new Contrato();
            this.contrato.setOsId(osId);
            this.contrato.setNumeroContrato(DatabaseManager.gerarProximoNumeroContrato());
            this.contrato.setVersaoAtual("1.0");
            this.contrato.setStatus(StatusContrato.RASCUNHO);
            this.contrato.setModelo(ModeloContrato.BUFFET_COMPLETO);

            // Carrega dados da OS
            carregarDadosOsNoContrato(this.contrato, osId);

            // Carrega dados da Empresa cadastrados nas configurações
            carregarDadosEmpresaNoContrato(this.contrato);

            // Inicializa cláusulas padrão do modelo
            this.contrato.setClausulas(ContratoTemplateEngine.criarClausulasPadrao(this.contrato.getModelo()));
        } else {
            // Já existe contrato: se as cláusulas estiverem vazias, recria
            if (this.contrato.getClausulas() == null || this.contrato.getClausulas().isEmpty()) {
                this.contrato.setClausulas(ContratoTemplateEngine.criarClausulasPadrao(this.contrato.getModelo()));
            }
        }
    }

    private void carregarDadosOsNoContrato(Contrato c, int osId) {
        if (osId <= 0) return;
        OrdemServicoDAO dao = new OrdemServicoDAO();
        OrdemServico os = dao.buscarPorNumeroOS(osId);
        if (os == null) {
            os = dao.buscarPorId(osId);
        }
        if (os != null) {
            c.setOrdemServicoId(os.getId());
            c.setNumeroOS(os.getNumeroOS());
            if (c.getClienteNome() == null || c.getClienteNome().isBlank()) c.setClienteNome(os.getCliente());
            if (c.getClienteTelefone() == null || c.getClienteTelefone().isBlank()) c.setClienteTelefone(os.getTelefone());
            c.setTipoEvento(os.getTipoEvento() != null ? os.getTipoEvento() : "Evento Social");
            c.setDataEvento(os.getDataEvento());
            String hor = os.getHorarioInicioFormatado() + " às " + os.getHorarioFimFormatado();
            c.setHorarioEvento(hor);
            c.setLocalEvento(os.getLocal() != null ? os.getLocal() : "Espaço Festalia");
            c.setNumeroConvidados(os.getQuantidadeConvidados());
            c.setValorTotal(os.getValorTotal());
            if (os.getCardapioSelecionado() != null) {
                String descCard = os.getCardapioSelecionado().getDescricao() != null && !os.getCardapioSelecionado().getDescricao().isBlank()
                        ? "\n" + os.getCardapioSelecionado().getDescricao() : "";
                c.setCardapioServicos(os.getCardapioSelecionado().getNome() + descCard);
            }
            c.setObservacoesEspecificas(os.getObservacoes() != null ? os.getObservacoes() : "");
        }
    }

    public void vincularOrdemServico(OrdemServico os) {
        if (os == null) {
            if (lblStatusOsVinculada != null) {
                lblStatusOsVinculada.setText("  Nenhuma Ordem de Serviço alocada para este contrato.");
                lblStatusOsVinculada.setForeground(new Color(185, 28, 28));
                lblStatusOsVinculada.setIcon(IconesFestalia.alerta(14, new Color(185, 28, 28)));
            }
            if (lblNumeroOs != null) {
                lblNumeroOs.setText("[ Nenhuma OS Alocada ]");
            }
            contrato.setOrdemServicoId(0);
            contrato.setNumeroOS(0);
            executarValidacaoVisual();
            return;
        }

        contrato.setOrdemServicoId(os.getId());
        contrato.setNumeroOS(os.getNumeroOS());
        contrato.setClienteNome(os.getCliente());
        contrato.setClienteTelefone(os.getTelefone());
        contrato.setTipoEvento(os.getTipoEvento() != null ? os.getTipoEvento() : "Evento Social");
        contrato.setDataEvento(os.getDataEvento());
        String hor = os.getHorarioInicioFormatado() + " às " + os.getHorarioFimFormatado();
        contrato.setHorarioEvento(hor);
        contrato.setLocalEvento(os.getLocal() != null ? os.getLocal() : "Espaço Festalia");
        contrato.setNumeroConvidados(os.getQuantidadeConvidados());
        contrato.setValorTotal(os.getValorTotal());
        if (os.getCardapioSelecionado() != null) {
            String descCard = os.getCardapioSelecionado().getDescricao() != null && !os.getCardapioSelecionado().getDescricao().isBlank()
                    ? "\n" + os.getCardapioSelecionado().getDescricao() : "";
            contrato.setCardapioServicos(os.getCardapioSelecionado().getNome() + descCard);
        }
        contrato.setObservacoesEspecificas(os.getObservacoes() != null ? os.getObservacoes() : "");

        // Sincroniza campos visuais das abas
        if (txtClienteNome != null) txtClienteNome.setText(contrato.getClienteNome());
        if (txtClienteTelefone != null) txtClienteTelefone.setText(contrato.getClienteTelefone());
        if (txtTipoEvento != null) txtTipoEvento.setText(contrato.getTipoEvento());
        if (txtDataEvento != null) txtDataEvento.setText(contrato.getDataEventoFormatada());
        if (txtHorarioEvento != null) txtHorarioEvento.setText(contrato.getHorarioEvento());
        if (txtLocalEvento != null) txtLocalEvento.setText(contrato.getLocalEvento());
        if (txtNumConvidados != null) txtNumConvidados.setText(String.valueOf(contrato.getNumeroConvidados()));
        if (txtValorTotal != null) txtValorTotal.setText(String.format(Locale.US, "%.2f", contrato.getValorTotal()));
        if (txtServicosCardapio != null) txtServicosCardapio.setText(contrato.getCardapioServicos());
        if (txtObservacoesEspecificas != null) txtObservacoesEspecificas.setText(contrato.getObservacoesEspecificas());

        if (lblNumeroOs != null) {
            lblNumeroOs.setText("[ Vinculado à OS #" + String.format("%04d", os.getNumeroOS()) + " ]");
        }
        if (lblStatusOsVinculada != null) {
            lblStatusOsVinculada.setText(String.format("  OS #%04d alocada com sucesso — Cliente: %s — Data: %s",
                    os.getNumeroOS(), os.getCliente(), os.getDataEventoFormatada()));
            lblStatusOsVinculada.setForeground(new Color(22, 101, 52));
            lblStatusOsVinculada.setIcon(IconesFestalia.confirmar(14, new Color(22, 101, 52)));
        }

        atualizarPreviaTexto();
        executarValidacaoVisual();
    }

    private void carregarDadosEmpresaNoContrato(Contrato c) {
        String sql = "SELECT chave, valor FROM configuracoes_empresa";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String k = rs.getString("chave");
                String v = rs.getString("valor");
                if ("empresa_razao_social".equals(k)) c.setEmpresaRazaoSocial(v);
                else if ("empresa_nome_fantasia".equals(k)) c.setEmpresaNomeFantasia(v);
                else if ("empresa_cnpj".equals(k)) c.setEmpresaCnpj(v);
                else if ("empresa_endereco".equals(k)) c.setEmpresaEndereco(v);
                else if ("empresa_telefone".equals(k)) c.setEmpresaTelefone(v);
                else if ("empresa_email".equals(k)) c.setEmpresaEmail(v);
                else if ("empresa_representante".equals(k)) c.setEmpresaRepresentante(v);
                else if ("empresa_foro_comarca".equals(k)) c.setEmpresaForoComarca(v);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao carregar configurações da empresa: " + ex.getMessage());
        }
    }

    private void construirInterface() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(8, 8));
        painelPrincipal.setBorder(new EmptyBorder(12, 16, 12, 16));
        painelPrincipal.setBackground(new Color(248, 249, 250));

        // 1. TOPO: Identificação e Alerta de Validação
        painelPrincipal.add(construirPainelTopo(), BorderLayout.NORTH);

        // 2. CENTRO: Divisão entre Dados de Entrada/Customização e Prévia da Minuta
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.46);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(construirPainelEdicaoEClausulas());
        splitPane.setRightComponent(construirPainelPrevia());

        painelPrincipal.add(splitPane, BorderLayout.CENTER);

        // 3. RODAPÉ: Botões de Ação
        painelPrincipal.add(construirPainelRodape(), BorderLayout.SOUTH);

        setContentPane(painelPrincipal);
    }

    private JPanel construirPainelTopo() {
        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.setOpaque(false);

        // Barra de Título e Badges
        JPanel headerInfo = new JPanel(new BorderLayout());
        headerInfo.setOpaque(false);

        JPanel infoEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        infoEsquerda.setOpaque(false);

        JLabel icone = new JLabel(IconesFestalia.contrato(28, new Color(41, 128, 185)));
        JLabel lblTitulo = new JLabel("Minuta de Contrato de Prestação de Serviços");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(44, 62, 80));

        lblNumeroContrato = new JLabel("Nº " + (contrato.getNumeroContrato() != null ? contrato.getNumeroContrato() : "NOVO"));
        lblNumeroContrato.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNumeroContrato.setForeground(new Color(41, 128, 185));

        lblNumeroOs = new JLabel("[ Vinculado à OS #" + String.format("%04d", osId) + " ]");
        lblNumeroOs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNumeroOs.setForeground(new Color(100, 116, 139));

        infoEsquerda.add(icone);
        infoEsquerda.add(lblTitulo);
        infoEsquerda.add(lblNumeroContrato);
        infoEsquerda.add(lblNumeroOs);

        // Badges da Direita (Status e Versão)
        JPanel infoDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoDireita.setOpaque(false);

        lblStatusBadge = new JLabel(contrato.getStatus().getDescricao());
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(contrato.getStatus().getColorFundo());
        lblStatusBadge.setForeground(contrato.getStatus().getColorTexto());
        lblStatusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(contrato.getStatus().getColorTexto(), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel lblVersao = new JLabel("Versão " + contrato.getVersaoAtual());
        lblVersao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVersao.setForeground(new Color(71, 85, 105));
        lblVersao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        infoDireita.add(lblStatusBadge);
        infoDireita.add(lblVersao);

        headerInfo.add(infoEsquerda, BorderLayout.WEST);
        headerInfo.add(infoDireita, BorderLayout.EAST);

        topo.add(headerInfo, BorderLayout.NORTH);

        // Banner de Alerta de Validação (Invisível por padrão até verificação)
        painelAlertaValidacao = new JPanel(new BorderLayout(8, 4));
        painelAlertaValidacao.setBackground(new Color(254, 242, 242));
        painelAlertaValidacao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(239, 68, 68), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        painelAlertaValidacao.setVisible(false);

        topo.add(painelAlertaValidacao, BorderLayout.SOUTH);

        return topo;
    }

    private JComponent construirPainelEdicaoEClausulas() {
        JTabbedPane abasEdicao = new JTabbedPane();
        abasEdicao.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Aba 1: Qualificação do Cliente e Dados da Empresa
        abasEdicao.addTab("  Qualificação das Partes", IconesFestalia.cliente(16, new Color(41, 128, 185)), construirAbaQualificacao());

        // Aba 2: Dados do Evento e Valores
        abasEdicao.addTab("  Dados do Evento", IconesFestalia.calendario(16, new Color(22, 163, 74)), construirAbaEvento());

        // Aba 3: Personalização de Cláusulas (Regra #7)
        abasEdicao.addTab("  Cláusulas & Opções", IconesFestalia.balanca(16, new Color(147, 51, 234)), construirAbaClausulas());

        return abasEdicao;
    }

    private JComponent construirAbaQualificacao() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // --- 1. DADOS DO CONTRATANTE (CLIENTE) ---
        JPanel pHeaderCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pHeaderCliente.setOpaque(false);
        pHeaderCliente.add(new JLabel(IconesFestalia.cliente(16, new Color(41, 128, 185))));
        JLabel lblContratante = new JLabel("DADOS DO CONTRATANTE (CLIENTE)");
        lblContratante.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblContratante.setForeground(new Color(41, 128, 185));
        pHeaderCliente.add(lblContratante);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        p.add(pHeaderCliente, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Nome / Razão Social: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteNome = new JTextField(contrato.getClienteNome() != null ? contrato.getClienteNome() : "");
        p.add(txtClienteNome, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("CPF / CNPJ: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteCpfCnpj = new JTextField(contrato.getClienteCpfCnpj() != null ? contrato.getClienteCpfCnpj() : "");
        p.add(txtClienteCpfCnpj, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("RG / Doc. Identidade:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteRg = new JTextField(contrato.getClienteRg() != null ? contrato.getClienteRg() : "");
        p.add(txtClienteRg, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Endereço Completo: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteEndereco = new JTextField(contrato.getClienteEndereco() != null ? contrato.getClienteEndereco() : "");
        p.add(txtClienteEndereco, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("E-mail de Contato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteEmail = new JTextField(contrato.getClienteEmail() != null ? contrato.getClienteEmail() : "");
        p.add(txtClienteEmail, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Telefone / WhatsApp:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtClienteTelefone = new JTextField(contrato.getClienteTelefone() != null ? contrato.getClienteTelefone() : "");
        p.add(txtClienteTelefone, gbc);
        row++;

        // Divisor Elegante
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 4, 10, 4);
        p.add(new JSeparator(), gbc);
        gbc.insets = new Insets(3, 4, 3, 4);

        // --- 2. DADOS DA CONTRATADA (BUFFET) ---
        JPanel pHeaderEmpresa = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pHeaderEmpresa.setOpaque(false);
        pHeaderEmpresa.add(new JLabel(IconesFestalia.contrato(16, new Color(41, 128, 185))));
        JLabel lblContratada = new JLabel("DADOS DA CONTRATADA (BUFFET)");
        lblContratada.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblContratada.setForeground(new Color(41, 128, 185));
        pHeaderEmpresa.add(lblContratada);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        p.add(pHeaderEmpresa, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Razão Social do Buffet: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String razao = (contrato.getEmpresaRazaoSocial() != null && !contrato.getEmpresaRazaoSocial().isBlank())
                ? contrato.getEmpresaRazaoSocial() : "Festalia Buffet & Eventos Ltda";
        txtEmpresaRazaoSocial = new JTextField(razao);
        p.add(txtEmpresaRazaoSocial, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("CNPJ: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String cnpj = (contrato.getEmpresaCnpj() != null && !contrato.getEmpresaCnpj().isBlank())
                ? contrato.getEmpresaCnpj() : "12.345.678/0001-90";
        txtEmpresaCnpj = new JTextField(cnpj);
        p.add(txtEmpresaCnpj, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Endereço / Sede:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String endEmp = (contrato.getEmpresaEndereco() != null && !contrato.getEmpresaEndereco().isBlank())
                ? contrato.getEmpresaEndereco() : "Rua das Flores, 500 - Jardins, São Paulo/SP";
        txtEmpresaEndereco = new JTextField(endEmp);
        p.add(txtEmpresaEndereco, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Representante Legal:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String rep = (contrato.getEmpresaRepresentante() != null && !contrato.getEmpresaRepresentante().isBlank())
                ? contrato.getEmpresaRepresentante() : "Diretoria Festalia";
        txtEmpresaRepresentante = new JTextField(rep);
        p.add(txtEmpresaRepresentante, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Foro de Eleição (Comarca):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String foro = (contrato.getEmpresaForoComarca() != null && !contrato.getEmpresaForoComarca().isBlank())
                ? contrato.getEmpresaForoComarca() : "São Paulo/SP";
        txtEmpresaForo = new JTextField(foro);
        p.add(txtEmpresaForo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Telefone / Contato:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        String telEmp = (contrato.getEmpresaTelefone() != null && !contrato.getEmpresaTelefone().isBlank())
                ? contrato.getEmpresaTelefone() : "(11) 99999-8888";
        txtEmpresaTelefone = new JTextField(telEmp);
        p.add(txtEmpresaTelefone, gbc);
        row++;

        // Espaço elástico no fundo
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(51, 65, 85));
        return lbl;
    }

    private JComponent construirAbaEvento() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // --- SEÇÃO DE ALOCAÇÃO DA ORDEM DE SERVIÇO ---
        JPanel pAlocacaoOs = new JPanel(new BorderLayout(8, 6));
        pAlocacaoOs.setBackground(new Color(241, 245, 249));
        pAlocacaoOs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel pLinhaOs = new JPanel(new BorderLayout(6, 0));
        pLinhaOs.setOpaque(false);

        JLabel lblSeletorOs = new JLabel("Alocar Ordem de Serviço (OS): *");
        lblSeletorOs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSeletorOs.setForeground(new Color(30, 41, 59));
        lblSeletorOs.setIcon(IconesFestalia.contrato(16, new Color(41, 128, 185)));
        pLinhaOs.add(lblSeletorOs, BorderLayout.WEST);

        cbOrdemServico = new JComboBox<>();
        cbOrdemServico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        carregarOpcoesOrdemServicoNoCombo();
        cbOrdemServico.addActionListener(e -> {
            ItemOS sel = (ItemOS) cbOrdemServico.getSelectedItem();
            if (sel != null) {
                vincularOrdemServico(sel.os);
            }
        });
        pLinhaOs.add(cbOrdemServico, BorderLayout.CENTER);

        // Busca rápida por número de OS
        JPanel pBuscaRapida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pBuscaRapida.setOpaque(false);
        txtBuscarNumeroOs = new JTextField(6);
        txtBuscarNumeroOs.setToolTipText("Digite o número da OS para localizar");
        JButton btnBuscarOs = new JButton("Buscar OS");
        btnBuscarOs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnBuscarOs.addActionListener(e -> {
            String txt = txtBuscarNumeroOs.getText().trim().replaceAll("[^0-9]", "");
            if (!txt.isEmpty()) {
                int n = Integer.parseInt(txt);
                selecionarOsPorNumeroOuId(n);
            }
        });
        pBuscaRapida.add(new JLabel("Nº:"));
        pBuscaRapida.add(txtBuscarNumeroOs);
        pBuscaRapida.add(btnBuscarOs);
        pLinhaOs.add(pBuscaRapida, BorderLayout.EAST);

        pAlocacaoOs.add(pLinhaOs, BorderLayout.NORTH);

        lblStatusOsVinculada = new JLabel(" ");
        lblStatusOsVinculada.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (contrato.getOrdemServicoId() > 0 || contrato.getNumeroOS() > 0) {
            lblStatusOsVinculada.setText(String.format("  OS #%04d alocada com sucesso — Cliente: %s — Data: %s",
                    contrato.getNumeroOS(), contrato.getClienteNome(), contrato.getDataEventoFormatada()));
            lblStatusOsVinculada.setForeground(new Color(22, 101, 52));
            lblStatusOsVinculada.setIcon(IconesFestalia.confirmar(14, new Color(22, 101, 52)));
        } else {
            lblStatusOsVinculada.setText("  Nenhuma Ordem de Serviço alocada. Selecione uma OS acima para vincular automaticamente.");
            lblStatusOsVinculada.setForeground(new Color(185, 28, 28));
            lblStatusOsVinculada.setIcon(IconesFestalia.alerta(14, new Color(185, 28, 28)));
        }
        pAlocacaoOs.add(lblStatusOsVinculada, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 4, 10, 4);
        p.add(pAlocacaoOs, gbc);
        gbc.insets = new Insets(3, 4, 3, 4);

        // --- CAMPOS DO EVENTO ---
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        p.add(criarLabelCampo("Tipo do Evento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.68;
        txtTipoEvento = new JTextField(contrato.getTipoEvento() != null ? contrato.getTipoEvento() : "");
        p.add(txtTipoEvento, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Data do Evento: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDataEvento = new JTextField(contrato.getDataEventoFormatada().equals("—") ? "" : contrato.getDataEventoFormatada());
        p.add(txtDataEvento, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Horário do Evento:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtHorarioEvento = new JTextField(contrato.getHorarioEvento() != null ? contrato.getHorarioEvento() : "");
        p.add(txtHorarioEvento, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Local do Evento: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtLocalEvento = new JTextField(contrato.getLocalEvento() != null ? contrato.getLocalEvento() : "");
        p.add(txtLocalEvento, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Nº de Convidados:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNumConvidados = new JTextField(String.valueOf(contrato.getNumeroConvidados()));
        p.add(txtNumConvidados, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Valor Total Contratado (R$): *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtValorTotal = new JTextField(String.format(Locale.US, "%.2f", contrato.getValorTotal()));
        p.add(txtValorTotal, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Cardápio e Serviços:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtServicosCardapio = new JTextArea(contrato.getCardapioServicos() != null ? contrato.getCardapioServicos() : "", 4, 20);
        txtServicosCardapio.setLineWrap(true);
        txtServicosCardapio.setWrapStyleWord(true);
        p.add(new JScrollPane(txtServicosCardapio), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel("Observações Especiais:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtObservacoesEspecificas = new JTextArea(contrato.getObservacoesEspecificas() != null ? contrato.getObservacoesEspecificas() : "", 3, 20);
        txtObservacoesEspecificas.setLineWrap(true);
        txtObservacoesEspecificas.setWrapStyleWord(true);
        p.add(new JScrollPane(txtObservacoesEspecificas), gbc);
        row++;

        gbc.gridy = row; gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private void carregarOpcoesOrdemServicoNoCombo() {
        cbOrdemServico.removeAllItems();
        cbOrdemServico.addItem(new ItemOS(null));

        List<OrdemServico> lista = new OrdemServicoDAO().listarTodas();
        ItemOS itemParaSelecionar = null;

        for (OrdemServico o : lista) {
            ItemOS it = new ItemOS(o);
            cbOrdemServico.addItem(it);
            if (contrato.getOrdemServicoId() > 0 && (o.getId() == contrato.getOrdemServicoId() || o.getNumeroOS() == contrato.getOrdemServicoId())) {
                itemParaSelecionar = it;
            } else if (osId > 0 && (o.getId() == osId || o.getNumeroOS() == osId)) {
                itemParaSelecionar = it;
            }
        }

        if (itemParaSelecionar != null) {
            cbOrdemServico.setSelectedItem(itemParaSelecionar);
        }
    }

    private void selecionarOsPorNumeroOuId(int num) {
        for (int i = 0; i < cbOrdemServico.getItemCount(); i++) {
            ItemOS it = cbOrdemServico.getItemAt(i);
            if (it != null && it.os != null && (it.os.getNumeroOS() == num || it.os.getId() == num)) {
                cbOrdemServico.setSelectedIndex(i);
                return;
            }
        }
        OrdemServico os = new OrdemServicoDAO().buscarPorNumeroOS(num);
        if (os == null) os = new OrdemServicoDAO().buscarPorId(num);
        if (os != null) {
            ItemOS novo = new ItemOS(os);
            cbOrdemServico.addItem(novo);
            cbOrdemServico.setSelectedItem(novo);
        } else {
            JOptionPane.showMessageDialog(this, "Nenhuma Ordem de Serviço encontrada com o número/ID: " + num, "OS Não Encontrada", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static class ItemOS {
        public final OrdemServico os;

        public ItemOS(OrdemServico os) {
            this.os = os;
        }

        @Override
        public String toString() {
            if (os == null) return "— Selecione uma Ordem de Serviço cadastrada —";
            return String.format("OS #%04d — %s (%s - R$ %,.2f)",
                    os.getNumeroOS(),
                    os.getCliente() != null ? os.getCliente() : "Sem Cliente",
                    os.getDataEventoFormatada(),
                    os.getValorTotal());
        }
    }

    private JPanel construirAbaClausulas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Topo: Seleção do Modelo de Contrato
        JPanel pModelo = new JPanel(new BorderLayout(6, 6));
        pModelo.setOpaque(false);
        pModelo.setBorder(BorderFactory.createTitledBorder("Modelo de Contrato"));

        cbModelo = new JComboBox<>(ModeloContrato.values());
        cbModelo.setSelectedItem(contrato.getModeloTipo());
        cbModelo.addActionListener(e -> {
            ModeloContrato mod = (ModeloContrato) cbModelo.getSelectedItem();
            if (mod != null && mod != contrato.getModeloTipo()) {
                contrato.setModeloTipo(mod);
                contrato.setClausulas(ContratoTemplateEngine.criarClausulasPadrao(mod));
                atualizarPreviaTexto();
            }
        });
        pModelo.add(cbModelo, BorderLayout.CENTER);

        JLabel lblDicaModelo = new JLabel("<html><small style='color: #64748b;'>Cada modelo adapta os termos de cardápio, louças e estrutura ao formato do evento.</small></html>");
        pModelo.add(lblDicaModelo, BorderLayout.SOUTH);

        p.add(pModelo, BorderLayout.NORTH);

        // Centro: Cláusulas Opcionais / Personalização (Regra #7)
        JPanel pOpcionais = new JPanel();
        pOpcionais.setLayout(new BoxLayout(pOpcionais, BoxLayout.Y_AXIS));
        pOpcionais.setOpaque(false);
        pOpcionais.setBorder(BorderFactory.createTitledBorder("Cláusulas e Itens Configuráveis (Regra #7)"));

        chkCancelamento = criarCheckBoxClausula("Incluir Cláusula 7ª — Cancelamento e Rescisão Proporcional", true, "cancelamento_rescisao");
        chkArrependimentoCdc = criarCheckBoxClausula("Incluir Cláusula 8ª — Direito de Arrependimento (CDC Art. 49 para vendas externas)", true, "direito_arrependimento");
        chkDanosLoucas = criarCheckBoxClausula("Incluir Cláusula 9ª — Danos a Bens, Louças, Utensílios e Equipamentos", true, "danos_bens_loucas");
        chkLgpd = criarCheckBoxClausula("Incluir Cláusula 12ª — Proteção de Dados Pessoais (LGPD)", true, "protecao_dados_lgpd");
        chkCronogramaFinanceiro = criarCheckBoxClausula("Incluir Cláusula 4ª — Cronograma Financeiro Detalhado da OS", true, "cronograma_financeiro");
        chkTestemunhas = new JCheckBox("Incluir Espaço para Assinatura de 2 Testemunhas", true);
        chkTestemunhas.setOpaque(false);
        chkTestemunhas.addItemListener(e -> atualizarPreviaTexto());

        pOpcionais.add(chkCancelamento);
        pOpcionais.add(Box.createVerticalStrut(4));
        pOpcionais.add(chkArrependimentoCdc);
        pOpcionais.add(Box.createVerticalStrut(4));
        pOpcionais.add(chkDanosLoucas);
        pOpcionais.add(Box.createVerticalStrut(4));
        pOpcionais.add(chkLgpd);
        pOpcionais.add(Box.createVerticalStrut(4));
        pOpcionais.add(chkCronogramaFinanceiro);
        pOpcionais.add(Box.createVerticalStrut(4));
        pOpcionais.add(chkTestemunhas);

        p.add(new JScrollPane(pOpcionais), BorderLayout.CENTER);

        return p;
    }

    private JCheckBox criarCheckBoxClausula(String texto, boolean selecionadoPadrao, String chaveClausula) {
        JCheckBox chk = new JCheckBox(texto, selecionadoPadrao);
        chk.setOpaque(false);
        chk.addItemListener(e -> {
            boolean ativo = (e.getStateChange() == ItemEvent.SELECTED);
            for (ClausulaContrato cl : contrato.getClausulas()) {
                if (chaveClausula.equals(cl.getChave())) {
                    cl.setAtiva(ativo);
                    break;
                }
            }
            atualizarPreviaTexto();
        });
        return chk;
    }

    private JPanel construirPainelPrevia() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(241, 245, 249));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), "Prévia da Minuta do Contrato"),
                new EmptyBorder(6, 6, 6, 6)
        ));

        txtPreviaMinuta = new JTextArea();
        txtPreviaMinuta.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtPreviaMinuta.setLineWrap(true);
        txtPreviaMinuta.setWrapStyleWord(true);
        txtPreviaMinuta.setEditable(true); // Permite edição manual direta do texto da minuta!
        txtPreviaMinuta.setBackground(Color.WHITE);
        txtPreviaMinuta.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(txtPreviaMinuta);
        p.add(scroll, BorderLayout.CENTER);

        // Barra inferior da prévia com botão de sincronizar
        JPanel barraPrevia = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        barraPrevia.setOpaque(false);

        btnAtualizarPrevia = new JButton("Sincronizar Prévia com Campos");
        btnAtualizarPrevia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAtualizarPrevia.setIcon(IconesFestalia.versao(14, new Color(71, 85, 105)));
        btnAtualizarPrevia.addActionListener(e -> {
            sincronizarCamposInterfaceNoObjeto();
            atualizarPreviaTexto();
            executarValidacaoVisual();
        });

        barraPrevia.add(btnAtualizarPrevia);
        p.add(barraPrevia, BorderLayout.SOUTH);

        return p;
    }

    private JPanel construirPainelRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setOpaque(false);
        rodape.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        esquerda.setOpaque(false);

        JButton btnHistorico = new JButton("Histórico de Versões");
        btnHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHistorico.setIcon(IconesFestalia.versao(16, new Color(71, 85, 105)));
        btnHistorico.addActionListener(e -> {
            if (contrato.getId() > 0) {
                HistoricoVersoesDialog dlg = new HistoricoVersoesDialog(this, contrato.getId(), contrato.getNumeroContrato());
                dlg.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "O contrato ainda não foi gravado no banco de dados.\nSalve o rascunho ou gere o PDF primeiro para habilitar o versionamento.",
                        "Histórico Indisponível", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        esquerda.add(btnHistorico);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);

        JButton btnCancelar = new JButton("Fechar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.setIcon(IconesFestalia.cancelar(16, new Color(100, 116, 139)));
        btnCancelar.addActionListener(e -> dispose());

        btnSalvarRascunho = new JButton("Salvar Rascunho");
        btnSalvarRascunho.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSalvarRascunho.setIcon(IconesFestalia.confirmar(16, new Color(71, 85, 105)));
        btnSalvarRascunho.addActionListener(e -> acaoSalvarRascunho());

        btnGerarPdf = new JButton("Salvar & Gerar PDF do Contrato");
        btnGerarPdf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGerarPdf.setBackground(new Color(41, 128, 185));
        btnGerarPdf.setForeground(Color.WHITE);
        btnGerarPdf.setIcon(IconesFestalia.contrato(18, Color.WHITE));
        btnGerarPdf.addActionListener(e -> acaoGerarPdfContrato());

        direita.add(btnCancelar);
        direita.add(btnSalvarRascunho);
        direita.add(btnGerarPdf);

        rodape.add(esquerda, BorderLayout.WEST);
        rodape.add(direita, BorderLayout.EAST);

        return rodape;
    }

    private void sincronizarCamposInterfaceNoObjeto() {
        contrato.setClienteNome(txtClienteNome.getText().trim());
        contrato.setClienteCpfCnpj(txtClienteCpfCnpj.getText().trim());
        contrato.setClienteRg(txtClienteRg.getText().trim());
        contrato.setClienteEndereco(txtClienteEndereco.getText().trim());
        contrato.setClienteEmail(txtClienteEmail.getText().trim());
        contrato.setClienteTelefone(txtClienteTelefone.getText().trim());

        if (txtEmpresaRazaoSocial != null) contrato.setEmpresaRazaoSocial(txtEmpresaRazaoSocial.getText().trim());
        if (txtEmpresaCnpj != null) contrato.setEmpresaCnpj(txtEmpresaCnpj.getText().trim());
        if (txtEmpresaEndereco != null) contrato.setEmpresaEndereco(txtEmpresaEndereco.getText().trim());
        if (txtEmpresaRepresentante != null) contrato.setEmpresaRepresentante(txtEmpresaRepresentante.getText().trim());
        if (txtEmpresaForo != null) contrato.setEmpresaForoComarca(txtEmpresaForo.getText().trim());
        if (txtEmpresaTelefone != null) contrato.setEmpresaTelefone(txtEmpresaTelefone.getText().trim());

        contrato.setTipoEvento(txtTipoEvento.getText().trim());
        contrato.setDataEvento(txtDataEvento.getText().trim());
        contrato.setHorarioEvento(txtHorarioEvento.getText().trim());
        contrato.setLocalEvento(txtLocalEvento.getText().trim());
        try {
            contrato.setNumeroConvidados(Integer.parseInt(txtNumConvidados.getText().trim()));
        } catch (Exception ignored) {}
        try {
            contrato.setValorTotal(Double.parseDouble(txtValorTotal.getText().trim().replace(",", ".")));
        } catch (Exception ignored) {}

        contrato.setCardapioServicos(txtServicosCardapio.getText().trim());
        contrato.setObservacoesEspecificas(txtObservacoesEspecificas.getText().trim());
    }

    private void atualizarPreviaTexto() {
        sincronizarCamposInterfaceNoObjeto();
        int targetOsId = contrato.getOrdemServicoId() > 0 ? contrato.getOrdemServicoId() : osId;
        List<ParcelaOS> parcelas = new FinanceiroDAO().listarParcelasPorOS(targetOsId);
        String textoCompilado = ContratoTemplateEngine.compilarTextoCompleto(contrato, parcelas);
        txtPreviaMinuta.setText(textoCompilado);
        txtPreviaMinuta.setCaretPosition(0);
    }

    private void executarValidacaoVisual() {
        sincronizarCamposInterfaceNoObjeto();
        ValidacaoContratoResultado res = ContratoTemplateEngine.validarDadosParaContrato(contrato);

        if (!res.isValido()) {
            painelAlertaValidacao.removeAll();

            JPanel pConteudoAlerta = new JPanel();
            pConteudoAlerta.setLayout(new BoxLayout(pConteudoAlerta, BoxLayout.Y_AXIS));
            pConteudoAlerta.setOpaque(false);

            JLabel lblAviso = new JLabel("Atenção: Existem informações obrigatórias que precisam ser preenchidas antes da emissão definitiva:");
            lblAviso.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblAviso.setForeground(new Color(185, 28, 28));
            lblAviso.setIcon(IconesFestalia.alerta(16, new Color(185, 28, 28)));
            pConteudoAlerta.add(lblAviso);
            pConteudoAlerta.add(Box.createVerticalStrut(4));

            for (String erro : res.getErros()) {
                JLabel lblErro = new JLabel(" • " + erro);
                lblErro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblErro.setForeground(new Color(153, 27, 27));
                pConteudoAlerta.add(lblErro);
            }

            for (String aviso : res.getAvisos()) {
                JLabel lblAvisoItem = new JLabel("  " + aviso);
                lblAvisoItem.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblAvisoItem.setForeground(new Color(161, 98, 7));
                lblAvisoItem.setIcon(IconesFestalia.alerta(14, new Color(217, 119, 6)));
                pConteudoAlerta.add(lblAvisoItem);
            }

            painelAlertaValidacao.add(pConteudoAlerta, BorderLayout.CENTER);
            painelAlertaValidacao.setVisible(true);
        } else {
            painelAlertaValidacao.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void acaoSalvarRascunho() {
        sincronizarCamposInterfaceNoObjeto();
        contrato.setTextoMinuta(txtPreviaMinuta.getText());

        boolean ok = ContratoDAO.salvarOuAtualizarContrato(contrato, "Rascunho de contrato atualizado manualmente", "Administrador");
        if (ok) {
            lblNumeroContrato.setText("Nº " + contrato.getNumeroContrato());
            lblStatusBadge.setText(contrato.getStatus().getDescricao());
            JOptionPane.showMessageDialog(this,
                    "Rascunho do Contrato Nº " + contrato.getNumeroContrato() + " gravado com sucesso!",
                    "Rascunho Salvo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar rascunho do contrato no banco de dados.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoGerarPdfContrato() {
        sincronizarCamposInterfaceNoObjeto();
        executarValidacaoVisual();

        ValidacaoContratoResultado res = ContratoTemplateEngine.validarDadosParaContrato(contrato);
        if (!res.isValido()) {
            StringBuilder sb = new StringBuilder("Existem informações que precisam ser revisadas antes da emissão:\n\n");
            for (String e : res.getErros()) {
                sb.append(" • ").append(e).append("\n");
            }
            sb.append("\nPor favor, preencha os dados nas abas correspondentes antes de gerar o PDF oficial.");

            JOptionPane.showMessageDialog(this, sb.toString(), "Validação Pré-Emissão de Contrato", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Se passar na validação, compila texto final (respeitando qualquer edição direta feita na área de texto)
        String textoFinal = txtPreviaMinuta.getText();
        contrato.setTextoMinuta(textoFinal);
        contrato.setStatus(StatusContrato.GERADO);

        // Salva contrato no banco
        boolean salvo = ContratoDAO.salvarOuAtualizarContrato(contrato, "Emissão de PDF do Contrato", "Administrador");
        if (!salvo) {
            JOptionPane.showMessageDialog(this, "Não foi possível registrar o contrato no banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Gera PDF através do ContratoPDFGenerator
        int targetOsId = contrato.getOrdemServicoId() > 0 ? contrato.getOrdemServicoId() : osId;
        List<ParcelaOS> parcelas = new FinanceiroDAO().listarParcelasPorOS(targetOsId);
        String pdfPath = ContratoPDFGenerator.gerarPdfContrato(contrato, parcelas, true);

        if (pdfPath != null) {
            contrato.setArquivoPdfPath(pdfPath);

            // Registra versão e auditoria
            ContratoDAO.registrarNovaVersao(contrato.getId(), contrato.getVersaoAtual(), textoFinal, pdfPath,
                    "Geração do documento oficial do contrato em PDF", "Administrador");

            // Atualiza status e badges
            lblNumeroContrato.setText("Nº " + contrato.getNumeroContrato());
            lblStatusBadge.setText(contrato.getStatus().getDescricao());
            lblStatusBadge.setBackground(contrato.getStatus().getColorFundo());
            lblStatusBadge.setForeground(contrato.getStatus().getColorTexto());

            contratoGeradoComSucesso = true;

            // Pergunta se deseja abrir o PDF agora
            int opt = JOptionPane.showConfirmDialog(this,
                    "Contrato Nº " + contrato.getNumeroContrato() + " gerado com sucesso em PDF!\n\n"
                            + "Arquivo salvo em:\n" + pdfPath + "\n\nDeseja abrir o PDF agora?",
                    "Contrato Emitido com Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (opt == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new File(pdfPath));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Não foi possível abrir o PDF automaticamente: " + ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }

            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao gerar o documento PDF do contrato.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public boolean isContratoGeradoComSucesso() {
        return contratoGeradoComSucesso;
    }

    public Contrato getContrato() {
        return contrato;
    }
}
