package com.festalia.financeiro;

import com.festalia.OrdemServico;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Modal para registro de pagamentos (totais ou parciais) de uma parcela da Ordem de Serviço.
 */
public class RegistrarPagamentoDialog extends JDialog {

    private final OrdemServico os;
    private final ParcelaOS parcela;
    private final FinanceiroDAO dao;
    private PagamentoParcela pagamentoRealizado = null;

    private JTextField txtValorPago;
    private JTextField txtDataPagamento;
    private JComboBox<String> cbFormaPagamento;
    private JTextField txtIdentificacao;
    private JTextArea txtObservacoes;

    public RegistrarPagamentoDialog(Window owner, OrdemServico os, ParcelaOS parcela, FinanceiroDAO dao) {
        super(owner, "Registrar Pagamento - " + parcela.getDescricao() + " (OS #" + (os != null ? os.getNumeroOS() : "") + ")", ModalityType.APPLICATION_MODAL);
        this.os = os;
        this.parcela = parcela;
        this.dao = dao;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface();
        pack();
        setSize(Math.max(520, getWidth()), Math.min(480, getHeight() + 30));
        setLocationRelativeTo(owner);
    }

    private void construirInterface() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(248, 250, 252));
        principal.setBorder(new EmptyBorder(14, 16, 14, 16));

        // 1. Card Informativo da Parcela
        JPanel cardInfo = new JPanel(new GridLayout(3, 2, 8, 4));
        cardInfo.setBackground(new Color(239, 246, 255));
        cardInfo.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        cardInfo.add(criarItemInfo("Cliente:", os != null ? os.getCliente() : ""));
        cardInfo.add(criarItemInfo("Ordem de Serviço:", "OS #" + (os != null ? os.getNumeroOS() : "")));
        cardInfo.add(criarItemInfo("Parcela:", parcela.getDescricao() + " (Venc: " + parcela.getDataVencimentoFormatada() + ")"));
        cardInfo.add(criarItemInfo("Valor Previsto:", parcela.getValorFormatado()));
        cardInfo.add(criarItemInfo("Total Já Pago:", parcela.getValorPagoFormatado()));
        cardInfo.add(criarItemInfo("Saldo Devedor Restante:", parcela.getSaldoRestanteFormatado(), new Color(185, 28, 28), true));

        principal.add(cardInfo, BorderLayout.NORTH);

        // 2. Formulário do Pagamento
        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBackground(Color.WHITE);
        painelForm.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        // Linha 0: Valor Pago (R$) e Data do Pagamento
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        painelForm.add(new JLabel("Valor Pago (R$): *"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.75;
        double saldoRestante = parcela.getSaldoRestante();
        txtValorPago = new JTextField(String.format(Locale.US, "%.2f", saldoRestante > 0 ? saldoRestante : parcela.getValor()));
        txtValorPago.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtValorPago.setForeground(new Color(21, 128, 61));
        painelForm.add(txtValorPago, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelForm.add(new JLabel("Data do Pagamento: *"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtDataPagamento = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        painelForm.add(txtDataPagamento, gbc);

        // Linha 2: Forma de Pagamento
        gbc.gridx = 0; gbc.gridy = 2;
        painelForm.add(new JLabel("Forma de Pagamento: *"), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        cbFormaPagamento = new JComboBox<>(new String[]{
                "PIX", "Cartão de Crédito", "Cartão de Débito", "Transferência Bancária", "Boleto Bancário", "Dinheiro"
        });
        if (parcela.getFormaPagamentoSugerida() != null) {
            cbFormaPagamento.setSelectedItem(parcela.getFormaPagamentoSugerida());
        }
        painelForm.add(cbFormaPagamento, gbc);

        // Linha 3: Identificação / Comprovante
        gbc.gridx = 0; gbc.gridy = 3;
        painelForm.add(new JLabel("Comprovante / NSU / Ref:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        txtIdentificacao = new JTextField();
        txtIdentificacao.putClientProperty("JTextField.placeholderText", "Ex: Código do PIX, nº da autorização ou recibo...");
        painelForm.add(txtIdentificacao, gbc);

        // Linha 4: Observações
        gbc.gridx = 0; gbc.gridy = 4;
        painelForm.add(new JLabel("Observações:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        txtObservacoes = new JTextArea(2, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        txtObservacoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        painelForm.add(new JScrollPane(txtObservacoes), gbc);

        principal.add(painelForm, BorderLayout.CENTER);

        // 3. Rodapé com Botões de Ação
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rodape.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnConfirmar = new JButton("Confirmar Pagamento");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnConfirmar.setBackground(new Color(22, 101, 52));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.addActionListener(e -> confirmarPagamento());

        rodape.add(btnCancelar);
        rodape.add(btnConfirmar);
        principal.add(rodape, BorderLayout.SOUTH);

        add(principal);
    }

    private JPanel criarItemInfo(String rotulo, String valor) {
        return criarItemInfo(rotulo, valor, Color.DARK_GRAY, false);
    }

    private JPanel criarItemInfo(String rotulo, String valor, Color corValor, boolean destaque) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel lR = new JLabel(rotulo);
        lR.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lR.setForeground(new Color(71, 85, 105));

        JLabel lV = new JLabel(valor);
        lV.setFont(new Font("Segoe UI", destaque ? Font.BOLD : Font.PLAIN, destaque ? 12 : 11));
        lV.setForeground(corValor);

        p.add(lR);
        p.add(lV);
        return p;
    }

    private void confirmarPagamento() {
        double valorPago;
        try {
            valorPago = Double.parseDouble(txtValorPago.getText().trim().replace(",", "."));
            if (valorPago <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Informe um valor de pagamento válido e positivo.", "Valor Inválido", JOptionPane.WARNING_MESSAGE);
            txtValorPago.requestFocus();
            return;
        }

        LocalDate dataPag;
        try {
            dataPag = LocalDate.parse(txtDataPagamento.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Data do pagamento inválida! Use o formato dd/MM/yyyy.", "Data Inválida", JOptionPane.WARNING_MESSAGE);
            txtDataPagamento.requestFocus();
            return;
        }

        double saldoAtual = parcela.getSaldoRestante();
        if (valorPago > (saldoAtual + 0.01)) {
            int conf = JOptionPane.showConfirmDialog(this,
                    String.format("O valor informado (R$ %,.2f) é superior ao saldo devedor restante desta parcela (R$ %,.2f).\n" +
                                    "Deseja registrar o pagamento mesmo assim?",
                            valorPago, saldoAtual),
                    "Confirmação de Pagamento Acima do Saldo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String forma = (String) cbFormaPagamento.getSelectedItem();
        String ident = txtIdentificacao.getText().trim();
        String obs = txtObservacoes.getText().trim();

        PagamentoParcela pag = new PagamentoParcela(
                0, parcela.getId(), os != null ? os.getId() : parcela.getOrdemServicoId(),
                valorPago, dataPag, forma, ident, obs, "Operador", LocalDateTime.now()
        );

        try {
            dao.registrarPagamento(pag);
            this.pagamentoRealizado = pag;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar pagamento no banco: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public PagamentoParcela getPagamentoRealizado() {
        return pagamentoRealizado;
    }
}
