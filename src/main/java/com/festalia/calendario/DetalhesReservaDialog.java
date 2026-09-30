package com.festalia.calendario;

import com.festalia.OrdemServico;
import com.festalia.OrdemServicoDAO;
import com.festalia.PDFGenerator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Modal profissional para visualização de detalhes, alteração rápida de status,
 * reemissão de PDF e edição de reservas a partir do Calendário do Festalia ERP.
 */
public class DetalhesReservaDialog extends JDialog {

    public interface ReservaListener {
        void onReservaAlterada();
        void onSolicitarEdicao(OrdemServico os);
    }

    private final OrdemServico os;
    private final OrdemServicoDAO dao;
    private final List<OrdemServico> todosEventos;
    private final ReservaListener listener;

    public DetalhesReservaDialog(Window owner, OrdemServico os, List<OrdemServico> todosEventos, OrdemServicoDAO dao, ReservaListener listener) {
        super(owner, "Detalhes da Reserva - OS #" + os.getNumeroOS(), ModalityType.APPLICATION_MODAL);
        this.os = os;
        this.dao = dao;
        this.todosEventos = todosEventos;
        this.listener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface();
        pack();
        setSize(Math.max(620, getWidth()), Math.min(640, getHeight() + 30));
        setLocationRelativeTo(owner);
    }

    private void construirInterface() {
        setLayout(new BorderLayout());
        JPanel principal = new JPanel(new BorderLayout(12, 14));
        principal.setBorder(new EmptyBorder(16, 20, 16, 20));
        principal.setBackground(new Color(250, 252, 255));

        // 1. Cabeçalho com Número da OS e Status
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);

