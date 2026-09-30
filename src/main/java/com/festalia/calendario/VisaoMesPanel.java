package com.festalia.calendario;

import com.festalia.OrdemServico;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visualização em Grade Mensal (7x6) do Calendário do Festalia ERP.
 * Apresenta eventos por dia, badges de status, identificação visual de conflitos (⚠️)
 * e interação para ver detalhes ou criar nova reserva.
 */
public class VisaoMesPanel extends JPanel {

    public interface VisaoMesListener {
        void onEventoClicado(OrdemServico os);
        void onDiaClicado(LocalDate data);
        void onDiaDuploClique(LocalDate data);
    }

    private YearMonth mesAtual;
    private List<OrdemServico> eventos;
    private final VisaoMesListener listener;
    private final Map<LocalDate, List<OrdemServico>> mapaEventosPorData = new HashMap<>();

    public VisaoMesPanel(YearMonth mesInicial, List<OrdemServico> eventos, VisaoMesListener listener) {
        this.mesAtual = mesInicial != null ? mesInicial : YearMonth.now();
        this.eventos = eventos != null ? eventos : new ArrayList<>();
        this.listener = listener;

        setLayout(new BorderLayout());
        setOpaque(false);
        renderizar();
    }

    public void atualizar(YearMonth mes, List<OrdemServico> novosEventos) {
        this.mesAtual = mes;
        this.eventos = novosEventos != null ? novosEventos : new ArrayList<>();
        renderizar();
    }

