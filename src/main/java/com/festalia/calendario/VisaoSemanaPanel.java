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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visualização Semanal do Calendário do Festalia ERP.
 * Apresenta 7 colunas (dias da semana) e linhas de horário (das 08:00 às 23:00).
 * IMPLEMENTA DISPOSIÇÃO LADO A LADO PARA EVENTOS CONFLITANTES / SIMULTÂNEOS:
 * Eventos no mesmo horário são divididos em colunas paralelas para que nenhum esconda o outro.
 */
public class VisaoSemanaPanel extends JPanel {

    public interface VisaoSemanaListener {
        void onEventoClicado(OrdemServico os);
        void onSlotClicado(LocalDate data, LocalTime horario);
    }

    private LocalDate inicioSemana;
    private List<OrdemServico> eventos;
    private final VisaoSemanaListener listener;

    private static final int HORA_INICIAL = 8;
    private static final int HORA_FINAL = 23;
    private static final int ALTURA_HORA = 48; // pixels por hora

    public VisaoSemanaPanel(LocalDate dataReferencia, List<OrdemServico> eventos, VisaoSemanaListener listener) {
        this.eventos = eventos != null ? eventos : new ArrayList<>();
        this.listener = listener;
        setInicioSemana(dataReferencia);

        setLayout(new BorderLayout());
        setOpaque(false);
        renderizar();
    }

    public void setInicioSemana(LocalDate dataReferencia) {
        LocalDate d = dataReferencia != null ? dataReferencia : LocalDate.now();
        // Alinha para o domingo anterior ou mesmo dia se for domingo
        int diaSemana = d.getDayOfWeek().getValue(); // 1 = Seg ... 7 = Dom
        int recuo = (diaSemana == 7) ? 0 : diaSemana;
        this.inicioSemana = d.minusDays(recuo);
    }

    public void atualizar(LocalDate dataReferencia, List<OrdemServico> novosEventos) {
        setInicioSemana(dataReferencia);
        this.eventos = novosEventos != null ? novosEventos : new ArrayList<>();
        renderizar();
    }

    private void renderizar() {
        removeAll();

        // 1. Agrupar eventos por data da semana
        Map<LocalDate, List<OrdemServico>> eventosPorDia = new HashMap<>();
        for (OrdemServico os : eventos) {
            if (os != null && os.getDataEvento() != null) {
                eventosPorDia.computeIfAbsent(os.getDataEvento(), k -> new ArrayList<>()).add(os);
            }
        }

        // 2. Painel Superior com Cabeçalho dos 7 Dias
        JPanel painelTopo = new JPanel(new GridLayout(1, 8, 2, 0));
        painelTopo.setOpaque(false);
        painelTopo.setBorder(new EmptyBorder(6, 6, 8, 6));

        // Coluna vazia para o eixo de horas
        JLabel lblEixoVazio = new JLabel("HORA", SwingConstants.CENTER);
        lblEixoVazio.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblEixoVazio.setForeground(new Color(148, 163, 184));
        painelTopo.add(lblEixoVazio);

        String[] diasAbrev = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"};
        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            boolean isHoje = dia.equals(hoje);

            JPanel cabecalhoDia = new JPanel(new GridLayout(2, 1, 0, 1));
            cabecalhoDia.setBackground(isHoje ? new Color(239, 246, 255) : Color.WHITE);
            cabecalhoDia.setBorder(new CompoundBorder(
                    new LineBorder(isHoje ? new Color(59, 130, 246) : new Color(226, 232, 240), isHoje ? 2 : 1, true),
                    new EmptyBorder(3, 4, 3, 4)
            ));

            JLabel lblNome = new JLabel(diasAbrev[i], SwingConstants.CENTER);
            lblNome.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblNome.setForeground(isHoje ? new Color(37, 99, 235) : new Color(71, 85, 105));

            JLabel lblData = new JLabel(String.format("%02d/%02d", dia.getDayOfMonth(), dia.getMonthValue()), SwingConstants.CENTER);
            lblData.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblData.setForeground(isHoje ? new Color(30, 58, 138) : Color.DARK_GRAY);

            cabecalhoDia.add(lblNome);
            cabecalhoDia.add(lblData);
            painelTopo.add(cabecalhoDia);
        }
        add(painelTopo, BorderLayout.NORTH);

        // 3. Área Rolável com Grade de Horários e Colunas de Dias
        int totalHoras = HORA_FINAL - HORA_INICIAL + 1;
        int alturaTotal = totalHoras * ALTURA_HORA;

