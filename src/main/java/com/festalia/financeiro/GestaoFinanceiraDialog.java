package com.festalia.financeiro;

import com.festalia.OrdemServico;
import com.festalia.OrdemServicoDAO;
import com.festalia.util.IconesFestalia;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel e Janela Principal de Gestão Financeira Avançada da Ordem de Serviço.
 * Unifica Dashboard com métricas em tempo real, cronograma de parcelas, registro de pagamentos,
 * emissão de Recibos em PDF, controle de inadimplência e Termo de Quitação Integral.
 */
public class GestaoFinanceiraDialog extends JDialog {

    private final Window owner;
    private final OrdemServico os;
    private final FinanceiroDAO financeiroDAO;
    private final OrdemServicoDAO osDAO;
    private final Runnable callbackAtualizacao;

    private List<ParcelaOS> parcelas = new ArrayList<>();
    private ResumoFinanceiroOS resumoAtual;

    // Componentes do Dashboard
    private JLabel lblValorTotalOS;
    private JLabel lblTotalPago;
    private JLabel lblSaldoPendente;
    private JLabel lblTotalVencido;
    private JLabel lblPercentualPago;
    private JProgressBar barraProgresso;
    private JPanel bannerInadimplencia;
    private JLabel lblTextoInadimplencia;

    // Tabela e Ações
    private JTable tabelaParcelas;
    private DefaultTableModel modelParcelas;
    private JButton btnRegistrarPagamento;
    private JButton btnGerarRecibo;
    private JButton btnTermoQuitacao;
    private JButton btnExcluirParcela;

    public GestaoFinanceiraDialog(Window owner, OrdemServico os, FinanceiroDAO financeiroDAO) {
        this(owner, os, financeiroDAO, new OrdemServicoDAO(), null);
    }

    public GestaoFinanceiraDialog(Window owner, OrdemServico os, FinanceiroDAO financeiroDAO, Runnable callbackAtualizacao) {
        this(owner, os, financeiroDAO, new OrdemServicoDAO(), callbackAtualizacao);
    }

    public GestaoFinanceiraDialog(Window owner, OrdemServico os, FinanceiroDAO financeiroDAO, OrdemServicoDAO osDAO, Runnable callbackAtualizacao) {
        super(owner, "Gestão Financeira & Cronograma de Pagamentos - OS #" + (os != null ? os.getNumeroOS() : ""), ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        this.os = os;
        this.financeiroDAO = financeiroDAO;
        this.osDAO = osDAO;
        this.callbackAtualizacao = callbackAtualizacao;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface();
        recarregarDadosFinanceiros();
        pack();
        setSize(Math.max(860, getWidth()), Math.min(680, getHeight() + 40));
        setLocationRelativeTo(owner);
    }

    private void construirInterface() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(248, 250, 252));
        principal.setBorder(new EmptyBorder(12, 14, 12, 14));

        // 1. Cabeçalho + Dashboard Financeiro no Topo
        JPanel topoContainer = new JPanel(new BorderLayout(0, 8));
        topoContainer.setOpaque(false);

        topoContainer.add(criarCabecalhoOS(), BorderLayout.NORTH);
        topoContainer.add(criarDashboardCards(), BorderLayout.CENTER);

