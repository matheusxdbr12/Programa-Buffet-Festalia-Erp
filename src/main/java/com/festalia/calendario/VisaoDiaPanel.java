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
import java.util.List;

/**
 * Visualização Diária Detalhada do Calendário do Festalia ERP.
 * Apresenta o dia selecionado com linha do tempo de 07:00 às 23:00,
 * cards ricos com métricas completas do evento e destaque imediato
 * para quaisquer conflitos de horários.
 */
public class VisaoDiaPanel extends JPanel {

    public interface VisaoDiaListener {
        void onEventoClicado(OrdemServico os);
        void onNovoEventoSlot(LocalDate data, LocalTime hora);
    }

    private LocalDate dataSelecionada;
    private List<OrdemServico> eventosDoDia;
    private final VisaoDiaListener listener;

    private static final int HORA_INICIAL = 7;
    private static final int HORA_FINAL = 23;
    private static final int ALTURA_HORA = 60; // pixels por hora na visão diária (mais espaçoso)

    public VisaoDiaPanel(LocalDate dataInicial, List<OrdemServico> todosEventos, VisaoDiaListener listener) {
        this.dataSelecionada = dataInicial != null ? dataInicial : LocalDate.now();
        this.listener = listener;
        filtrarEventosDoDia(todosEventos);

        setLayout(new BorderLayout());
        setOpaque(false);
        renderizar();
    }

    public void atualizar(LocalDate data, List<OrdemServico> todosEventos) {
        this.dataSelecionada = data != null ? data : LocalDate.now();
        filtrarEventosDoDia(todosEventos);
        renderizar();
    }

    private void filtrarEventosDoDia(List<OrdemServico> todos) {
        this.eventosDoDia = new ArrayList<>();
        if (todos != null) {
            for (OrdemServico os : todos) {
                if (os != null && dataSelecionada.equals(os.getDataEvento())) {
                    this.eventosDoDia.add(os);
                }
            }
        }
    }

