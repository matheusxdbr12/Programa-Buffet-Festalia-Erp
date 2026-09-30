package com.festalia.calendario;

import com.festalia.OrdemServico;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Validador e gerenciador de conflitos de reservas no Festalia ERP.
 * Responsável por detectar sobreposições de horários no mesmo dia e
 * exibir a interface de confirmação interativa com as opções:
 * - Cancelar
 * - Voltar e alterar horário
 * - Cadastrar mesmo assim (Confirmar Exceção)
 */
public class ReservaConflitoValidator {

    public enum RespostaConflito {
        CANCELAR,
        VOLTAR_ALTERAR_HORARIO,
        CADASTRAR_MESMO_ASSIM
    }

    public enum AcaoConflito {
        CANCELAR,
        VOLTAR_ALTERAR_HORARIO,
        CADASTRAR_MESMO_ASSIM
    }

    /**
     * Identifica todas as reservas existentes que entram em conflito com a nova/editada.
     */
    public static List<OrdemServico> verificarConflitos(OrdemServico nova, List<OrdemServico> listaExistente) {
        List<OrdemServico> conflitos = new ArrayList<>();
        if (nova == null || listaExistente == null || "Cancelado".equalsIgnoreCase(nova.getStatus())) {
            return conflitos;
        }

        for (OrdemServico existente : listaExistente) {
            if (existente == null || existente.getId() == nova.getId()) {
                continue;
            }
            if ("Cancelado".equalsIgnoreCase(existente.getStatus())) {
                continue;
            }
            if (nova.temSobreposicaoCom(existente)) {
                conflitos.add(existente);
            }
        }
        return conflitos;
    }

    /**
     * Exibe o modal moderno de alerta de conflito de reservas conforme os requisitos do sistema.
     */
    public static RespostaConflito exibirModalConflito(Component parent, OrdemServico nova, List<OrdemServico> conflitos) {
        if (conflitos == null || conflitos.isEmpty()) {
            return RespostaConflito.CADASTRAR_MESMO_ASSIM;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Atenção: Conflito de Reservas Detectado", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JPanel painelConteudo = new JPanel(new BorderLayout(14, 14));
        painelConteudo.setBorder(new EmptyBorder(18, 20, 18, 20));
        painelConteudo.setBackground(new Color(253, 253, 254));

        // Topo com banner de aviso
        JPanel bannerAviso = new JPanel(new BorderLayout(12, 0));
        bannerAviso.setBackground(new Color(254, 243, 235));
        bannerAviso.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 158, 66), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblIconeAlerta = new JLabel(com.festalia.util.IconesFestalia.alertaGrande(36));
        bannerAviso.add(lblIconeAlerta, BorderLayout.WEST);

        JPanel painelTextosAlerta = new JPanel(new GridLayout(2, 1, 2, 2));
        painelTextosAlerta.setOpaque(false);
        JLabel lblTituloAlerta = new JLabel("Atenção: Já existe uma reserva para este período!");
        lblTituloAlerta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloAlerta.setForeground(new Color(180, 83, 9));

        JLabel lblSubAlerta = new JLabel("Foi detectada uma sobreposição de horários na data " + nova.getDataEventoFormatada() + ".");
        lblSubAlerta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubAlerta.setForeground(new Color(120, 53, 15));

        painelTextosAlerta.add(lblTituloAlerta);
        painelTextosAlerta.add(lblSubAlerta);
        bannerAviso.add(painelTextosAlerta, BorderLayout.CENTER);
        painelConteudo.add(bannerAviso, BorderLayout.NORTH);

        // Corpo com os cards comparativos: Novo Evento vs Evento(s) Existente(s)
        JPanel painelComparativo = new JPanel();
        painelComparativo.setLayout(new BoxLayout(painelComparativo, BoxLayout.Y_AXIS));
        painelComparativo.setOpaque(false);

        // Card do Novo Evento
        painelComparativo.add(criarCardEvento("Novo Evento a Cadastrar", nova, new Color(59, 130, 246), new Color(239, 246, 255)));
        painelComparativo.add(Box.createVerticalStrut(10));

        // Cards dos Eventos Existentes em Conflito
        JLabel lblExistentes = new JLabel("Reserva(s) já confirmada(s) no mesmo período:");
        lblExistentes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExistentes.setForeground(Color.DARK_GRAY);
        lblExistentes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelComparativo.add(lblExistentes);
        painelComparativo.add(Box.createVerticalStrut(6));

        for (OrdemServico conf : conflitos) {
            painelComparativo.add(criarCardEvento("Reserva Conflitante (OS #" + conf.getNumeroOS() + ")", conf, new Color(239, 68, 68), new Color(254, 242, 242)));
            painelComparativo.add(Box.createVerticalStrut(6));
        }

        JLabel lblPergunta = new JLabel("Deseja cadastrar os dois eventos mesmo assim?");
        lblPergunta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPergunta.setForeground(new Color(30, 41, 59));
        lblPergunta.setBorder(new EmptyBorder(8, 2, 4, 2));
        lblPergunta.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelComparativo.add(lblPergunta);

        JScrollPane scroll = new JScrollPane(painelComparativo);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        painelConteudo.add(scroll, BorderLayout.CENTER);

        // Barra inferior de botões com as 3 opções
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        painelBotoes.setBackground(new Color(248, 250, 252));
        painelBotoes.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JButton btnCancelar = new JButton("Cancelar", com.festalia.util.IconesFestalia.cancelar(14, new Color(185, 28, 28)));
        btnCancelar.setIconTextGap(6);
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.setToolTipText("Cancela o cadastro sem salvar alterações");

        JButton btnAlterarHorario = new JButton("Voltar e Alterar Horário", com.festalia.util.IconesFestalia.voltar(14, new Color(51, 65, 85)));
        btnAlterarHorario.setIconTextGap(6);
        btnAlterarHorario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAlterarHorario.setBackground(new Color(241, 245, 249));
        btnAlterarHorario.setToolTipText("Volta ao formulário para você ajustar a faixa de horário do evento");

        JButton btnCadastrarMesmoAssim = new JButton("Cadastrar Mesmo Assim", com.festalia.util.IconesFestalia.alerta(14, Color.WHITE));
        btnCadastrarMesmoAssim.setIconTextGap(6);
        btnCadastrarMesmoAssim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCadastrarMesmoAssim.setBackground(new Color(234, 88, 12));
        btnCadastrarMesmoAssim.setForeground(Color.WHITE);
        btnCadastrarMesmoAssim.setToolTipText("Permite a sobreposição de horário mantendo ambos os eventos cadastrados");

        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnAlterarHorario);
        painelBotoes.add(btnCadastrarMesmoAssim);