        // Banner de Alerta de Inadimplência (aparece dinamicamente se houver vencidas)
        bannerInadimplencia = new JPanel(new BorderLayout(8, 0));
        bannerInadimplencia.setBackground(new Color(254, 242, 242));
        bannerInadimplencia.setBorder(new CompoundBorder(
                new LineBorder(new Color(239, 68, 68), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        JLabel lblIconeInad = new JLabel(IconesFestalia.alerta(14, new Color(220, 38, 38)));
        lblTextoInadimplencia = new JLabel(" ");
        lblTextoInadimplencia.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTextoInadimplencia.setForeground(new Color(185, 28, 28));

        bannerInadimplencia.add(lblIconeInad, BorderLayout.WEST);
        bannerInadimplencia.add(lblTextoInadimplencia, BorderLayout.CENTER);
        bannerInadimplencia.setVisible(false);
        topoContainer.add(bannerInadimplencia, BorderLayout.SOUTH);

        principal.add(topoContainer, BorderLayout.NORTH);

        // 2. Centro: Barra de Ferramentas + Tabela do Cronograma
        JPanel centroPainel = new JPanel(new BorderLayout(0, 8));
        centroPainel.setOpaque(false);

        centroPainel.add(criarBarraAcoes(), BorderLayout.NORTH);
        centroPainel.add(criarTabelaCronograma(), BorderLayout.CENTER);

        principal.add(centroPainel, BorderLayout.CENTER);

        // 3. Rodapé com Informações de Pagamento e Fechamento
        JPanel rodape = new JPanel(new BorderLayout(10, 0));
        rodape.setOpaque(false);

        JLabel lblDica = new JLabel("💡 Dica: Dê duplo clique em qualquer parcela para registrar pagamento ou visualizar comprovantes.");
        lblDica.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDica.setForeground(Color.GRAY);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());

        rodape.add(lblDica, BorderLayout.WEST);
        rodape.add(btnFechar, BorderLayout.EAST);
        principal.add(rodape, BorderLayout.SOUTH);

        add(principal);
    }

    private JPanel criarCabecalhoOS() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        JPanel pEsq = new JPanel(new GridLayout(2, 1, 1, 1));
        pEsq.setOpaque(false);

        JLabel lblTit = new JLabel("Controle Financeiro da Ordem de Serviço #" + (os != null ? os.getNumeroOS() : ""));
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(new Color(30, 41, 59));

        String sub = String.format("Cliente: %s  •  Evento: %s em %s  •  Local: %s",
                os != null ? os.getCliente() : "—",
                os != null ? os.getTipoEvento() : "—",
                os != null ? os.getDataEventoFormatada() : "—",
                os != null && os.getLocal() != null && !os.getLocal().isBlank() ? os.getLocal() : "Espaço Festalia");
        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        pEsq.add(lblTit);
        pEsq.add(lblSub);
        p.add(pEsq, BorderLayout.WEST);

        return p;
    }

    private JPanel criarDashboardCards() {
        JPanel painelDash = new JPanel(new BorderLayout(8, 8));
        painelDash.setOpaque(false);

        JPanel gridCards = new JPanel(new GridLayout(1, 5, 8, 0));
        gridCards.setOpaque(false);

        lblValorTotalOS = new JLabel("R$ 0,00");
        lblTotalPago = new JLabel("R$ 0,00");
        lblSaldoPendente = new JLabel("R$ 0,00");
        lblTotalVencido = new JLabel("R$ 0,00");
        lblPercentualPago = new JLabel("0,00%");

        gridCards.add(criarCardMetrica("VALOR DA OS", lblValorTotalOS, new Color(51, 65, 85), new Color(241, 245, 249)));
        gridCards.add(criarCardMetrica("TOTAL PAGO", lblTotalPago, new Color(22, 101, 52), new Color(240, 253, 244)));
        gridCards.add(criarCardMetrica("SALDO PENDENTE", lblSaldoPendente, new Color(180, 83, 9), new Color(254, 243, 199)));
        gridCards.add(criarCardMetrica("TOTAL VENCIDO", lblTotalVencido, new Color(185, 28, 28), new Color(254, 242, 242)));
        gridCards.add(criarCardMetrica("% QUITADO", lblPercentualPago, new Color(29, 78, 216), new Color(239, 246, 255)));

        painelDash.add(gridCards, BorderLayout.CENTER);

        // Barra de Progresso visual
        JPanel painelBarra = new JPanel(new BorderLayout(6, 0));
        painelBarra.setOpaque(false);
        JLabel lblTagProg = new JLabel("Progresso de Quitação:");
        lblTagProg.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTagProg.setForeground(Color.GRAY);

        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setValue(0);
        barraProgresso.setStringPainted(true);
        barraProgresso.setFont(new Font("Segoe UI", Font.BOLD, 11));
        barraProgresso.setPreferredSize(new Dimension(Integer.MAX_VALUE, 16));

        painelBarra.add(lblTagProg, BorderLayout.WEST);
        painelBarra.add(barraProgresso, BorderLayout.CENTER);
        painelDash.add(painelBarra, BorderLayout.SOUTH);

        return painelDash;
    }

