package com.festalia.financeiro;

import com.festalia.OrdemServico;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal para exibição do histórico de auditoria financeira e rastreabilidade da OS.
 */
public class HistoricoFinanceiroDialog extends JDialog {

    private final OrdemServico os;
    private final FinanceiroDAO dao;

    public HistoricoFinanceiroDialog(Window owner, OrdemServico os, FinanceiroDAO dao) {
        super(owner, "Histórico e Auditoria Financeira - OS #" + (os != null ? os.getNumeroOS() : ""), ModalityType.APPLICATION_MODAL);
        this.os = os;
        this.dao = dao;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface();
        pack();
        setSize(Math.max(680, getWidth()), Math.min(480, getHeight() + 40));
        setLocationRelativeTo(owner);
    }

    private void construirInterface() {
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(new Color(248, 250, 252));
        principal.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Cabeçalho
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JLabel lblTit = new JLabel("Trilha de Auditoria e Movimentações Financeiras");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTit.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel("Histórico cronológico de lançamentos, baixas de pagamentos e emissões vinculadas à OS #" + (os != null ? os.getNumeroOS() : ""));
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));

        topo.add(lblTit, BorderLayout.NORTH);
        topo.add(lblSub, BorderLayout.SOUTH);
        principal.add(topo, BorderLayout.NORTH);

        // Tabela
        String[] cols = {"Data / Hora", "Operação", "Descrição da Ação", "Valor", "Responsável"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabela = new JTable(model);
        tabela.setRowHeight(26);
        tabela.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabela.putClientProperty("JTable.alternateRowColor", true);

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);
        tabela.getColumnModel().getColumn(0).setCellRenderer(centro);
        tabela.getColumnModel().getColumn(1).setCellRenderer(centro);
        tabela.getColumnModel().getColumn(4).setCellRenderer(centro);

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
        direita.setHorizontalAlignment(JLabel.RIGHT);
        tabela.getColumnModel().getColumn(3).setCellRenderer(direita);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(125);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(260);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(95);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);

        List<HistoricoFinanceiro> lista = dao.listarHistoricoPorOS(os != null ? os.getId() : 0);
        for (HistoricoFinanceiro h : lista) {
            model.addRow(new Object[]{
                    h.getDataHoraFormatada(),
                    h.getTipoOperacao(),
                    h.getDescricao(),
                    h.getValor() != 0 ? h.getValorFormatado() : "—",
                    h.getUsuario()
            });
        }

        if (lista.isEmpty()) {
            model.addRow(new Object[]{"—", "INFO", "Nenhum histórico registrado até o momento.", "—", "Sistema"});
        }

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        principal.add(scroll, BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);
        principal.add(rodape, BorderLayout.SOUTH);

        add(principal);
    }
}