    private void renderizar() {
        removeAll();

        // 1. Agrupar eventos por data
        mapaEventosPorData.clear();
        for (OrdemServico os : eventos) {
            if (os != null && os.getDataEvento() != null) {
                mapaEventosPorData.computeIfAbsent(os.getDataEvento(), k -> new ArrayList<>()).add(os);
            }
        }

        // 2. Cabeçalho dos dias da semana (DOMINGO a SÁBADO)
        JPanel painelDiasSemana = new JPanel(new GridLayout(1, 7, 4, 0));
        painelDiasSemana.setOpaque(false);
        painelDiasSemana.setBorder(new EmptyBorder(4, 4, 6, 4));

        String[] diasNomes = {"DOMINGO", "SEGUNDA", "TERÇA", "QUARTA", "QUINTA", "SEXTA", "SÁBADO"};
        for (int i = 0; i < 7; i++) {
            JLabel lblDiaSemana = new JLabel(diasNomes[i], SwingConstants.CENTER);
            lblDiaSemana.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDiaSemana.setForeground(i == 0 || i == 6 ? new Color(185, 28, 28) : new Color(71, 85, 105));
            painelDiasSemana.add(lblDiaSemana);
        }
        add(painelDiasSemana, BorderLayout.NORTH);

        // 3. Grade Mensal 7 colunas x até 6 linhas
        JPanel gridMes = new JPanel(new GridLayout(0, 7, 4, 4));
        gridMes.setOpaque(false);
        gridMes.setBorder(new EmptyBorder(2, 4, 6, 4));

        LocalDate primeiroDiaDoMes = mesAtual.atDay(1);
        int diasNoMes = mesAtual.lengthOfMonth();

        // Dia da semana do 1º dia (1 = Seg, ..., 7 = Dom)
        int diaSemanaPrimeiro = primeiroDiaDoMes.getDayOfWeek().getValue();
        // Ajuste para 0 = Domingo, 1 = Segunda, ..., 6 = Sábado
        int deslocamentoInicio = (diaSemanaPrimeiro == 7) ? 0 : diaSemanaPrimeiro;

        LocalDate hoje = LocalDate.now();

        // Células do mês anterior para preenchimento
        LocalDate mesAnterior = primeiroDiaDoMes.minusMonths(1);
        int diasMesAnterior = mesAnterior.lengthOfMonth();
        for (int i = deslocamentoInicio - 1; i >= 0; i--) {
            LocalDate dataAnt = mesAnterior.withDayOfMonth(diasMesAnterior - i);
            gridMes.add(criarCelulaDia(dataAnt, false, dataAnt.equals(hoje)));
        }

        // Células do mês atual
        for (int d = 1; d <= diasNoMes; d++) {
            LocalDate dataCorrente = mesAtual.atDay(d);
            gridMes.add(criarCelulaDia(dataCorrente, true, dataCorrente.equals(hoje)));
        }

        // Células do próximo mês para completar a última semana
        int celulasRestantes = 7 - (gridMes.getComponentCount() % 7);
        if (celulasRestantes < 7) {
            LocalDate proximoMes = primeiroDiaDoMes.plusMonths(1);
            for (int d = 1; d <= celulasRestantes; d++) {
                LocalDate dataProx = proximoMes.withDayOfMonth(d);
                gridMes.add(criarCelulaDia(dataProx, false, dataProx.equals(hoje)));
            }
        }

        add(new JScrollPane(gridMes), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel criarCelulaDia(LocalDate data, boolean doMesAtual, boolean isHoje) {
        JPanel celula = new JPanel(new BorderLayout(2, 2));
        celula.setBackground(doMesAtual ? (isHoje ? new Color(240, 249, 255) : Color.WHITE) : new Color(248, 250, 252));
        celula.setBorder(new CompoundBorder(
                new LineBorder(isHoje ? new Color(59, 130, 246) : new Color(226, 232, 240), isHoje ? 2 : 1, true),
                new EmptyBorder(3, 4, 3, 4)
        ));

        // Topo da célula: número do dia + badge "Hoje"
        JPanel topoCelula = new JPanel(new BorderLayout());
        topoCelula.setOpaque(false);

        JLabel lblNumeroDia = new JLabel(String.valueOf(data.getDayOfMonth()));
        lblNumeroDia.setFont(new Font("Segoe UI", isHoje ? Font.BOLD : Font.PLAIN, 12));
        lblNumeroDia.setForeground(doMesAtual ? (isHoje ? new Color(30, 58, 138) : new Color(30, 41, 59)) : new Color(148, 163, 184));
        topoCelula.add(lblNumeroDia, BorderLayout.WEST);

        if (isHoje) {
            JLabel lblBadgeHoje = new JLabel(" HOJE ");
            lblBadgeHoje.setFont(new Font("Segoe UI", Font.BOLD, 9));
            lblBadgeHoje.setOpaque(true);
            lblBadgeHoje.setBackground(new Color(59, 130, 246));
            lblBadgeHoje.setForeground(Color.WHITE);
            lblBadgeHoje.setBorder(new EmptyBorder(1, 4, 1, 4));
            topoCelula.add(lblBadgeHoje, BorderLayout.EAST);
        }

        celula.add(topoCelula, BorderLayout.NORTH);

        // Lista de eventos da data
        JPanel painelEventos = new JPanel();
        painelEventos.setLayout(new BoxLayout(painelEventos, BoxLayout.Y_AXIS));
        painelEventos.setOpaque(false);

        List<OrdemServico> listaDia = mapaEventosPorData.getOrDefault(data, new ArrayList<>());
        int maxVisiveis = 3;
        int total = listaDia.size();

        for (int i = 0; i < Math.min(total, maxVisiveis); i++) {
            OrdemServico os = listaDia.get(i);
            boolean temConflito = verificarSeTemConflitoNoDia(os, listaDia);
            painelEventos.add(criarItemEventoMini(os, temConflito));
            painelEventos.add(Box.createVerticalStrut(2));
        }

        if (total > maxVisiveis) {
            JLabel lblMais = new JLabel("+" + (total - maxVisiveis) + " mais...");
            lblMais.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblMais.setForeground(new Color(37, 99, 235));
            lblMais.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblMais.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null) {
                        listener.onDiaClicado(data);
                    }
                }
            });
            painelEventos.add(lblMais);
        }

        celula.add(painelEventos, BorderLayout.CENTER);

        // Interação com o dia
        celula.setCursor(new Cursor(Cursor.HAND_CURSOR));
        celula.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (listener != null) {
                        listener.onDiaDuploClique(data);
                    }
                } else if (e.getClickCount() == 1) {
                    if (listener != null) {
                        listener.onDiaClicado(data);
                    }
                }
            }
        });

        return celula;
    }

    private boolean verificarSeTemConflitoNoDia(OrdemServico os, List<OrdemServico> eventosDoDia) {
        if ("Cancelado".equalsIgnoreCase(os.getStatus())) {
            return false;
        }
        for (OrdemServico outro : eventosDoDia) {
            if (os != outro && !"Cancelado".equalsIgnoreCase(outro.getStatus())) {
                if (os.temSobreposicaoCom(outro)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JPanel criarItemEventoMini(OrdemServico os, boolean temConflito) {
        JPanel card = new JPanel(new BorderLayout(3, 0));
        card.setOpaque(true);

        Color corFundo;
        Color corBorda;
        Color corTexto;

        if (temConflito) {
            // DESTAQUE VISUAL DE CONFLITO OBRIGATÓRIO (Âmbar/Vermelho)
            corFundo = new Color(254, 242, 242);
            corBorda = new Color(239, 68, 68);
            corTexto = new Color(153, 27, 27);
        } else if ("Confirmado".equalsIgnoreCase(os.getStatus())) {
            corFundo = new Color(240, 253, 244);
            corBorda = new Color(74, 222, 128);
            corTexto = new Color(22, 101, 52);
        } else if ("Cancelado".equalsIgnoreCase(os.getStatus())) {
            corFundo = new Color(241, 245, 249);
            corBorda = new Color(203, 213, 225);
            corTexto = new Color(100, 116, 139);
        } else { // Orçamento
            corFundo = new Color(254, 252, 232);
            corBorda = new Color(250, 204, 21);
            corTexto = new Color(133, 77, 14);
        }

        card.setBackground(corFundo);
        card.setBorder(new CompoundBorder(
                new LineBorder(corBorda, temConflito ? 2 : 1, true),
                new EmptyBorder(2, 4, 2, 4)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        // Ícone ou hora à esquerda
        JLabel lblHora = new JLabel(os.getHorarioInicioFormatado());
        if (temConflito) {
            lblHora.setIcon(com.festalia.util.IconesFestalia.alerta(10, new Color(220, 38, 38)));
            lblHora.setIconTextGap(3);
        } else {
            lblHora.setText("• " + os.getHorarioInicioFormatado());
        }
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHora.setForeground(temConflito ? new Color(220, 38, 38) : corTexto);
        card.add(lblHora, BorderLayout.WEST);

        // Nome do cliente
        JLabel lblNome = new JLabel(" " + os.getCliente());
        lblNome.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNome.setForeground(corTexto);
        card.add(lblNome, BorderLayout.CENTER);

        // Tooltip rica com informações do evento e aviso de conflito
        StringBuilder tip = new StringBuilder("<html>");
        if (temConflito) {
            tip.append("<b style='color:#dc2626;'>[ALERTA: CONFLITO DE HORÁRIO DETECTADO]</b><br/>");
        }
        tip.append("<b>OS #").append(os.getNumeroOS()).append(" • ").append(os.getCliente()).append("</b><br/>");
        tip.append("Horário: ").append(os.getHorarioFormatado()).append("<br/>");
        tip.append("Tipo: ").append(os.getTipoEvento()).append("<br/>");
        tip.append("Status: ").append(os.getStatus()).append("<br/>");
        tip.append("Local: ").append(os.getLocal()).append("<br/>");
        tip.append("Convidados: ").append(os.getQuantidadeConvidados()).append(" pessoas<br/>");
        tip.append("Valor: ").append(os.getValorTotalFormatado()).append("<br/>");
        tip.append("<i>Clique para abrir os detalhes da reserva</i></html>");
        card.setToolTipText(tip.toString());

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) {
                    listener.onEventoClicado(os);
                }
            }
        });

        return card;
    }
}