    private JPanel criarCardMetrica(String titulo, JLabel lblValor, Color corTexto, Color corFundo) {
        JPanel card = new JPanel(new BorderLayout(4, 2));
        card.setBackground(corFundo);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel lTit = new JLabel(titulo);
        lTit.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lTit.setForeground(new Color(100, 116, 139));

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValor.setForeground(corTexto);

        card.add(lTit, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarBarraAcoes() {
        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);

        // Lado Esquerdo: Geração e Adição de Parcelas
        JPanel pEsq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pEsq.setOpaque(false);

        JButton btnGerarAuto = new JButton("Gerar Parcelamento", IconesFestalia.horario(13, Color.WHITE));
        btnGerarAuto.setIconTextGap(5);
        btnGerarAuto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGerarAuto.setBackground(new Color(37, 99, 235));
        btnGerarAuto.setForeground(Color.WHITE);
        btnGerarAuto.setToolTipText("Calcular e distribuir parcelas automaticamente com base na entrada e intervalo");
        btnGerarAuto.addActionListener(e -> abrirGeradorAutomatico());

        JButton btnAddManual = new JButton("Nova Parcela", IconesFestalia.mais(12, new Color(51, 65, 85)));
        btnAddManual.setIconTextGap(5);
        btnAddManual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAddManual.addActionListener(e -> adicionarParcelaManual());

        btnExcluirParcela = new JButton("Excluir Parcela", IconesFestalia.cancelar(12, new Color(185, 28, 28)));
        btnExcluirParcela.setIconTextGap(5);
        btnExcluirParcela.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExcluirParcela.setEnabled(false);
        btnExcluirParcela.addActionListener(e -> excluirParcelaSelecionada());

        pEsq.add(btnGerarAuto);
        pEsq.add(btnAddManual);
        pEsq.add(btnExcluirParcela);
        barra.add(pEsq, BorderLayout.WEST);

        // Lado Direito: Ações Financeiras, Recibos e Quitação
        JPanel pDir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pDir.setOpaque(false);

        btnRegistrarPagamento = new JButton("Registrar Pagamento", IconesFestalia.confirmar(13, Color.WHITE));
        btnRegistrarPagamento.setIconTextGap(5);
        btnRegistrarPagamento.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegistrarPagamento.setBackground(new Color(22, 101, 52));
        btnRegistrarPagamento.setForeground(Color.WHITE);
        btnRegistrarPagamento.setEnabled(false);
        btnRegistrarPagamento.addActionListener(e -> registrarPagamentoSelecionado());

        btnGerarRecibo = new JButton("Gerar Recibo", IconesFestalia.pdf(13, new Color(37, 99, 235)));
        btnGerarRecibo.setIconTextGap(5);
        btnGerarRecibo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGerarRecibo.setEnabled(false);
        btnGerarRecibo.addActionListener(e -> emitirReciboSelecionado());

        btnTermoQuitacao = new JButton("Termo de Quitação", IconesFestalia.pdf(13, new Color(180, 83, 9)));
        btnTermoQuitacao.setIconTextGap(5);
        btnTermoQuitacao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTermoQuitacao.setEnabled(false);
        btnTermoQuitacao.setToolTipText("Habilitado exclusivamente quando a OS estiver 100% quitada");
        btnTermoQuitacao.addActionListener(e -> emitirTermoQuitacao());

        JButton btnHistorico = new JButton("Auditoria", IconesFestalia.calendario(12, new Color(71, 85, 105)));
        btnHistorico.setIconTextGap(5);
        btnHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHistorico.setToolTipText("Ver histórico de lançamentos e pagamentos desta OS");
        btnHistorico.addActionListener(e -> abrirHistoricoAuditoria());

        pDir.add(btnRegistrarPagamento);
        pDir.add(btnGerarRecibo);
        pDir.add(btnTermoQuitacao);
        pDir.add(btnHistorico);
        barra.add(pDir, BorderLayout.EAST);

        return barra;
    }

    private JScrollPane criarTabelaCronograma() {
        String[] cols = {
                "Nº", "Descrição da Parcela", "Vencimento", "Dias Atraso", "Valor Previsto",
                "Total Pago", "Saldo Restante", "Forma Sugerida", "Status"
        };

        modelParcelas = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabelaParcelas = new JTable(modelParcelas);
        tabelaParcelas.setRowHeight(28);
        tabelaParcelas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaParcelas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabelaParcelas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaParcelas.putClientProperty("JTable.alternateRowColor", true);

        // Alinhamentos
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);
        tabelaParcelas.getColumnModel().getColumn(0).setCellRenderer(centro);
        tabelaParcelas.getColumnModel().getColumn(2).setCellRenderer(centro);
        tabelaParcelas.getColumnModel().getColumn(3).setCellRenderer(centro);
        tabelaParcelas.getColumnModel().getColumn(7).setCellRenderer(centro);

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(JLabel.RIGHT);
        tabelaParcelas.getColumnModel().getColumn(4).setCellRenderer(direita);
        tabelaParcelas.getColumnModel().getColumn(5).setCellRenderer(direita);
        tabelaParcelas.getColumnModel().getColumn(6).setCellRenderer(direita);

        // Renderizador de Status Semafórico com Badges
        tabelaParcelas.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

                if (row >= 0 && row < parcelas.size()) {
                    ParcelaOS p = parcelas.get(row);
                    ParcelaOS.StatusParcela st = p.getStatusDinamico();
                    lbl.setText(" " + st.getDescricao() + " ");
                    if (!isSelected) {
                        lbl.setBackground(Color.decode(st.getCorFundo()));
                        lbl.setForeground(Color.decode(st.getCorTexto()));
                        lbl.setOpaque(true);
                    }
                }
                return lbl;
            }
        });

        // Larguras calibradas
        tabelaParcelas.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabelaParcelas.getColumnModel().getColumn(1).setPreferredWidth(170);
        tabelaParcelas.getColumnModel().getColumn(2).setPreferredWidth(95);
        tabelaParcelas.getColumnModel().getColumn(3).setPreferredWidth(85);
        tabelaParcelas.getColumnModel().getColumn(4).setPreferredWidth(105);
        tabelaParcelas.getColumnModel().getColumn(5).setPreferredWidth(105);
        tabelaParcelas.getColumnModel().getColumn(6).setPreferredWidth(110);
        tabelaParcelas.getColumnModel().getColumn(7).setPreferredWidth(100);
        tabelaParcelas.getColumnModel().getColumn(8).setPreferredWidth(125);

        // Listeners de seleção e duplo clique
        tabelaParcelas.getSelectionModel().addListSelectionListener(e -> atualizarEstadoBotoesAcao());

        tabelaParcelas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabelaParcelas.getSelectedRow() >= 0) {
                    registrarPagamentoSelecionado();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabelaParcelas);
        scroll.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        return scroll;
    }

    public void recarregarDadosFinanceiros() {
        int osId = os != null ? os.getId() : 0;
        double valorTotal = os != null ? os.getValorTotal() : 0.0;

        parcelas = financeiroDAO.listarParcelasPorOS(osId);
        resumoAtual = financeiroDAO.obterResumoFinanceiro(osId, valorTotal);

        // Atualiza Cards do Dashboard
        lblValorTotalOS.setText(resumoAtual.getValorTotalOSFormatado());
        lblTotalPago.setText(resumoAtual.getTotalPagoFormatado());
        lblSaldoPendente.setText(resumoAtual.getTotalPendenteFormatado());
        lblTotalVencido.setText(resumoAtual.getTotalVencidoFormatado());
        lblPercentualPago.setText(resumoAtual.getPercentualPagoFormatado());

        int pctInt = (int) Math.round(resumoAtual.getPercentualPago());
        barraProgresso.setValue(pctInt);
        barraProgresso.setString(String.format("%.1f%% Quitado", resumoAtual.getPercentualPago()));
        if (pctInt >= 100) {
            barraProgresso.setForeground(new Color(22, 101, 52)); // Verde
        } else if (resumoAtual.temParcelasVencidas()) {
            barraProgresso.setForeground(new Color(220, 38, 38)); // Vermelho
        } else {
            barraProgresso.setForeground(new Color(37, 99, 235)); // Azul
        }

        // Banner de Alerta de Inadimplência
        if (resumoAtual.temParcelasVencidas()) {
            lblTextoInadimplencia.setText(String.format("⚠️ Atenção: Esta OS possui %d parcela(s) vencida(s) totalizando %s em atraso!",
                    resumoAtual.getQuantidadeVencidas(), resumoAtual.getTotalVencidoFormatado()));
            bannerInadimplencia.setVisible(true);
        } else {
            bannerInadimplencia.setVisible(false);
        }

        // Atualiza Tabela de Parcelas
        modelParcelas.setRowCount(0);
        for (ParcelaOS p : parcelas) {
            long atraso = p.getDiasAtraso();
            String strAtraso = atraso > 0 ? (atraso + " dia(s)") : "—";

            modelParcelas.addRow(new Object[]{
                    "#" + p.getNumeroParcela(),
                    p.getDescricao(),
                    p.getDataVencimentoFormatada(),
                    strAtraso,
                    p.getValorFormatado(),
                    p.getValorPagoFormatado(),
                    p.getSaldoRestanteFormatado(),
                    p.getFormaPagamentoSugerida(),
                    p.getStatusDinamico().getDescricao()
            });
        }

        // Atualiza Termo de Quitação (disponível se 100% pago)
        btnTermoQuitacao.setEnabled(resumoAtual.isTotalmenteQuitada());

        atualizarEstadoBotoesAcao();

        if (callbackAtualizacao != null) {
            callbackAtualizacao.run();
        }
    }

    private void atualizarEstadoBotoesAcao() {
        int row = tabelaParcelas.getSelectedRow();
        boolean temSelecao = (row >= 0 && row < parcelas.size());

        if (temSelecao) {
            ParcelaOS p = parcelas.get(row);
            btnRegistrarPagamento.setEnabled(!p.isTotalmentePaga() && p.getStatusDinamico() != ParcelaOS.StatusParcela.CANCELADO);
            btnGerarRecibo.setEnabled(!p.getPagamentos().isEmpty());
            btnExcluirParcela.setEnabled(p.getPagamentos().isEmpty());
        } else {
            btnRegistrarPagamento.setEnabled(false);
            btnGerarRecibo.setEnabled(false);
            btnExcluirParcela.setEnabled(false);
        }

        btnTermoQuitacao.setEnabled(resumoAtual != null && resumoAtual.isTotalmenteQuitada());
    }

    private void abrirGeradorAutomatico() {
        GeradorParcelamentoDialog dlg = new GeradorParcelamentoDialog(this, os, os != null ? os.getValorTotal() : 0.0);
        dlg.setVisible(true);

        List<ParcelaOS> geradas = dlg.getParcelasGeradas();
        if (geradas != null && !geradas.isEmpty()) {
            try {
                financeiroDAO.salvarCronogramaCompleto(os.getId(), geradas, "Operador");
                recarregarDadosFinanceiros();
                JOptionPane.showMessageDialog(this,
                        "Cronograma de pagamento com " + geradas.size() + " parcela(s) gerado e salvo com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar cronograma: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adicionarParcelaManual() {
        JTextField txtDesc = new JTextField("Parcela " + (parcelas.size() + 1));
        JTextField txtVlr = new JTextField("0.00");
        JTextField txtDt = new JTextField(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JComboBox<String> cbForma = new JComboBox<>(new String[]{"PIX", "Boleto Bancário", "Cartão de Crédito", "Transferência", "Dinheiro"});

        JPanel painel = new JPanel(new GridLayout(4, 2, 8, 6));
        painel.add(new JLabel("Descrição da Parcela:"));
        painel.add(txtDesc);
        painel.add(new JLabel("Valor Previsto (R$):"));
        painel.add(txtVlr);
        painel.add(new JLabel("Data de Vencimento:"));
        painel.add(txtDt);
        painel.add(new JLabel("Forma Sugerida:"));
        painel.add(cbForma);

        int res = JOptionPane.showConfirmDialog(this, painel, "Adicionar Parcela Manual", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double vlr = Double.parseDouble(txtVlr.getText().trim().replace(",", "."));
                if (vlr <= 0) throw new NumberFormatException();
                LocalDate dt = LocalDate.parse(txtDt.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                ParcelaOS p = new ParcelaOS(
                        0, os.getId(), parcelas.size() + 1, txtDesc.getText().trim(),
                        vlr, dt, (String) cbForma.getSelectedItem(), "PENDENTE", "", LocalDateTime.now()
                );
                financeiroDAO.inserirParcela(p);
                recarregarDadosFinanceiros();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dados inválidos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registrarPagamentoSelecionado() {
        int row = tabelaParcelas.getSelectedRow();
        if (row < 0 || row >= parcelas.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma parcela na tabela para registrar pagamento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ParcelaOS p = parcelas.get(row);
        RegistrarPagamentoDialog dlg = new RegistrarPagamentoDialog(this, os, p, financeiroDAO);
        dlg.setVisible(true);

        PagamentoParcela pagRealizado = dlg.getPagamentoRealizado();
        if (pagRealizado != null) {
            recarregarDadosFinanceiros();

            int resp = JOptionPane.showConfirmDialog(this,
                    String.format("Pagamento de R$ %,.2f registrado com sucesso!\n\nDeseja gerar o Recibo de Pagamento em PDF agora?",
                            pagRealizado.getValorPago()),
                    "Pagamento Registrado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (resp == JOptionPane.YES_OPTION) {
                emitirReciboParaPagamento(p, pagRealizado);
            }
        }
    }

    private void emitirReciboSelecionado() {
        int row = tabelaParcelas.getSelectedRow();
        if (row < 0 || row >= parcelas.size()) return;

        ParcelaOS p = parcelas.get(row);
        List<PagamentoParcela> pags = p.getPagamentos();
        if (pags.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta parcela ainda não possui pagamentos registrados.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Pega o último pagamento registrado para a parcela
        PagamentoParcela ultimo = pags.get(pags.size() - 1);
        emitirReciboParaPagamento(p, ultimo);
    }

    private void emitirReciboParaPagamento(ParcelaOS p, PagamentoParcela pag) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Recibo de Pagamento em PDF");
        fc.setFileFilter(new FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf"));
        String nomeSugerido = String.format("Recibo_OS%d_Parc%d_%s.pdf",
                os.getNumeroOS(), p.getNumeroParcela(), os.getCliente().replaceAll("[^a-zA-Z0-9_-]", "_"));
        fc.setSelectedFile(new File(nomeSugerido));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fc.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
            }

            try {
                double saldoRestanteParc = p.getSaldoRestante();
                double saldoRestanteOS = resumoAtual != null ? resumoAtual.getTotalPendente() : 0.0;

                FinanceiroPDFGenerator.gerarReciboPagamentoPDF(os, p, pag, saldoRestanteParc, saldoRestanteOS, destino.getAbsolutePath());

                // Registra histórico
                financeiroDAO.registrarHistorico(new HistoricoFinanceiro(
                        0, os.getId(), "RECIBO_EMITIDO",
                        String.format("Recibo de pagamento da parcela #%d emitido em PDF.", p.getNumeroParcela()),
                        pag.getValorPago(), "Operador", LocalDateTime.now()
                ));

                int resp = JOptionPane.showConfirmDialog(this,
                        "Recibo em PDF gerado com sucesso!\nArquivo: " + destino.getAbsolutePath() + "\n\nDeseja abrir o recibo agora?",
                        "Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (resp == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(destino);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar Recibo em PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void emitirTermoQuitacao() {
        if (resumoAtual == null || !resumoAtual.isTotalmenteQuitada()) {
            JOptionPane.showMessageDialog(this,
                    "O Termo de Quitação só pode ser emitido quando a OS estiver 100% quitada (Saldo devedor = R$ 0,00).",
                    "Aviso de Quitação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Salvar Termo de Quitação Financeira em PDF");
        fc.setFileFilter(new FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf"));
        String nomeSugerido = String.format("Termo_Quitacao_OS%d_%s.pdf",
                os.getNumeroOS(), os.getCliente().replaceAll("[^a-zA-Z0-9_-]", "_"));
        fc.setSelectedFile(new File(nomeSugerido));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fc.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
            }

            try {
                FinanceiroPDFGenerator.gerarTermoQuitacaoPDF(os, resumoAtual, destino.getAbsolutePath());

                // Registra histórico
                financeiroDAO.registrarHistorico(new HistoricoFinanceiro(
                        0, os.getId(), "QUITACAO_EMITIDA",
                        "Termo de Quitação Financeira Integral emitido em PDF.",
                        resumoAtual.getTotalPago(), "Operador", LocalDateTime.now()
                ));

                int resp = JOptionPane.showConfirmDialog(this,
                        "Termo de Quitação em PDF gerado com sucesso!\nArquivo: " + destino.getAbsolutePath() + "\n\nDeseja abrir o documento agora?",
                        "Sucesso", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (resp == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(destino);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar Termo de Quitação em PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void excluirParcelaSelecionada() {
        int row = tabelaParcelas.getSelectedRow();
        if (row < 0 || row >= parcelas.size()) return;

        ParcelaOS p = parcelas.get(row);
        int conf = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a parcela #" + p.getNumeroParcela() + " (" + p.getDescricao() + ")?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (conf == JOptionPane.YES_OPTION) {
            try {
                financeiroDAO.excluirParcela(p.getId());
                recarregarDadosFinanceiros();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir parcela: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirHistoricoAuditoria() {
        HistoricoFinanceiroDialog dlg = new HistoricoFinanceiroDialog(this, os, financeiroDAO);
        dlg.setVisible(true);
    }
}