        JPanel infoTitulo = new JPanel(new GridLayout(2, 1, 2, 2));
        infoTitulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("OS #" + os.getNumeroOS() + " • " + os.getCliente());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(30, 58, 138));

        JLabel lblSub = new JLabel("Tipo: " + os.getTipoEvento() + " • Realização: " + os.getDataEventoFormatada() + " (" + os.getHorarioFormatado() + ")");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.DARK_GRAY);

        infoTitulo.add(lblTitulo);
        infoTitulo.add(lblSub);
        cabecalho.add(infoTitulo, BorderLayout.CENTER);

        JLabel lblStatus = new JLabel(" " + os.getStatus().toUpperCase() + " ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setOpaque(true);
        lblStatus.setBorder(new EmptyBorder(4, 10, 4, 10));
        if ("Confirmado".equalsIgnoreCase(os.getStatus())) {
            lblStatus.setBackground(new Color(220, 252, 231));
            lblStatus.setForeground(new Color(22, 101, 52));
        } else if ("Cancelado".equalsIgnoreCase(os.getStatus())) {
            lblStatus.setBackground(new Color(254, 226, 226));
            lblStatus.setForeground(new Color(153, 27, 27));
        } else {
            lblStatus.setBackground(new Color(254, 240, 138));
            lblStatus.setForeground(new Color(133, 77, 14));
        }
        cabecalho.add(lblStatus, BorderLayout.EAST);
        principal.add(cabecalho, BorderLayout.NORTH);

        // 2. Painel Central com Informações e Alerta de Conflito se houver
        JPanel painelCentral = new JPanel();
        painelCentral.setLayout(new BoxLayout(painelCentral, BoxLayout.Y_AXIS));
        painelCentral.setOpaque(false);

        // Se houver conflitos com outros eventos, mostra destaque visual
        List<OrdemServico> conflitos = ReservaConflitoValidator.verificarConflitos(os, todosEventos);
        if (!conflitos.isEmpty()) {
            JPanel bannerConflito = new JPanel(new BorderLayout(10, 0));
            bannerConflito.setBackground(new Color(254, 242, 242));
            bannerConflito.setBorder(new CompoundBorder(
                    new LineBorder(new Color(239, 68, 68), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            bannerConflito.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblIcon = new JLabel(com.festalia.util.IconesFestalia.alertaGrande(28));
            bannerConflito.add(lblIcon, BorderLayout.WEST);

            StringBuilder sb = new StringBuilder("<html><b>CONFLITO DE HORÁRIO DETECTADO:</b><br/>Esta reserva sobrepõe horário com: ");
            for (int i = 0; i < conflitos.size(); i++) {
                OrdemServico c = conflitos.get(i);
                if (i > 0) sb.append(", ");
                sb.append("<b>OS #").append(c.getNumeroOS()).append(" (").append(c.getCliente()).append(" - ").append(c.getHorarioFormatado()).append(")</b>");
            }
            sb.append("</html>");

            JLabel lblConfDesc = new JLabel(sb.toString());
            lblConfDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblConfDesc.setForeground(new Color(153, 27, 27));
            bannerConflito.add(lblConfDesc, BorderLayout.CENTER);

            painelCentral.add(bannerConflito);
            painelCentral.add(Box.createVerticalStrut(12));
        }

        // Grade com Detalhes da Reserva
        JPanel gridDetalhes = new JPanel(new GridLayout(6, 2, 12, 8));
        gridDetalhes.setBackground(Color.WHITE);
        gridDetalhes.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        gridDetalhes.setAlignmentX(Component.LEFT_ALIGNMENT);

        gridDetalhes.add(criarItemInfo("Cliente / Contratante:", os.getCliente()));
        gridDetalhes.add(criarItemInfo("Telefone / WhatsApp:", (os.getTelefone() != null && !os.getTelefone().isBlank()) ? os.getTelefone() : "Não informado"));
        gridDetalhes.add(criarItemInfo("Data do Evento:", os.getDataEventoFormatada()));
        gridDetalhes.add(criarItemInfo("Faixa de Horário:", os.getHorarioFormatado()));
        gridDetalhes.add(criarItemInfo("Espaço / Local:", (os.getLocal() != null && !os.getLocal().isBlank()) ? os.getLocal() : "Espaço Festalia"));
        gridDetalhes.add(criarItemInfo("Qtd. de Convidados:", os.getQuantidadeConvidados() + " pessoas"));
        gridDetalhes.add(criarItemInfo("Cardápio Selecionado:", os.getCardapioSelecionado() != null ? os.getCardapioSelecionado().getNome() : "Personalizado"));
        gridDetalhes.add(criarItemInfo("Preço por Pessoa:", String.format("R$ %.2f", os.getPrecoPorPessoa())));
        gridDetalhes.add(criarItemInfo("Taxa de Serviço:", String.format("%.1f%%", os.getTaxaServico())));
        gridDetalhes.add(criarItemInfo("Taxa de Transporte:", String.format("R$ %.2f", os.getTaxaTransporte())));
        gridDetalhes.add(criarItemInfo("VALOR TOTAL:", os.getValorTotalFormatado(), new Color(22, 101, 52), true));
        gridDetalhes.add(criarItemInfo("Data de Cadastro:", os.getDataCriacaoFormatada()));

        painelCentral.add(gridDetalhes);
        painelCentral.add(Box.createVerticalStrut(10));

        // Observações
        if (os.getObservacoes() != null && !os.getObservacoes().isBlank()) {
            JPanel painelObs = new JPanel(new BorderLayout(4, 4));
            painelObs.setBackground(Color.WHITE);
            painelObs.setBorder(new CompoundBorder(
                    new LineBorder(new Color(226, 232, 240), 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            painelObs.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblTitObs = new JLabel("Observações do Evento:");
            lblTitObs.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTitObs.setForeground(Color.DARK_GRAY);
            painelObs.add(lblTitObs, BorderLayout.NORTH);

            JTextArea txtObs = new JTextArea(os.getObservacoes());
            txtObs.setEditable(false);
            txtObs.setLineWrap(true);
            txtObs.setWrapStyleWord(true);
            txtObs.setBackground(Color.WHITE);
            txtObs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txtObs.setRows(3);
            painelObs.add(new JScrollPane(txtObs), BorderLayout.CENTER);

            painelCentral.add(painelObs);
        }

        JScrollPane scroll = new JScrollPane(painelCentral);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        principal.add(scroll, BorderLayout.CENTER);

        // 3. Barra Inferior com Ações
        JPanel rodape = new JPanel(new BorderLayout(8, 0));
        rodape.setOpaque(false);

        // Lado Esquerdo: Ações Rápidas de Status
        JPanel painelStatusRapido = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelStatusRapido.setOpaque(false);

        JLabel lblMudar = new JLabel("Status:");
        lblMudar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        painelStatusRapido.add(lblMudar);

        JButton btnConfirmar = new JButton("Confirmar", com.festalia.util.IconesFestalia.confirmar(12, new Color(22, 101, 52)));
        btnConfirmar.setIconTextGap(5);
        btnConfirmar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnConfirmar.setBackground(new Color(220, 252, 231));
        btnConfirmar.addActionListener(e -> alterarStatus("Confirmado"));

        JButton btnRealizar = new JButton("Realizado", com.festalia.util.IconesFestalia.estrela(12, new Color(180, 83, 9)));
        btnRealizar.setIconTextGap(5);
        btnRealizar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRealizar.setBackground(new Color(241, 245, 249));
        btnRealizar.addActionListener(e -> alterarStatus("Realizado"));

        JButton btnCancelarReserva = new JButton("Cancelar", com.festalia.util.IconesFestalia.cancelar(12, new Color(185, 28, 28)));
        btnCancelarReserva.setIconTextGap(5);
        btnCancelarReserva.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCancelarReserva.setBackground(new Color(254, 226, 226));
        btnCancelarReserva.addActionListener(e -> alterarStatus("Cancelado"));

        painelStatusRapido.add(btnConfirmar);
        painelStatusRapido.add(btnRealizar);
        painelStatusRapido.add(btnCancelarReserva);
        rodape.add(painelStatusRapido, BorderLayout.WEST);

        // Lado Direito: Financeiro, Editar, Gerar PDF e Fechar
        JPanel painelAcoesDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        painelAcoesDireita.setOpaque(false);

        JButton btnFinanceiro = new JButton("Financeiro", com.festalia.util.IconesFestalia.moeda(14, new Color(22, 101, 52)));
        btnFinanceiro.setIconTextGap(6);
        btnFinanceiro.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFinanceiro.setBackground(new Color(220, 252, 231));
        btnFinanceiro.setToolTipText("Abrir Cronograma Financeiro, Parcelas e Recibos");
        btnFinanceiro.addActionListener(e -> {
            new com.festalia.financeiro.GestaoFinanceiraDialog(this, os, new com.festalia.financeiro.FinanceiroDAO()).setVisible(true);
            if (listener != null) {
                listener.onReservaAlterada();
            }
        });

        JButton btnContrato = new JButton("Contrato", com.festalia.util.IconesFestalia.contrato(14, new Color(41, 128, 185)));
        btnContrato.setIconTextGap(6);
        btnContrato.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnContrato.setBackground(new Color(235, 248, 255));
        btnContrato.setToolTipText("Visualizar, Revisar e Emitir Contrato de Prestação de Serviços");
        btnContrato.addActionListener(e -> {
            new com.festalia.contrato.GerarRevisarContratoDialog(this, os.getId()).setVisible(true);
            if (listener != null) {
                listener.onReservaAlterada();
            }
        });

        JButton btnPdf = new JButton("Gerar PDF", com.festalia.util.IconesFestalia.pdf(14, new Color(37, 99, 235)));
        btnPdf.setIconTextGap(6);
        btnPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPdf.setBackground(new Color(239, 246, 255));
        btnPdf.addActionListener(e -> gerarVisualizarPDF());

        JButton btnEditar = new JButton("Editar", com.festalia.util.IconesFestalia.editar(14, Color.WHITE));
        btnEditar.setIconTextGap(6);
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditar.setBackground(new Color(245, 158, 11));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.addActionListener(e -> {
            dispose();
            if (listener != null) {
                listener.onSolicitarEdicao(os);
            }
        });

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFechar.addActionListener(e -> dispose());

        painelAcoesDireita.add(btnFinanceiro);
        painelAcoesDireita.add(btnPdf);
        painelAcoesDireita.add(btnEditar);
        painelAcoesDireita.add(btnFechar);
        rodape.add(painelAcoesDireita, BorderLayout.EAST);

        principal.add(rodape, BorderLayout.SOUTH);
        add(principal);
    }

    private JPanel criarItemInfo(String rotulo, String valor) {
        return criarItemInfo(rotulo, valor, Color.BLACK, false);
    }

    private JPanel criarItemInfo(String rotulo, String valor, Color corValor, boolean destaque) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
        p.setOpaque(false);
        JLabel lRotulo = new JLabel(rotulo);
        lRotulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lRotulo.setForeground(Color.GRAY);

        JLabel lValor = new JLabel(valor);
        lValor.setFont(new Font("Segoe UI", destaque ? Font.BOLD : Font.PLAIN, destaque ? 13 : 12));
        lValor.setForeground(corValor);

        p.add(lRotulo);
        p.add(lValor);
        return p;
    }

    private void alterarStatus(String novoStatus) {
        try {
            dao.atualizarStatus(os.getId(), novoStatus);
            os.setStatus(novoStatus);
            JOptionPane.showMessageDialog(this,
                    "Status da reserva atualizado para: " + novoStatus,
                    "Status Atualizado", JOptionPane.INFORMATION_MESSAGE);
            if (listener != null) {
                listener.onReservaAlterada();
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao atualizar status da reserva: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void gerarVisualizarPDF() {
        try {
            File pastaSaida = new File("saida_pdf_valido");
            if (!pastaSaida.exists()) {
                pastaSaida.mkdirs();
            }
            File arquivoPdf = new File(pastaSaida, "OS_" + os.getNumeroOS() + "_" + os.getCliente().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            PDFGenerator.gerarOrdemServicoPDF(os, arquivoPdf.getAbsolutePath());

            if (Desktop.isDesktopSupported() && arquivoPdf.exists()) {
                Desktop.getDesktop().open(arquivoPdf);
            } else {
                JOptionPane.showMessageDialog(this,
                        "PDF gerado com sucesso em: " + arquivoPdf.getAbsolutePath(),
                        "PDF Gerado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar documento PDF da OS: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
