package com.festalia.financeiro;

import com.festalia.OrdemServico;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modal para geração automática de cronograma de parcelamento financeiro de uma OS.
 * Contempla entrada, quantidade de parcelas, intervalos e cálculo de prazo máximo antes da data do evento.
 */
public class GeradorParcelamentoDialog extends JDialog {

    private final OrdemServico os;
    private final double valorTotalOS;
    private List<ParcelaOS> parcelasGeradas = null;

    private JTextField txtEntrada;
    private JSpinner spQtdParcelas;
    private JTextField txtPrimeiroVencimento;
    private JComboBox<String> cbIntervalo;
    private JSpinner spDiasAntesEvento;
    private JComboBox<String> cbFormaPagamentoPadrao;

    private JLabel lblResumoCalculo;
    private JLabel lblAvisoPrazoEvento;
    private JTable tabelaPrevia;
    private DefaultTableModel modelPrevia;

    public GeradorParcelamentoDialog(Window owner, OrdemServico os, double valorTotal) {
        super(owner, "Gerar Parcelamento Automático - OS #" + (os != null ? os.getNumeroOS() : ""), ModalityType.APPLICATION_MODAL);
        this.os = os;
        this.valorTotalOS = valorTotal;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface();
        pack();
        setSize(Math.max(680, getWidth()), Math.min(640, getHeight() + 40));
        setLocationRelativeTo(owner);
    }

