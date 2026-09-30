package com.festalia.calendario;

import com.festalia.CardapioDAO;
import com.festalia.OrdemServico;
import com.festalia.OrdemServicoDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Painel Principal do Calendário Visual de Reservas do Festalia ERP.
 * Integra as visões de Mês, Semana e Dia, barra de filtros avançados,
 * alerta de conflitos e criação/edição ágil de reservas.
 */
public class CalendarioPanel extends JPanel {

    public enum ModoVisualizacao {
        MES,
        SEMANA,
        DIA
    }

    private final OrdemServicoDAO osDAO;
    private final CardapioDAO cardapioDAO;
    private final DetalhesReservaDialog.ReservaListener listenerExterno;

    private ModoVisualizacao modoAtual = ModoVisualizacao.MES;
    private LocalDate dataReferencia = LocalDate.now();
    private List<OrdemServico> todasReservas = new ArrayList<>();
    private List<OrdemServico> reservasFiltradas = new ArrayList<>();

    // Componentes de topo
    private JLabel lblPeriodoTitulo;
    private JToggleButton btnModoMes;
    private JToggleButton btnModoSemana;
    private JToggleButton btnModoDia;

    private JComboBox<String> cbFiltroStatus;
    private JComboBox<String> cbFiltroTipo;
    private JCheckBox chkApenasConflitos;
    private JTextField txtBuscaRapida;
    private JLabel lblContadorConflitos;

    // Painéis de visualização
    private JPanel painelCentroConteudo;
    private VisaoMesPanel visaoMes;
    private VisaoSemanaPanel visaoSemana;
    private VisaoDiaPanel visaoDia;

    public CalendarioPanel(OrdemServicoDAO osDAO, CardapioDAO cardapioDAO, DetalhesReservaDialog.ReservaListener listenerExterno) {
        this.osDAO = osDAO;
        this.cardapioDAO = cardapioDAO;
        this.listenerExterno = listenerExterno;

        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        construirBarraSuperior();
        construirAreaVisualizacao();
        recarregarDados();
    }

