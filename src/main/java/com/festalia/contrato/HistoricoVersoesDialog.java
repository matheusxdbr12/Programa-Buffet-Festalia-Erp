package com.festalia.contrato;

import com.festalia.util.IconesFestalia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Diálogo para visualização do histórico de versões e auditoria de um contrato.
 */
public class HistoricoVersoesDialog extends JDialog {

    private final int contratoId;
    private final String numeroContrato;

    private JTable tabelaVersoes;
    private DefaultTableModel modelVersoes;
    private JTable tabelaAuditoria;
    private DefaultTableModel modelAuditoria;
    private List<VersaoContrato> versoesList;

    public HistoricoVersoesDialog(Window parent, int contratoId, String numeroContrato) {
        super(parent, "Histórico de Versões & Auditoria — Contrato " + numeroContrato, ModalityType.APPLICATION_MODAL);
        this.contratoId = contratoId;
        this.numeroContrato = numeroContrato;

        construirInterface();
        carregarDados();

        setSize(850, 520);
        setLocationRelativeTo(parent);
    }

    private void construirInterface() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(8, 8));
        painelPrincipal.setBorder(new EmptyBorder(12, 16, 12, 16));
        painelPrincipal.setBackground(new Color(248, 249, 250));

        // Cabeçalho
        JPanel pTopo = new JPanel(new BorderLayout());
        pTopo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Histórico e Rastreabilidade do Contrato Nº " + numeroContrato);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(44, 62, 80));
        lblTitulo.setIcon(IconesFestalia.versao(22, new Color(41, 128, 185)));

        pTopo.add(lblTitulo, BorderLayout.WEST);
        painelPrincipal.add(pTopo, BorderLayout.NORTH);

        // Abas
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Aba 1: Versões
        JPanel pVersoes = new JPanel(new BorderLayout(6, 6));
        pVersoes.setBackground(Color.WHITE);
        pVersoes.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] colsVersoes = {"Versão", "Data de Criação", "Responsável", "Resumo das Alterações", "PDF Salvo"};
        modelVersoes = new DefaultTableModel(colsVersoes, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaVersoes = new JTable(modelVersoes);
        tabelaVersoes.setRowHeight(26);
        tabelaVersoes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaVersoes.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelaVersoes.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabelaVersoes.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabelaVersoes.getColumnModel().getColumn(3).setPreferredWidth(320);
        tabelaVersoes.getColumnModel().getColumn(4).setPreferredWidth(160);

        pVersoes.add(new JScrollPane(tabelaVersoes), BorderLayout.CENTER);

        // Botões de ação da versão
        JPanel pAcoesVersao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pAcoesVersao.setOpaque(false);

        JButton btnVerTexto = new JButton("Visualizar Texto Desta Versão");
        btnVerTexto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVerTexto.addActionListener(e -> visualizarTextoVersaoSelecionada());

        JButton btnAbrirPdfVersao = new JButton("Abrir PDF Desta Versão");
        btnAbrirPdfVersao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAbrirPdfVersao.setIcon(IconesFestalia.contrato(14, new Color(41, 128, 185)));
        btnAbrirPdfVersao.addActionListener(e -> abrirPdfVersaoSelecionada());

        pAcoesVersao.add(btnVerTexto);
        pAcoesVersao.add(btnAbrirPdfVersao);
        pVersoes.add(pAcoesVersao, BorderLayout.SOUTH);

        abas.addTab("  Versões Arquivadas", IconesFestalia.pdf(16, new Color(41, 128, 185)), pVersoes);

        // Aba 2: Trilha de Auditoria
        JPanel pAuditoria = new JPanel(new BorderLayout(6, 6));
        pAuditoria.setBackground(Color.WHITE);
        pAuditoria.setBorder(new EmptyBorder(8, 8, 8, 8));

        String[] colsAuditoria = {"Data/Hora", "Ação", "Versão", "Usuário Responsável", "Detalhes / Registro"};
        modelAuditoria = new DefaultTableModel(colsAuditoria, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaAuditoria = new JTable(modelAuditoria);
        tabelaAuditoria.setRowHeight(24);
        tabelaAuditoria.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaAuditoria.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabelaAuditoria.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabelaAuditoria.getColumnModel().getColumn(2).setPreferredWidth(70);
        tabelaAuditoria.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelaAuditoria.getColumnModel().getColumn(4).setPreferredWidth(320);

        pAuditoria.add(new JScrollPane(tabelaAuditoria), BorderLayout.CENTER);
        abas.addTab("  Trilha de Auditoria", IconesFestalia.versao(16, new Color(100, 116, 139)), pAuditoria);

        painelPrincipal.add(abas, BorderLayout.CENTER);

        // Rodapé
        JPanel pRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pRodape.setOpaque(false);
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());
        pRodape.add(btnFechar);
        painelPrincipal.add(pRodape, BorderLayout.SOUTH);

        setContentPane(painelPrincipal);
    }

    private void carregarDados() {
        // Carrega Versões
        modelVersoes.setRowCount(0);
        versoesList = ContratoDAO.listarVersoes(contratoId);
        for (VersaoContrato v : versoesList) {
            String pdfNome = v.getArquivoPdfPath() != null ? new File(v.getArquivoPdfPath()).getName() : "Não gerado";
            modelVersoes.addRow(new Object[]{
                    v.getVersao(),
                    v.getDataCriacao(),
                    v.getCriadoPor(),
                    v.getResumoAlteracoes(),
                    pdfNome
            });
        }

        // Carrega Auditoria
        modelAuditoria.setRowCount(0);
        List<HistoricoContrato> logs = ContratoDAO.listarAuditoria(contratoId);
        for (HistoricoContrato h : logs) {
            modelAuditoria.addRow(new Object[]{
                    h.getDataHora(),
                    h.getAcao(),
                    h.getVersao(),
                    h.getUsuario(),
                    h.getDetalhes()
            });
        }
    }

    private void visualizarTextoVersaoSelecionada() {
        int row = tabelaVersoes.getSelectedRow();
        if (row < 0 || row >= versoesList.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma versão na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VersaoContrato v = versoesList.get(row);
        JDialog dlgTexto = new JDialog(this, "Texto Integral — Versão " + v.getVersao(), ModalityType.APPLICATION_MODAL);
        dlgTexto.setSize(650, 500);
        dlgTexto.setLocationRelativeTo(this);

        JTextArea ta = new JTextArea(v.getTextoMinuta());
        ta.setFont(new Font("Consolas", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));

        dlgTexto.setContentPane(new JScrollPane(ta));
        dlgTexto.setVisible(true);
    }

    private void abrirPdfVersaoSelecionada() {
        int row = tabelaVersoes.getSelectedRow();
        if (row < 0 || row >= versoesList.size()) {
            JOptionPane.showMessageDialog(this, "Selecione uma versão na tabela primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VersaoContrato v = versoesList.get(row);
        if (v.getArquivoPdfPath() == null || v.getArquivoPdfPath().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há documento PDF registrado para esta versão.", "PDF Indisponível", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        File f = new File(v.getArquivoPdfPath());
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "O arquivo PDF não foi localizado no caminho:\n" + v.getArquivoPdfPath(), "Arquivo não encontrado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao abrir arquivo PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