        dialog.add(painelConteudo, BorderLayout.CENTER);
        dialog.add(painelBotoes, BorderLayout.SOUTH);

        final RespostaConflito[] resultado = {RespostaConflito.CANCELAR};

        btnCancelar.addActionListener(e -> {
            resultado[0] = RespostaConflito.CANCELAR;
            dialog.dispose();
        });

        btnAlterarHorario.addActionListener(e -> {
            resultado[0] = RespostaConflito.VOLTAR_ALTERAR_HORARIO;
            dialog.dispose();
        });

        btnCadastrarMesmoAssim.addActionListener(e -> {
            resultado[0] = RespostaConflito.CADASTRAR_MESMO_ASSIM;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(540, dialog.getWidth()), Math.min(520, dialog.getHeight() + 40));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return resultado[0];
    }

    private static JPanel criarCardEvento(String subtitulo, OrdemServico os, Color corBorda, Color corFundo) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(corFundo);
        card.setBorder(new CompoundBorder(
                new LineBorder(corBorda, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        JLabel lblTag = new JLabel(subtitulo);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTag.setForeground(corBorda.darker());
        topo.add(lblTag, BorderLayout.WEST);

        JLabel lblStatus = new JLabel(" " + os.getStatus() + " ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatus.setOpaque(true);
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
        topo.add(lblStatus, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        JPanel dados = new JPanel(new GridLayout(2, 2, 8, 2));
        dados.setOpaque(false);

        JLabel lblCliente = new JLabel("Cliente: " + os.getCliente() + (os.getTipoEvento() != null ? " (" + os.getTipoEvento() + ")" : ""));
        lblCliente.setIcon(com.festalia.util.IconesFestalia.cliente(14, new Color(37, 99, 235)));
        lblCliente.setIconTextGap(6);
        lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblHorario = new JLabel("Horário: " + os.getHorarioFormatado());
        lblHorario.setIcon(com.festalia.util.IconesFestalia.horario(14, new Color(194, 65, 12)));
        lblHorario.setIconTextGap(6);
        lblHorario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHorario.setForeground(new Color(194, 65, 12));

        String localStr = (os.getLocal() != null && !os.getLocal().isBlank()) ? os.getLocal() : "Espaço Festalia Padrão";
        JLabel lblLocal = new JLabel("Local: " + localStr);
        lblLocal.setIcon(com.festalia.util.IconesFestalia.local(14, new Color(100, 116, 139)));
        lblLocal.setIconTextGap(6);
        lblLocal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLocal.setForeground(Color.DARK_GRAY);

        JLabel lblConv = new JLabel("Convidados: " + os.getQuantidadeConvidados() + " pessoas");
        lblConv.setIcon(com.festalia.util.IconesFestalia.convidados(14, new Color(100, 116, 139)));
        lblConv.setIconTextGap(6);
        lblConv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblConv.setForeground(Color.DARK_GRAY);

        dados.add(lblCliente);
        dados.add(lblHorario);
        dados.add(lblLocal);
        dados.add(lblConv);

        card.add(dados, BorderLayout.CENTER);
        return card;
    }
}