    private void construirInterface() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(248, 250, 252));
        principal.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Cabeçalho Informativo
        JPanel topo = new JPanel(new BorderLayout(8, 4));
        topo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Configuração do Cronograma de Pagamento");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel(String.format("Valor Total da OS: R$ %,.2f  •  Data do Evento: %s",
                valorTotalOS, os != null ? os.getDataEventoFormatada() : "Não definida"));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(71, 85, 105));

        topo.add(lblTitulo, BorderLayout.NORTH);
        topo.add(lblSub, BorderLayout.SOUTH);
        principal.add(topo, BorderLayout.NORTH);

        // 2. Formulário de Parâmetros
        JPanel painelParametros = new JPanel(new GridBagLayout());
        painelParametros.setBackground(Color.WHITE);
        painelParametros.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        // Linha 0: Entrada (R$) e Quantidade de Parcelas
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.18;
        painelParametros.add(new JLabel("Valor da Entrada (R$):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.32;
        double sugestaoEntrada = Math.round(valorTotalOS * 0.30); // 30% sugestão inicial
        txtEntrada = new JTextField(String.format(Locale.US, "%.2f", sugestaoEntrada));
        painelParametros.add(txtEntrada, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.20;
        painelParametros.add(new JLabel("Qtd. de Parcelas:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.30;
        spQtdParcelas = new JSpinner(new SpinnerNumberModel(3, 1, 24, 1));
        painelParametros.add(spQtdParcelas, gbc);

        // Linha 1: 1º Vencimento e Intervalo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.18;
        painelParametros.add(new JLabel("1º Vencimento:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.32;
        LocalDate dtSugestao = LocalDate.now().plusMonths(1);
        txtPrimeiroVencimento = new JTextField(dtSugestao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        painelParametros.add(txtPrimeiroVencimento, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.20;
        painelParametros.add(new JLabel("Periodicidade:"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.30;
        cbIntervalo = new JComboBox<>(new String[]{"Mensal (30 dias)", "Quinzenal (15 dias)", "Semanal (7 dias)"});
        painelParametros.add(cbIntervalo, gbc);

        // Linha 2: Quitação antes do evento e Forma de Pagamento
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.18;
        painelParametros.add(new JLabel("Quitação antes do Evento:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.32;
        JPanel painelDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        painelDias.setOpaque(false);
        spDiasAntesEvento = new JSpinner(new SpinnerNumberModel(7, 0, 60, 1));
        spDiasAntesEvento.setPreferredSize(new Dimension(60, 24));
        painelDias.add(spDiasAntesEvento);
        painelDias.add(new JLabel("dias antes"));
        painelParametros.add(painelDias, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.20;
        painelParametros.add(new JLabel("Forma Sugerida:"), gbc);

        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.30;
        cbFormaPagamentoPadrao = new JComboBox<>(new String[]{"PIX", "Boleto Bancário", "Cartão de Crédito", "Transferência", "Dinheiro"});
        painelParametros.add(cbFormaPagamentoPadrao, gbc);

        // Linha 3: Resumo e Alerta
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        JPanel painelFeedback = new JPanel(new GridLayout(2, 1, 2, 2));
        painelFeedback.setOpaque(false);

        lblResumoCalculo = new JLabel(" ");
        lblResumoCalculo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblResumoCalculo.setForeground(new Color(22, 101, 52));

        lblAvisoPrazoEvento = new JLabel(" ");
        lblAvisoPrazoEvento.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAvisoPrazoEvento.setForeground(new Color(185, 28, 28));

        painelFeedback.add(lblResumoCalculo);
        painelFeedback.add(lblAvisoPrazoEvento);
        painelParametros.add(painelFeedback, gbc);

        // 3. Tabela de Pré-visualização das Parcelas
        String[] cols = {"Parcela", "Descrição", "Vencimento", "Valor Previsto", "Forma Pagamento"};
        modelPrevia = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaPrevia = new JTable(modelPrevia);
        tabelaPrevia.setRowHeight(24);
        tabelaPrevia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabelaPrevia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);
        tabelaPrevia.getColumnModel().getColumn(0).setCellRenderer(centro);
        tabelaPrevia.getColumnModel().getColumn(2).setCellRenderer(centro);

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(JLabel.RIGHT);
        tabelaPrevia.getColumnModel().getColumn(3).setCellRenderer(direita);

        tabelaPrevia.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabelaPrevia.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabelaPrevia.getColumnModel().getColumn(2).setPreferredWidth(95);
        tabelaPrevia.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabelaPrevia.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollTabela = new JScrollPane(tabelaPrevia);
        scrollTabela.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240)),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scrollTabela.setPreferredSize(new Dimension(640, 180));

        JPanel centroPainel = new JPanel(new BorderLayout(0, 8));
        centroPainel.setOpaque(false);
        centroPainel.add(painelParametros, BorderLayout.NORTH);
        centroPainel.add(scrollTabela, BorderLayout.CENTER);
        principal.add(centroPainel, BorderLayout.CENTER);

        // 4. Rodapé com Botões de Ação
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnConfirmar = new JButton("Aplicar Cronograma");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmar.setBackground(new Color(22, 101, 52));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.addActionListener(e -> confirmarGeracao());

        rodape.add(btnCancelar);
        rodape.add(btnConfirmar);
        principal.add(rodape, BorderLayout.SOUTH);

        add(principal);

        // Listeners para recálculo dinâmico da prévia
        DocumentListener docList = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcularPrevia(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcularPrevia(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcularPrevia(); }
        };
        txtEntrada.getDocument().addDocumentListener(docList);
        txtPrimeiroVencimento.getDocument().addDocumentListener(docList);
        spQtdParcelas.addChangeListener(e -> recalcularPrevia());
        cbIntervalo.addActionListener(e -> recalcularPrevia());
        spDiasAntesEvento.addChangeListener(e -> recalcularPrevia());
        cbFormaPagamentoPadrao.addActionListener(e -> recalcularPrevia());

        recalcularPrevia();
    }

    private void recalcularPrevia() {
        modelPrevia.setRowCount(0);
        lblAvisoPrazoEvento.setText("");

        double entrada;
        try {
            entrada = Double.parseDouble(txtEntrada.getText().trim().replace(",", "."));
            if (entrada < 0) entrada = 0;
        } catch (Exception ex) {
            entrada = 0;
        }

        int qtdParcelas = (Integer) spQtdParcelas.getValue();
        if (qtdParcelas < 1) qtdParcelas = 1;

        LocalDate dataVenc;
        try {
            dataVenc = LocalDate.parse(txtPrimeiroVencimento.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException ex) {
            dataVenc = LocalDate.now().plusMonths(1);
        }

        int diasIntervalo = 30;
        if (cbIntervalo.getSelectedIndex() == 1) diasIntervalo = 15;
        else if (cbIntervalo.getSelectedIndex() == 2) diasIntervalo = 7;

        double saldoParaParcelar = Math.max(0.0, valorTotalOS - entrada);
        double valorParcelaBase = qtdParcelas > 0 ? (saldoParaParcelar / qtdParcelas) : 0.0;
        String formaPadrao = (String) cbFormaPagamentoPadrao.getSelectedItem();

        List<ParcelaOS> listaTemporaria = new ArrayList<>();
        int numSeq = 1;

        // 1. Entrada (se houver)
        if (entrada > 0) {
            ParcelaOS pEntrada = new ParcelaOS(0, os != null ? os.getId() : 0, numSeq++,
                    "Entrada / Sinal", entrada, LocalDate.now(), formaPadrao, "PENDENTE", "Sinal de confirmação", null);
            listaTemporaria.add(pEntrada);
        }

        // 2. Parcelas
        LocalDate dataAtual = dataVenc;
        double acumuladoParcelas = 0.0;
        for (int i = 1; i <= qtdParcelas; i++) {
            double vlr = Math.round(valorParcelaBase * 100.0) / 100.0;
            // Ajuste de arredondamento de centavos na última parcela
            if (i == qtdParcelas) {
                vlr = Math.max(0.0, saldoParaParcelar - acumuladoParcelas);
            } else {
                acumuladoParcelas += vlr;
            }

            ParcelaOS p = new ParcelaOS(0, os != null ? os.getId() : 0, numSeq++,
                    String.format("Parcela %02d", i), vlr, dataAtual, formaPadrao, "PENDENTE", "", null);
            listaTemporaria.add(p);
            dataAtual = dataAtual.plusDays(diasIntervalo);
        }

        // Popula a tabela de prévia
        for (ParcelaOS p : listaTemporaria) {
            modelPrevia.addRow(new Object[]{
                    "#" + p.getNumeroParcela(),
                    p.getDescricao(),
                    p.getDataVencimentoFormatada(),
                    p.getValorFormatado(),
                    p.getFormaPagamentoSugerida()
            });
        }

        double totalGerado = listaTemporaria.stream().mapToDouble(ParcelaOS::getValor).sum();
        lblResumoCalculo.setText(String.format("Total do Cronograma: R$ %,.2f (%d lançamentos)",
                totalGerado, listaTemporaria.size()));

        // Validação de prazo máximo antes da data do evento
        if (os != null && os.getDataEvento() != null && !listaTemporaria.isEmpty()) {
            int diasLimite = (Integer) spDiasAntesEvento.getValue();
            LocalDate dataLimite = os.getDataEvento().minusDays(diasLimite);
            ParcelaOS ultima = listaTemporaria.get(listaTemporaria.size() - 1);

            if (ultima.getDataVencimento().isAfter(dataLimite)) {
                lblAvisoPrazoEvento.setText(String.format(
                        "⚠️ Atenção: A última parcela vence em %s, posterior ao limite recomendado (%s, %d dias antes do evento)!",
                        ultima.getDataVencimentoFormatada(),
                        dataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        diasLimite
                ));
            }
        }

        this.parcelasGeradas = listaTemporaria;
    }

    private void confirmarGeracao() {
        if (parcelasGeradas == null || parcelasGeradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma parcela gerada.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalGerado = parcelasGeradas.stream().mapToDouble(ParcelaOS::getValor).sum();
        if (Math.abs(totalGerado - valorTotalOS) > 0.05) {
            int conf = JOptionPane.showConfirmDialog(this,
                    String.format("A soma das parcelas (R$ %,.2f) difere do valor total da OS (R$ %,.2f).\n" +
                                    "Deseja manter e aplicar este cronograma mesmo assim?",
                            totalGerado, valorTotalOS),
                    "Confirmação de Cronograma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (!lblAvisoPrazoEvento.getText().isBlank()) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "Existe(m) parcela(s) com vencimento muito próximo ou posterior à data do evento.\n" +
                            "Deseja confirmar a geração com essas datas mesmo assim?",
                    "Aviso de Vencimento", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) {
                return;
            }
        }

        dispose();
    }

    public List<ParcelaOS> getParcelasGeradas() {
        return parcelasGeradas;
    }
}
