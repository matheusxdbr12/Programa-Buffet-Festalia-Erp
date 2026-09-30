package com.festalia.contrato;

import com.festalia.financeiro.FinanceiroDAO;
import com.festalia.financeiro.ParcelaOS;
import com.festalia.util.IconesFestalia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Painel da Guia Principal "Contratos" do Festalia ERP.
 * Central de gerenciamento com 5 cartões de indicadores, tabela filtrável,
 * ações operacionais, controle de versões e auditoria.
 */
public class ContratosTabPanel extends JPanel {

    private final Window parentWindow;

    // Componentes de Indicadores (5 Cards)
    private JLabel lblContadorGerados;
    private JLabel lblContadorPendentes;
    private JLabel lblContadorAguardando;
    private JLabel lblContadorAssinados;
    private JLabel lblContadorCancelados;

    // Filtros
    private JTextField txtPesquisa;
    private JComboBox<String> cbFiltroStatus;

    // Tabela
    private JTable tabelaContratos;
    private DefaultTableModel modelContratos;
    private List<Contrato> listaContratos;

    private final NumberFormat moedaFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public ContratosTabPanel(Window parentWindow) {
        this.parentWindow = parentWindow;
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(new Color(248, 250, 252));

        construirInterface();
        recarregarDados();
    }

    private void construirInterface() {
        // 1. TOPO: Título e 5 Cards de Indicadores
        JPanel pNorte = new JPanel(new BorderLayout(0, 12));
        pNorte.setOpaque(false);

        pNorte.add(construirCabecalho(), BorderLayout.NORTH);
        pNorte.add(construirCardsIndicadores(), BorderLayout.CENTER);
        pNorte.add(construirBarraFiltros(), BorderLayout.SOUTH);

        add(pNorte, BorderLayout.NORTH);

        // 2. CENTRO: Tabela de Contratos
        add(construirPainelTabela(), BorderLayout.CENTER);

        // 3. SUL: Barra de Ações Rápidas
        add(construirBarraAcoes(), BorderLayout.SOUTH);
    }

    private JPanel construirCabecalho() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel pEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pEsquerda.setOpaque(false);