    private void construirBarraSuperior() {
        JPanel painelTopo = new JPanel(new BorderLayout(10, 8));
        painelTopo.setOpaque(false);

        // Linha 1: Navegação (◀ Hoje ▶), Título do Período, Modos (Mês/Semana/Dia) e Nova Reserva
        JPanel linha1 = new JPanel(new BorderLayout(10, 0));
        linha1.setOpaque(false);

        // Esquerda: Navegação
        JPanel navEsq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navEsq.setOpaque(false);

        JButton btnAnt = new JButton("◀");
        btnAnt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAnt.setToolTipText("Período Anterior");
        btnAnt.addActionListener(e -> navegarPeriodo(-1));

        JButton btnHoje = new JButton("Hoje");
        btnHoje.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHoje.setBackground(new Color(239, 246, 255));
        btnHoje.setForeground(new Color(29, 78, 216));
        btnHoje.setToolTipText("Voltar para a data atual");
        btnHoje.addActionListener(e -> {
            dataReferencia = LocalDate.now();
            atualizarPeriodoEViews();
        });

        JButton btnProx = new JButton("▶");
        btnProx.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnProx.setToolTipText("Próximo Período");
        btnProx.addActionListener(e -> navegarPeriodo(1));

        lblPeriodoTitulo = new JLabel("Outubro de 2026");
        lblPeriodoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblPeriodoTitulo.setForeground(new Color(15, 23, 42));
        lblPeriodoTitulo.setBorder(new EmptyBorder(0, 10, 0, 10));

        navEsq.add(btnAnt);
        navEsq.add(btnHoje);
        navEsq.add(btnProx);
        navEsq.add(lblPeriodoTitulo);
        linha1.add(navEsq, BorderLayout.WEST);

        // Direita: Seletor de Modo (Mês / Semana / Dia) + Botão Nova Reserva
        JPanel navDir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navDir.setOpaque(false);

        btnModoMes = new JToggleButton("Mês", com.festalia.util.IconesFestalia.calendario(13, new Color(71, 85, 105)), true);
        btnModoMes.setIconTextGap(4);
        btnModoSemana = new JToggleButton("Semana", com.festalia.util.IconesFestalia.calendario(13, new Color(71, 85, 105)), false);
        btnModoSemana.setIconTextGap(4);
        btnModoDia = new JToggleButton("Dia", com.festalia.util.IconesFestalia.horario(13, new Color(71, 85, 105)), false);
        btnModoDia.setIconTextGap(4);

        ButtonGroup grupoModos = new ButtonGroup();
        grupoModos.add(btnModoMes);
        grupoModos.add(btnModoSemana);
        grupoModos.add(btnModoDia);

        btnModoMes.addActionListener(e -> alterarModo(ModoVisualizacao.MES));
        btnModoSemana.addActionListener(e -> alterarModo(ModoVisualizacao.SEMANA));
        btnModoDia.addActionListener(e -> alterarModo(ModoVisualizacao.DIA));

        JButton btnNovaReserva = new JButton("Nova Reserva", com.festalia.util.IconesFestalia.mais(13, Color.WHITE));
        btnNovaReserva.setIconTextGap(6);
        btnNovaReserva.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNovaReserva.setBackground(new Color(22, 101, 52));
        btnNovaReserva.setForeground(Color.WHITE);
        btnNovaReserva.setToolTipText("Agendar nova reserva diretamente no calendário com verificação de conflito");
        btnNovaReserva.addActionListener(e -> abrirNovaReservaModal(dataReferencia, LocalTime.of(18, 0)));

        navDir.add(btnModoMes);
        navDir.add(btnModoSemana);
        navDir.add(btnModoDia);
        navDir.add(Box.createHorizontalStrut(6));
        navDir.add(btnNovaReserva);
        linha1.add(navDir, BorderLayout.EAST);

        painelTopo.add(linha1, BorderLayout.NORTH);

        // Linha 2: Barra de Filtros e Busca Rápida
        JPanel linhaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        linhaFiltros.setBackground(Color.WHITE);
        linhaFiltros.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        // Filtro Status
        linhaFiltros.add(new JLabel("Status:"));
        cbFiltroStatus = new JComboBox<>(new String[]{"Todos os Status", "Confirmado", "Orçamento", "Realizado", "Cancelado"});
        cbFiltroStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbFiltroStatus.addActionListener(e -> aplicarFiltros());
        linhaFiltros.add(cbFiltroStatus);

        // Filtro Tipo de Evento
        linhaFiltros.add(new JLabel("Tipo:"));
        cbFiltroTipo = new JComboBox<>(new String[]{"Todos os Tipos", "Casamento", "Aniversário", "15 Anos", "Corporativo", "Festa Infantil", "Formatura", "Outro"});
        cbFiltroTipo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbFiltroTipo.addActionListener(e -> aplicarFiltros());
        linhaFiltros.add(cbFiltroTipo);

        // Checkbox Apenas Conflitos
        chkApenasConflitos = new JCheckBox("Apenas Conflitos");
        chkApenasConflitos.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkApenasConflitos.setForeground(new Color(194, 65, 12));
        chkApenasConflitos.setOpaque(false);
        chkApenasConflitos.addActionListener(e -> aplicarFiltros());
        linhaFiltros.add(chkApenasConflitos);

        // Campo de Busca
        linhaFiltros.add(Box.createHorizontalStrut(8));
        txtBuscaRapida = new JTextField(16);
        txtBuscaRapida.putClientProperty("JTextField.placeholderText", "Buscar cliente ou local...");
        txtBuscaRapida.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtBuscaRapida.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        linhaFiltros.add(txtBuscaRapida);

        // Contador de Conflitos
        lblContadorConflitos = new JLabel("");
        lblContadorConflitos.setFont(new Font("Segoe UI", Font.BOLD, 11));
        linhaFiltros.add(Box.createHorizontalGlue());
        linhaFiltros.add(lblContadorConflitos);

        painelTopo.add(linhaFiltros, BorderLayout.SOUTH);
        add(painelTopo, BorderLayout.NORTH);
    }