    private void renderizar() {
        removeAll();

        // 1. Topo Informativo do Dia
        JPanel painelTopo = new JPanel(new BorderLayout(10, 4));
        painelTopo.setBackground(Color.WHITE);
        painelTopo.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel textos = new JPanel(new GridLayout(2, 1, 2, 2));
        textos.setOpaque(false);

        String nomeDiaSemana = dataSelecionada.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR"));
        nomeDiaSemana = nomeDiaSemana.substring(0, 1).toUpperCase() + nomeDiaSemana.substring(1);

        JLabel lblData = new JLabel(nomeDiaSemana + ", " + dataSelecionada.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("pt", "BR"))));
        lblData.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblData.setForeground(new Color(30, 58, 138));

        int qtd = eventosDoDia.size();
        double totalDia = 0.0;
        int totalConflitos = 0;
        for (OrdemServico os : eventosDoDia) {
            if (!"Cancelado".equalsIgnoreCase(os.getStatus())) {
                totalDia += os.getValorTotal();
            }
            if (verificarConflito(os)) {
                totalConflitos++;
            }
        }

        String resumo = qtd + (qtd == 1 ? " evento agendado" : " eventos agendados") + " • Faturamento previsto: R$ " + String.format("%.2f", totalDia);
        if (totalConflitos > 0) {
            resumo += " • " + totalConflitos + " evento(s) com sobreposição de horário!";
        }
        JLabel lblResumo = new JLabel(resumo);
        lblResumo.setFont(new Font("Segoe UI", totalConflitos > 0 ? Font.BOLD : Font.PLAIN, 12));
        lblResumo.setForeground(totalConflitos > 0 ? new Color(220, 38, 38) : Color.DARK_GRAY);

        textos.add(lblData);
        textos.add(lblResumo);
        painelTopo.add(textos, BorderLayout.WEST);

        if (totalConflitos > 0) {
            JLabel lblAlertaBadge = new JLabel(" CONFLITO DE RESERVA NESTA DATA ", com.festalia.util.IconesFestalia.alerta(13, new Color(185, 28, 28)), SwingConstants.CENTER);
            lblAlertaBadge.setIconTextGap(5);
            lblAlertaBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblAlertaBadge.setOpaque(true);
            lblAlertaBadge.setBackground(new Color(254, 226, 226));
            lblAlertaBadge.setForeground(new Color(185, 28, 28));
            lblAlertaBadge.setBorder(new CompoundBorder(
                    new LineBorder(new Color(239, 68, 68), 1, true),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            painelTopo.add(lblAlertaBadge, BorderLayout.EAST);
        }
        add(painelTopo, BorderLayout.NORTH);

        // 2. Linha do Tempo e Cards do Dia
        int totalHoras = HORA_FINAL - HORA_INICIAL + 1;
        int alturaTotal = totalHoras * ALTURA_HORA;

        JPanel painelLinhaTempo = new JPanel(null);
        painelLinhaTempo.setBackground(Color.WHITE);
        painelLinhaTempo.setPreferredSize(new Dimension(750, alturaTotal));

        // Linhas de cada hora
        for (int h = HORA_INICIAL; h <= HORA_FINAL; h++) {
            int y = (h - HORA_INICIAL) * ALTURA_HORA;

            JLabel lblH = new JLabel(String.format("%02d:00", h));
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblH.setForeground(new Color(100, 116, 139));
            lblH.setBounds(14, y + 2, 55, 18);
            painelLinhaTempo.add(lblH);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(241, 245, 249));
            sep.setBounds(75, y, 2000, 1);
            painelLinhaTempo.add(sep);
        }

        // Interação: duplo clique para criar evento na hora clicada
        painelLinhaTempo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int hora = HORA_INICIAL + (e.getY() / ALTURA_HORA);
                    hora = Math.max(0, Math.min(23, hora));
                    if (listener != null) {
                        listener.onNovoEventoSlot(dataSelecionada, LocalTime.of(hora, 0));
                    }
                }
            }
        });

        // 3. Posicionar os Cards dos Eventos na Linha do Tempo
        posicionarCardsDia(painelLinhaTempo);

        JScrollPane scroll = new JScrollPane(painelLinhaTempo);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private boolean verificarConflito(OrdemServico os) {
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

    private void posicionarCardsDia(JPanel container) {
        if (eventosDoDia.isEmpty()) {
            JLabel lblVazio = new JLabel("Nenhum evento agendado para esta data. Dê um duplo clique em qualquer horário para cadastrar uma nova reserva.");
            lblVazio.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblVazio.setForeground(new Color(148, 163, 184));
            lblVazio.setBounds(90, 40, 650, 30);
            container.add(lblVazio);
            return;
        }

        // Agrupar eventos sobrepostos para dispor lado a lado
        List<List<OrdemServico>> grupos = new ArrayList<>();
        for (OrdemServico os : eventosDoDia) {
            boolean adicionado = false;
            for (List<OrdemServico> g : grupos) {
                for (OrdemServico m : g) {
                    if (os.temSobreposicaoCom(m)) {
                        g.add(os);
                        adicionado = true;
                        break;
                    }
                }
                if (adicionado) break;
            }
            if (!adicionado) {
                List<OrdemServico> ng = new ArrayList<>();
                ng.add(os);
                grupos.add(ng);
            }
        }

        // Para permitir redimensionamento responsivo dos cards
        container.setLayout(new LayoutManager() {
            @Override public void addLayoutComponent(String name, Component comp) {}
            @Override public void removeLayoutComponent(Component comp) {}
            @Override public Dimension preferredLayoutSize(Container parent) { return parent.getPreferredSize(); }
            @Override public Dimension minimumLayoutSize(Container parent) { return parent.getMinimumSize(); }

            @Override
            public void layoutContainer(Container parent) {
                int larguraTotal = parent.getWidth();
                int xOffset = 80;
                int larguraDisponivel = Math.max(300, larguraTotal - xOffset - 25);

                for (List<OrdemServico> grupo : grupos) {
                    int totalCols = grupo.size();
                    int largCol = larguraDisponivel / totalCols;

                    for (int c = 0; c < totalCols; c++) {
                        OrdemServico os = grupo.get(c);
                        Component comp = encontrarComponentePorOS(parent, os);
                        if (comp != null) {
                            LocalTime ini = os.getHorarioInicio();
                            LocalTime fim = os.getHorarioFim();

                            double iniDec = ini.getHour() + (ini.getMinute() / 60.0);
                            double fimDec = fim.getHour() + (fim.getMinute() / 60.0);
                            if (fimDec <= iniDec) fimDec = iniDec + 3.0;

                            int y = (int) Math.round((iniDec - HORA_INICIAL) * ALTURA_HORA);
                            int h = (int) Math.round((fimDec - iniDec) * ALTURA_HORA);
                            h = Math.max(56, h);

                            int x = xOffset + (c * largCol);
                            comp.setBounds(x, y, largCol - 8, h);
                        }
                    }
                }
            }
        });

        for (OrdemServico os : eventosDoDia) {
            boolean temConflito = verificarConflito(os);
            JPanel card = criarCardDiaCompleto(os, temConflito);
            card.putClientProperty("OS_OBJ", os);
            container.add(card);
        }
    }

    private Component encontrarComponentePorOS(Container c, OrdemServico os) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JComponent jc && jc.getClientProperty("OS_OBJ") == os) {
                return comp;
            }
        }
        return null;
    }

    private JPanel criarCardDiaCompleto(OrdemServico os, boolean temConflito) {
        JPanel card = new JPanel(new BorderLayout(8, 4));

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
                new EmptyBorder(6, 10, 6, 10)
        ));

        // Topo do Card: Horário, Tipo de Evento e Status
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);

        String titHora = (temConflito ? "[CONFLITO] " : "") + os.getHorarioFormatado();
        JLabel lblHora = new JLabel(titHora);
        lblHora.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHora.setForeground(temConflito ? new Color(220, 38, 38) : corTexto);
        topo.add(lblHora, BorderLayout.WEST);

        JLabel lblBadge = new JLabel(" " + os.getStatus() + " ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(Color.WHITE);
        lblBadge.setBorder(new LineBorder(corBorda, 1, true));
        lblBadge.setForeground(corTexto);
        topo.add(lblBadge, BorderLayout.EAST);
        card.add(topo, BorderLayout.NORTH);

        // Corpo do Card: Cliente, Local, Convidados e Valor
        JPanel corpo = new JPanel(new GridLayout(2, 2, 8, 2));
        corpo.setOpaque(false);

        JLabel lblCliente = new JLabel(os.getCliente() + " (" + os.getTipoEvento() + ")", com.festalia.util.IconesFestalia.cliente(13, new Color(37, 99, 235)), SwingConstants.LEFT);
        lblCliente.setIconTextGap(5);
        lblCliente.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblLocal = new JLabel((os.getLocal() != null && !os.getLocal().isBlank() ? os.getLocal() : "Espaço Festalia"), com.festalia.util.IconesFestalia.local(13, new Color(100, 116, 139)), SwingConstants.LEFT);
        lblLocal.setIconTextGap(5);
        lblLocal.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLocal.setForeground(Color.DARK_GRAY);

        JLabel lblConvidados = new JLabel(os.getQuantidadeConvidados() + " convidados", com.festalia.util.IconesFestalia.convidados(13, new Color(100, 116, 139)), SwingConstants.LEFT);
        lblConvidados.setIconTextGap(5);
        lblConvidados.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblConvidados.setForeground(Color.DARK_GRAY);

        JLabel lblTotal = new JLabel("Total: " + os.getValorTotalFormatado());
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(new Color(22, 101, 52));

        corpo.add(lblCliente);
        corpo.add(lblLocal);
        corpo.add(lblConvidados);
        corpo.add(lblTotal);
        card.add(corpo, BorderLayout.CENTER);

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