        JLabel icone = new JLabel(IconesFestalia.contrato(28, new Color(41, 128, 185)));
        JLabel lblTitulo = new JLabel("Central de Contratos de Prestação de Serviços");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(30, 41, 59));

        pEsquerda.add(icone);
        pEsquerda.add(lblTitulo);

        p.add(pEsquerda, BorderLayout.WEST);

        // Botão Novo Contrato a partir de OS
        JButton btnNovo = new JButton("Gerar Contrato para OS...");
        btnNovo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNovo.setBackground(new Color(41, 128, 185));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setIcon(IconesFestalia.contrato(16, Color.WHITE));
        btnNovo.addActionListener(e -> acaoAbrirNovoContratoParaOs());

        p.add(btnNovo, BorderLayout.EAST);

        return p;
    }

    private JPanel construirCardsIndicadores() {
        JPanel p = new JPanel(new GridLayout(1, 5, 12, 0));
        p.setOpaque(false);

        // 1. Gerados (Azul)
        lblContadorGerados = new JLabel("0");
        p.add(criarCardMetrica("Contratos Gerados", lblContadorGerados, new Color(41, 128, 185), new Color(235, 248, 255), "GERADO"));

        // 2. Pendentes / Rascunhos (Amarelo / Âmbar)
        lblContadorPendentes = new JLabel("0");
        p.add(criarCardMetrica("Contratos Pendentes", lblContadorPendentes, new Color(217, 119, 6), new Color(254, 243, 199), "RASCUNHO"));

        // 3. Aguardando Assinatura (Laranja)
        lblContadorAguardando = new JLabel("0");
        p.add(criarCardMetrica("Aguardando Assinatura", lblContadorAguardando, new Color(234, 88, 12), new Color(255, 237, 213), "AGUARDANDO_ASSINATURA"));

        // 4. Assinados (Verde)
        lblContadorAssinados = new JLabel("0");
        p.add(criarCardMetrica("Contratos Assinados", lblContadorAssinados, new Color(22, 163, 74), new Color(220, 252, 231), "ASSINADO"));

        // 5. Cancelados (Vermelho)
        lblContadorCancelados = new JLabel("0");
        p.add(criarCardMetrica("Contratos Cancelados", lblContadorCancelados, new Color(220, 38, 38), new Color(254, 226, 226), "CANCELADO"));

        return p;
    }

    private JPanel criarCardMetrica(String titulo, JLabel lblValor, Color corBorda, Color corFundo, String statusFiltro) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corBorda, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTit.setForeground(new Color(100, 116, 139));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(corBorda);

        card.add(lblTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        // Ao clicar no card, filtra automaticamente a tabela pelo status!
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cbFiltroStatus.setSelectedItem(statusFiltro != null ? statusFiltro : "TODOS");
                aplicarFiltros();
            }
        });

        return card;
    }

    private JPanel construirBarraFiltros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel lblBuscar = new JLabel("Buscar Contrato / Cliente / OS:");
        lblBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lblBuscar);

        txtPesquisa = new JTextField(18);
        txtPesquisa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPesquisa.putClientProperty("JTextField.placeholderText", "Nº contrato, cliente, CPF...");
        txtPesquisa.addActionListener(e -> aplicarFiltros());
        p.add(txtPesquisa);

        JLabel lblFiltro = new JLabel("Status:");
        lblFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lblFiltro);

        cbFiltroStatus = new JComboBox<>(new String[]{
                "TODOS",
                "GERADO",
                "RASCUNHO",
                "AGUARDANDO_ASSINATURA",
                "ASSINADO",
                "CANCELADO"
        });
        cbFiltroStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFiltroStatus.addActionListener(e -> aplicarFiltros());
        p.add(cbFiltroStatus);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFiltrar.setIcon(IconesFestalia.confirmar(12, new Color(71, 85, 105)));
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        p.add(btnFiltrar);

        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpar.addActionListener(e -> {
            txtPesquisa.setText("");
            cbFiltroStatus.setSelectedIndex(0);
            aplicarFiltros();
        });
        p.add(btnLimpar);

        return p;
    }

    private JComponent construirPainelTabela() {
        String[] colunas = {
                "Nº Contrato", "OS Nº", "Cliente", "CPF / CNPJ",
                "Evento", "Data Evento", "Valor Contratado", "Emissão", "Versão", "Status"
        };

        modelContratos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabelaContratos = new JTable(modelContratos);
        tabelaContratos.setRowHeight(30);
        tabelaContratos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaContratos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaContratos.setShowVerticalLines(false);
        tabelaContratos.setGridColor(new Color(226, 232, 240));

        // Larguras preferenciais
        tabelaContratos.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabelaContratos.getColumnModel().getColumn(1).setPreferredWidth(60);
        tabelaContratos.getColumnModel().getColumn(2).setPreferredWidth(170);
        tabelaContratos.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaContratos.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabelaContratos.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabelaContratos.getColumnModel().getColumn(6).setPreferredWidth(110);
        tabelaContratos.getColumnModel().getColumn(7).setPreferredWidth(85);
        tabelaContratos.getColumnModel().getColumn(8).setPreferredWidth(60);
        tabelaContratos.getColumnModel().getColumn(9).setPreferredWidth(130);

        // Renderizador estilizado para a coluna Status (badges coloridos)
        tabelaContratos.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof StatusContrato) {
                    StatusContrato st = (StatusContrato) value;
                    l.setText(st.getDescricao());
                    l.setBackground(isSelected ? table.getSelectionBackground() : st.getColorFundo());
                    l.setForeground(isSelected ? table.getSelectionForeground() : st.getColorTexto());
                    l.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                return l;
            }
        });

        // Duplo clique na tabela abre a visualização/revisão
        tabelaContratos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    acaoVisualizarEditarMinuta();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabelaContratos);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        return scroll;
    }

    private JPanel construirBarraAcoes() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        p.setOpaque(false);

        JButton btnVisualizar = new JButton("Visualizar / Editar Minuta");
        btnVisualizar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVisualizar.setIcon(IconesFestalia.contrato(14, new Color(41, 128, 185)));
        btnVisualizar.addActionListener(e -> acaoVisualizarEditarMinuta());

        JButton btnAbrirPdf = new JButton("Abrir / Imprimir PDF");
        btnAbrirPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAbrirPdf.setIcon(IconesFestalia.contrato(14, new Color(16, 185, 129)));
        btnAbrirPdf.addActionListener(e -> acaoAbrirPdf());

        JButton btnGerarPdf = new JButton("Gerar / Atualizar PDF");
        btnGerarPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGerarPdf.setIcon(IconesFestalia.confirmar(14, new Color(41, 128, 185)));
        btnGerarPdf.addActionListener(e -> acaoGerarPdfDireto());

        JButton btnStatus = new JButton("Alterar Status");
        btnStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnStatus.setIcon(IconesFestalia.confirmar(14, new Color(217, 119, 6)));
        btnStatus.addActionListener(e -> acaoAlterarStatus());

        JButton btnNovaVersao = new JButton("Nova Versão");
        btnNovaVersao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNovaVersao.setIcon(IconesFestalia.versao(14, new Color(59, 130, 246)));
        btnNovaVersao.addActionListener(e -> acaoGerarNovaVersao());

        JButton btnHistorico = new JButton("Histórico / Auditoria");
        btnHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHistorico.setIcon(IconesFestalia.versao(14, new Color(71, 85, 105)));
        btnHistorico.addActionListener(e -> acaoVerHistoricoAuditoria());

        JButton btnCancelar = new JButton("Cancelar Contrato");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setForeground(new Color(185, 28, 28));
        btnCancelar.setIcon(IconesFestalia.cancelar(14, new Color(185, 28, 28)));
        btnCancelar.addActionListener(e -> acaoCancelarContrato());

        p.add(btnVisualizar);
        p.add(btnAbrirPdf);
        p.add(btnGerarPdf);
        p.add(btnStatus);
        p.add(btnNovaVersao);
        p.add(btnHistorico);
        p.add(btnCancelar);

        return p;
    }

    public void recarregarDados() {
        atualizarCards();
        aplicarFiltros();
    }

    private void atualizarCards() {
        Map<String, Integer> counts = ContratoDAO.obterContadoresCardsEstatico();
        lblContadorGerados.setText(String.valueOf(counts.getOrDefault("GERADO", counts.getOrDefault("gerados", 0))));
        lblContadorPendentes.setText(String.valueOf(counts.getOrDefault("RASCUNHO", counts.getOrDefault("pendentes", 0))));
        lblContadorAguardando.setText(String.valueOf(counts.getOrDefault("AGUARDANDO_ASSINATURA", counts.getOrDefault("aguardando", 0))));
        lblContadorAssinados.setText(String.valueOf(counts.getOrDefault("ASSINADO", counts.getOrDefault("assinados", 0))));
        lblContadorCancelados.setText(String.valueOf(counts.getOrDefault("CANCELADO", counts.getOrDefault("cancelados", 0))));
    }

    private void aplicarFiltros() {
        String texto = txtPesquisa != null ? txtPesquisa.getText().trim() : null;
        String statusSel = (cbFiltroStatus != null && cbFiltroStatus.getSelectedItem() != null)
                ? cbFiltroStatus.getSelectedItem().toString() : "TODOS";

        StatusContrato statusFiltro = null;
        if (!"TODOS".equals(statusSel)) {
            try {
                statusFiltro = StatusContrato.valueOf(statusSel);
            } catch (Exception ignored) {}
        }

        listaContratos = ContratoDAO.listarTodos(texto, statusFiltro);
        modelContratos.setRowCount(0);

        for (Contrato c : listaContratos) {
            modelContratos.addRow(new Object[]{
                    c.getNumeroContrato(),
                    String.format("#%04d", c.getOsId()),
                    c.getClienteNome() != null ? c.getClienteNome() : "-",
                    c.getClienteCpfCnpj() != null ? c.getClienteCpfCnpj() : "-",
                    c.getTipoEvento() != null ? c.getTipoEvento() : "-",
                    c.getDataEventoFormatada(),
                    moedaFormat.format(c.getValorTotal()),
                    c.getDataEmissaoFormatada(),
                    c.getVersaoAtual(),
                    c.getStatus()
            });
        }
    }

    private Contrato obterContratoSelecionado() {
        int sel = tabelaContratos.getSelectedRow();
        if (sel < 0 || listaContratos == null || sel >= listaContratos.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um contrato na tabela para realizar esta ação.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return listaContratos.get(sel);
    }

    private void acaoVisualizarEditarMinuta() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        GerarRevisarContratoDialog dlg = new GerarRevisarContratoDialog(parentWindow, c.getOsId());
        dlg.setVisible(true);
        recarregarDados();
    }

    private void acaoAbrirPdf() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        if (c.getArquivoPdfPath() == null || c.getArquivoPdfPath().trim().isEmpty()) {
            int op = JOptionPane.showConfirmDialog(this,
                    "O PDF deste contrato ainda não foi gerado.\nDeseja gerá-lo agora?",
                    "PDF Não Gerado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                acaoGerarPdfDireto();
            }
            return;
        }

        File f = new File(c.getArquivoPdfPath());
        if (!f.exists()) {
            int op = JOptionPane.showConfirmDialog(this,
                    "O arquivo PDF não foi localizado em disco.\nDeseja gerar novamente?",
                    "Arquivo Ausente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                acaoGerarPdfDireto();
            }
            return;
        }

        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoGerarPdfDireto() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        // Valida dados
        ValidacaoContratoResultado res = ContratoTemplateEngine.validarDadosParaContrato(c);
        if (!res.isValido()) {
            JOptionPane.showMessageDialog(this,
                    "Existem pendências obrigatórias antes da emissão do PDF.\n" +
                            "Por favor, use a opção 'Visualizar / Editar Minuta' para preencher os dados pendentes.",
                    "Validação de Contrato", JOptionPane.WARNING_MESSAGE);
            acaoVisualizarEditarMinuta();
            return;
        }

        List<ParcelaOS> parcelas = new FinanceiroDAO().listarParcelasPorOS(c.getOrdemServicoId());
        String pdf = ContratoPDFGenerator.gerarPdfContrato(c, parcelas, true);
        if (pdf != null) {
            c.setArquivoPdfPath(pdf);
            c.setStatus(StatusContrato.GERADO);
            ContratoDAO.salvarOuAtualizarContrato(c, "PDF gerado/atualizado na central de contratos", "Administrador");
            recarregarDados();

            int opt = JOptionPane.showConfirmDialog(this,
                    "Contrato Nº " + c.getNumeroContrato() + " gerado com sucesso em PDF!\n\nDeseja abrir o arquivo agora?",
                    "Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new File(pdf));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao abrir PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao gerar PDF do contrato.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoAlterarStatus() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        StatusContrato[] opcoes = StatusContrato.values();
        StatusContrato novoStatus = (StatusContrato) JOptionPane.showInputDialog(
                this,
                "Selecione o novo status para o Contrato Nº " + c.getNumeroContrato() + ":",
                "Alterar Status do Contrato",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                c.getStatus()
        );

        if (novoStatus != null && novoStatus != c.getStatus()) {
            String motivo = JOptionPane.showInputDialog(this, "Informe uma observação/motivo para a alteração:", "Motivo da Alteração", JOptionPane.PLAIN_MESSAGE);
            if (motivo == null || motivo.trim().isEmpty()) {
                motivo = "Status alterado de " + c.getStatus().getDescricao() + " para " + novoStatus.getDescricao();
            }

            boolean ok = ContratoDAO.atualizarStatus(c.getId(), novoStatus, motivo, "Administrador");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Status atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                recarregarDados();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar status do contrato.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acaoGerarNovaVersao() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        // Sugere o próximo número de versão (ex: 1.0 -> 1.1)
        String versaoAtual = c.getVersaoAtual();
        String proximaVersao = sugerirProximaVersao(versaoAtual);

        String versaoDigitada = JOptionPane.showInputDialog(this,
                "Informe o identificador da nova versão para o Contrato Nº " + c.getNumeroContrato() + ":",
                proximaVersao);

        if (versaoDigitada == null || versaoDigitada.trim().isEmpty()) return;

        String motivo = JOptionPane.showInputDialog(this,
                "Descreva resumidamente as alterações desta nova versão:\n(Ex: Ajuste no número de convidados e cronograma de pagamento)",
                "Resumo da Nova Versão",
                JOptionPane.QUESTION_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = "Nova versão " + versaoDigitada + " gerada pelo usuário";
        }

        // Abre o diálogo de revisão permitindo ajustar dados antes de registrar a versão
        c.setVersaoAtual(versaoDigitada.trim());
        GerarRevisarContratoDialog dlg = new GerarRevisarContratoDialog(parentWindow, c.getOsId());
        dlg.setVisible(true);
        recarregarDados();
    }

    private String sugerirProximaVersao(String atual) {
        if (atual == null || !atual.contains(".")) return "1.1";
        try {
            String[] parts = atual.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major + "." + (minor + 1);
        } catch (Exception e) {
            return atual + ".1";
        }
    }

    private void acaoVerHistoricoAuditoria() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        HistoricoVersoesDialog dlg = new HistoricoVersoesDialog(parentWindow, c.getId(), c.getNumeroContrato());
        dlg.setVisible(true);
    }

    private void acaoCancelarContrato() {
        Contrato c = obterContratoSelecionado();
        if (c == null) return;

        if (c.getStatus() == StatusContrato.CANCELADO) {
            JOptionPane.showMessageDialog(this, "Este contrato já está cancelado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String motivo = JOptionPane.showInputDialog(this,
                "Tem certeza que deseja cancelar o Contrato Nº " + c.getNumeroContrato() + "?\n" +
                        "Informe obrigatoriamente a justificativa para auditoria:",
                "Confirmar Cancelamento de Contrato",
                JOptionPane.WARNING_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cancelamento não efetuado. É obrigatório informar o motivo para auditoria.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = ContratoDAO.atualizarStatus(c.getId(), StatusContrato.CANCELADO, "Cancelamento do contrato: " + motivo, "Administrador");
        if (ok) {
            JOptionPane.showMessageDialog(this, "Contrato cancelado e registrado na auditoria com sucesso.", "Contrato Cancelado", JOptionPane.INFORMATION_MESSAGE);
            recarregarDados();
        } else {
            JOptionPane.showMessageDialog(this, "Erro ao cancelar o contrato.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void acaoAbrirNovoContratoParaOs() {
        String input = JOptionPane.showInputDialog(this, "Informe o número da Ordem de Serviço (OS):", "Gerar Contrato para OS", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int osNum = Integer.parseInt(input.replaceAll("[^0-9]", ""));
            GerarRevisarContratoDialog dlg = new GerarRevisarContratoDialog(parentWindow, osNum);
            dlg.setVisible(true);
            recarregarDados();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Número de OS inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