    private void construirAreaVisualizacao() {
        painelCentroConteudo = new JPanel(new CardLayout());
        painelCentroConteudo.setOpaque(false);

        // 1. Visão Mês
        visaoMes = new VisaoMesPanel(YearMonth.from(dataReferencia), reservasFiltradas, new VisaoMesPanel.VisaoMesListener() {
            @Override
            public void onEventoClicado(OrdemServico os) {
                abrirDetalhesReservaModal(os);
            }

            @Override
            public void onDiaClicado(LocalDate data) {
                dataReferencia = data;
            }

            @Override
            public void onDiaDuploClique(LocalDate data) {
                dataReferencia = data;
                abrirNovaReservaModal(data, LocalTime.of(18, 0));
            }
        });

        // 2. Visão Semana
        visaoSemana = new VisaoSemanaPanel(dataReferencia, reservasFiltradas, new VisaoSemanaPanel.VisaoSemanaListener() {
            @Override
            public void onEventoClicado(OrdemServico os) {
                abrirDetalhesReservaModal(os);
            }

            @Override
            public void onSlotClicado(LocalDate data, LocalTime horario) {
                dataReferencia = data;
                abrirNovaReservaModal(data, horario);
            }
        });

        // 3. Visão Dia
        visaoDia = new VisaoDiaPanel(dataReferencia, reservasFiltradas, new VisaoDiaPanel.VisaoDiaListener() {
            @Override
            public void onEventoClicado(OrdemServico os) {
                abrirDetalhesReservaModal(os);
            }

            @Override
            public void onNovoEventoSlot(LocalDate data, LocalTime hora) {
                dataReferencia = data;
                abrirNovaReservaModal(data, hora);
            }
        });

        painelCentroConteudo.add(visaoMes, ModoVisualizacao.MES.name());
        painelCentroConteudo.add(visaoSemana, ModoVisualizacao.SEMANA.name());
        painelCentroConteudo.add(visaoDia, ModoVisualizacao.DIA.name());

        add(painelCentroConteudo, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        todasReservas = osDAO.listarTodas();
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String statusFiltro = (String) cbFiltroStatus.getSelectedItem();
        String tipoFiltro = (String) cbFiltroTipo.getSelectedItem();
        boolean apenasConflitos = chkApenasConflitos.isSelected();
        String busca = txtBuscaRapida.getText().trim().toLowerCase();

        reservasFiltradas = new ArrayList<>();
        int conflitosDetectados = 0;

        for (OrdemServico os : todasReservas) {
            // Filtro por Status
            if (statusFiltro != null && !"Todos os Status".equals(statusFiltro)) {
                if (!statusFiltro.equalsIgnoreCase(os.getStatus())) {
                    continue;
                }
            }

            // Filtro por Tipo de Evento
            if (tipoFiltro != null && !"Todos os Tipos".equals(tipoFiltro)) {
                if (!tipoFiltro.equalsIgnoreCase(os.getTipoEvento())) {
                    continue;
                }
            }

            // Conflito
            boolean temConflito = false;
            for (OrdemServico outro : todasReservas) {
                if (os.temSobreposicaoCom(outro)) {
                    temConflito = true;
                    break;
                }
            }
            if (temConflito) {
                conflitosDetectados++;
            }
            if (apenasConflitos && !temConflito) {
                continue;
            }

            // Busca por texto
            if (!busca.isEmpty()) {
                String c = os.getCliente() != null ? os.getCliente().toLowerCase() : "";
                String loc = os.getLocal() != null ? os.getLocal().toLowerCase() : "";
                String num = String.valueOf(os.getNumeroOS());
                if (!c.contains(busca) && !loc.contains(busca) && !num.contains(busca)) {
                    continue;
                }
            }

            reservasFiltradas.add(os);
        }

        if (conflitosDetectados > 0) {
            lblContadorConflitos.setText(conflitosDetectados + " conflito(s) de horário no total");
            lblContadorConflitos.setIcon(com.festalia.util.IconesFestalia.alerta(13, new Color(220, 38, 38)));
            lblContadorConflitos.setIconTextGap(5);
            lblContadorConflitos.setForeground(new Color(220, 38, 38));
        } else {
            lblContadorConflitos.setText("Nenhum conflito de horário");
            lblContadorConflitos.setIcon(com.festalia.util.IconesFestalia.confirmar(13, new Color(22, 101, 52)));
            lblContadorConflitos.setIconTextGap(5);
            lblContadorConflitos.setForeground(new Color(22, 101, 52));
        }

        atualizarPeriodoEViews();
    }

    private void alterarModo(ModoVisualizacao novoModo) {
        this.modoAtual = novoModo;
        CardLayout cl = (CardLayout) painelCentroConteudo.getLayout();
        cl.show(painelCentroConteudo, novoModo.name());
        atualizarPeriodoEViews();
    }

    private void navegarPeriodo(int direcao) {
        switch (modoAtual) {
            case MES -> dataReferencia = dataReferencia.plusMonths(direcao);
            case SEMANA -> dataReferencia = dataReferencia.plusWeeks(direcao);
            case DIA -> dataReferencia = dataReferencia.plusDays(direcao);
        }
        atualizarPeriodoEViews();
    }

    private void atualizarPeriodoEViews() {
        Locale ptBr = new Locale("pt", "BR");

        switch (modoAtual) {
            case MES -> {
                YearMonth ym = YearMonth.from(dataReferencia);
                String mesExtenso = ym.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", ptBr));
                mesExtenso = mesExtenso.substring(0, 1).toUpperCase() + mesExtenso.substring(1);
                lblPeriodoTitulo.setText(mesExtenso);
                visaoMes.atualizar(ym, reservasFiltradas);
            }
            case SEMANA -> {
                int diaSemana = dataReferencia.getDayOfWeek().getValue();
                int recuo = (diaSemana == 7) ? 0 : diaSemana;
                LocalDate dom = dataReferencia.minusDays(recuo);
                LocalDate sab = dom.plusDays(6);

                String titSemana = String.format("Semana %d: %02d/%02d a %02d/%02d/%d",
                        dom.get(WeekFields.of(ptBr).weekOfYear()),
                        dom.getDayOfMonth(), dom.getMonthValue(),
                        sab.getDayOfMonth(), sab.getMonthValue(), sab.getYear());
                lblPeriodoTitulo.setText(titSemana);
                visaoSemana.atualizar(dataReferencia, reservasFiltradas);
            }
            case DIA -> {
                String diaTit = dataReferencia.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", ptBr));
                diaTit = diaTit.substring(0, 1).toUpperCase() + diaTit.substring(1);
                lblPeriodoTitulo.setText(diaTit);
                visaoDia.atualizar(dataReferencia, reservasFiltradas);
            }
        }
    }

    private void abrirDetalhesReservaModal(OrdemServico os) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        DetalhesReservaDialog dlg = new DetalhesReservaDialog(owner, os, todasReservas, osDAO, new DetalhesReservaDialog.ReservaListener() {
            @Override
            public void onReservaAlterada() {
                recarregarDados();
                if (listenerExterno != null) {
                    listenerExterno.onReservaAlterada();
                }
            }

            @Override
            public void onSolicitarEdicao(OrdemServico osParaEditar) {
                if (listenerExterno != null) {
                    listenerExterno.onSolicitarEdicao(osParaEditar);
                }
            }
        });
        dlg.setVisible(true);
    }

    private void abrirNovaReservaModal(LocalDate data, LocalTime horario) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        NovaReservaRapidaDialog dlg = new NovaReservaRapidaDialog(
                owner,
                data != null ? data : dataReferencia,
                horario != null ? horario : LocalTime.of(18, 0),
                todasReservas,
                osDAO,
                cardapioDAO,
                () -> {
                    recarregarDados();
                    if (listenerExterno != null) {
                        listenerExterno.onReservaAlterada();
                    }
                }
        );
        dlg.setVisible(true);
    }
}