        JPanel painelCorpo = new JPanel(new GridLayout(1, 8, 2, 0));
        painelCorpo.setOpaque(false);
        painelCorpo.setPreferredSize(new Dimension(800, alturaTotal));

        // Coluna 0: Horas (08:00 às 23:00)
        JPanel colunaHoras = new JPanel(null);
        colunaHoras.setOpaque(false);
        colunaHoras.setPreferredSize(new Dimension(50, alturaTotal));
        for (int h = HORA_INICIAL; h <= HORA_FINAL; h++) {
            int y = (h - HORA_INICIAL) * ALTURA_HORA;
            JLabel lblH = new JLabel(String.format("%02d:00", h));
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblH.setForeground(new Color(100, 116, 139));
            lblH.setBounds(8, y + 2, 45, 16);
            colunaHoras.add(lblH);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(226, 232, 240));
            sep.setBounds(0, y, 60, 1);
            colunaHoras.add(sep);
        }
        painelCorpo.add(colunaHoras);

        // Colunas 1 a 7: Os 7 dias da semana
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            List<OrdemServico> eventosDoDia = eventosPorDia.getOrDefault(dia, new ArrayList<>());
            painelCorpo.add(criarColunaDiaSemana(dia, eventosDoDia, alturaTotal));
        }

        JScrollPane scroll = new JScrollPane(painelCorpo);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel criarColunaDiaSemana(LocalDate dia, List<OrdemServico> eventosDoDia, int alturaTotal) {
        ColunaDiaLayout coluna = new ColunaDiaLayout(dia, eventosDoDia, alturaTotal);
        return coluna;
    }

    /**
     * Painel personalizado de cada dia na visualização semanal.
     * Desenha as linhas horizontais de horário e posiciona eventos com cálculo
     * exato de sobreposição lado a lado.
     */
    private class ColunaDiaLayout extends JPanel {
        private final LocalDate dia;
        private final List<OrdemServico> eventosDoDia;

        public ColunaDiaLayout(LocalDate dia, List<OrdemServico> eventosDoDia, int alturaTotal) {
            this.dia = dia;
            this.eventosDoDia = eventosDoDia;
            setLayout(null);
            setBackground(dia.equals(LocalDate.now()) ? new Color(250, 252, 255) : Color.WHITE);
            setBorder(new LineBorder(new Color(241, 245, 249), 1));
            setPreferredSize(new Dimension(100, alturaTotal));

            // Interação: duplo clique em um slot de horário cria nova reserva naquele horário!
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int hora = HORA_INICIAL + (e.getY() / ALTURA_HORA);
                        hora = Math.max(0, Math.min(23, hora));
                        if (listener != null) {
                            listener.onSlotClicado(dia, LocalTime.of(hora, 0));
                        }
                    }
                }
            });

            posicionarCardsEventos();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(241, 245, 249));

            // Linhas horizontais de cada hora
            for (int h = HORA_INICIAL; h <= HORA_FINAL; h++) {
                int y = (h - HORA_INICIAL) * ALTURA_HORA;
                g2.drawLine(0, y, getWidth(), y);
            }
            g2.dispose();
        }

        private void posicionarCardsEventos() {
            if (eventosDoDia == null || eventosDoDia.isEmpty()) {
                return;
            }

            // Identificar grupos de conflitos simultâneos para calcular lado a lado
            List<List<OrdemServico>> gruposConflito = agruparSobreposicoes(eventosDoDia);

            for (List<OrdemServico> grupo : gruposConflito) {
                int totalColunas = grupo.size();
                for (int colIndex = 0; colIndex < totalColunas; colIndex++) {
                    OrdemServico os = grupo.get(colIndex);
                    boolean temConflito = (totalColunas > 1) && !"Cancelado".equalsIgnoreCase(os.getStatus());
                    JPanel card = criarCardSemana(os, temConflito);

                    // Posicionamento Y baseado na hora de início e duração
                    LocalTime inicio = os.getHorarioInicio() != null ? os.getHorarioInicio() : LocalTime.of(18, 0);
                    LocalTime fim = os.getHorarioFim() != null ? os.getHorarioFim() : LocalTime.of(22, 0);

                    double horaIniDec = inicio.getHour() + (inicio.getMinute() / 60.0);
                    double horaFimDec = fim.getHour() + (fim.getMinute() / 60.0);
                    if (horaFimDec <= horaIniDec) {
                        horaFimDec = horaIniDec + 3.0; // fallback mínimo de 3 horas
                    }

                    int y = (int) Math.round((horaIniDec - HORA_INICIAL) * ALTURA_HORA);
                    int h = (int) Math.round((horaFimDec - horaIniDec) * ALTURA_HORA);
                    h = Math.max(34, h); // altura mínima

                    // Posicionamento X lado a lado para não esconder nenhum evento
                    card.putClientProperty("colIndex", colIndex);
                    card.putClientProperty("totalColunas", totalColunas);
                    card.putClientProperty("posY", y);
                    card.putClientProperty("altH", h);

                    add(card);
                }
            }
        }

        @Override
        public void doLayout() {
            int larg = getWidth();
            for (Component c : getComponents()) {
                if (c instanceof JComponent jc) {
                    Integer colIndex = (Integer) jc.getClientProperty("colIndex");
                    Integer totalCols = (Integer) jc.getClientProperty("totalColunas");
                    Integer y = (Integer) jc.getClientProperty("posY");
                    Integer h = (Integer) jc.getClientProperty("altH");

                    if (colIndex != null && totalCols != null && y != null && h != null) {
                        int largCol = Math.max(20, (larg - 6) / totalCols);
                        int x = 3 + (colIndex * largCol);
                        c.setBounds(x, y, largCol - 3, h);
                    }
                }
            }
        }

        private List<List<OrdemServico>> agruparSobreposicoes(List<OrdemServico> lista) {
            List<List<OrdemServico>> grupos = new ArrayList<>();
            for (OrdemServico os : lista) {
                boolean alocado = false;
                for (List<OrdemServico> g : grupos) {
                    boolean conflitaComAlgum = false;
                    for (OrdemServico membro : g) {
                        if (os.temSobreposicaoCom(membro)) {
                            conflitaComAlgum = true;
                            break;
                        }
                    }
                    if (conflitaComAlgum) {
                        g.add(os);
                        alocado = true;
                        break;
                    }
                }
                if (!alocado) {
                    List<OrdemServico> novoG = new ArrayList<>();
                    novoG.add(os);
                    grupos.add(novoG);
                }
            }
            return grupos;
        }

        private JPanel criarCardSemana(OrdemServico os, boolean temConflito) {
            JPanel card = new JPanel(new BorderLayout(2, 2));

            Color corFundo;
            Color corBorda;
            Color corTexto;

            if (temConflito) {
                corFundo = new Color(254, 242, 242);
                corBorda = new Color(239, 68, 68);
                corTexto = new Color(153, 27, 27);
            } else if ("Confirmado".equalsIgnoreCase(os.getStatus())) {
                corFundo = new Color(240, 253, 244);
                corBorda = new Color(34, 197, 94);
                corTexto = new Color(22, 101, 52);
            } else if ("Cancelado".equalsIgnoreCase(os.getStatus())) {
                corFundo = new Color(248, 250, 252);
                corBorda = new Color(203, 213, 225);
                corTexto = new Color(100, 116, 139);
            } else {
                corFundo = new Color(254, 252, 232);
                corBorda = new Color(234, 179, 8);
                corTexto = new Color(133, 77, 14);
            }

            card.setBackground(corFundo);
            card.setBorder(new CompoundBorder(
                    new LineBorder(corBorda, temConflito ? 2 : 1, true),
                    new EmptyBorder(3, 4, 3, 4)
            ));

            JLabel lblHora = new JLabel(os.getHorarioFormatado());
            if (temConflito) {
                lblHora.setIcon(com.festalia.util.IconesFestalia.alerta(10, new Color(220, 38, 38)));
                lblHora.setIconTextGap(3);
            }
            lblHora.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblHora.setForeground(temConflito ? new Color(220, 38, 38) : corTexto);
            card.add(lblHora, BorderLayout.NORTH);

            JLabel lblNome = new JLabel("<html><b>" + os.getCliente() + "</b><br/><small>" + os.getTipoEvento() + "</small></html>");
            lblNome.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblNome.setForeground(corTexto);
            card.add(lblNome, BorderLayout.CENTER);

            // Tooltip detalhada
            card.setToolTipText("<html><b>OS #" + os.getNumeroOS() + " • " + os.getCliente() + "</b><br/>"
                    + (temConflito ? "<b style='color:#dc2626;'>[Conflito de Horário!]</b><br/>" : "")
                    + "Horário: " + os.getHorarioFormatado() + "<br/>"
                    + "Tipo: " + os.getTipoEvento() + "<br/>"
                    + "Status: " + os.getStatus() + "<br/>"
                    + "Valor: " + os.getValorTotalFormatado() + "<br/>"
                    + "<i>Clique para ver detalhes e ações</i></html>");

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
}
