package com.festalia.calendario;

import com.festalia.Cardapio;
import com.festalia.CardapioDAO;
import com.festalia.OrdemServico;
import com.festalia.OrdemServicoDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Diálogo rápido e moderno para criação ou edição ágil de reservas diretamente pelo Calendário.
 * Possui validação completa de conflito antes de salvar.
 */
public class NovaReservaRapidaDialog extends JDialog {

    private final OrdemServicoDAO osDAO;
    private final CardapioDAO cardapioDAO;
    private final List<OrdemServico> listaExistente;
    private final Runnable onSucessoCallback;

    private JTextField txtCliente;
    private JTextField txtTelefone;
    private JTextField txtData;
    private JComboBox<String> cbHorarioInicio;
    private JComboBox<String> cbHorarioFim;
    private JComboBox<String> cbTipoEvento;
    private JTextField txtLocal;
    private JSpinner spConvidados;
    private JComboBox<Cardapio> cbCardapio;
    private JComboBox<String> cbStatus;
    private JTextArea txtObservacoes;

    public NovaReservaRapidaDialog(Window owner, LocalDate dataInicial, LocalTime horarioInicial,
                                   List<OrdemServico> listaExistente, OrdemServicoDAO osDAO,
                                   CardapioDAO cardapioDAO, Runnable onSucessoCallback) {
        super(owner, "Nova Reserva de Evento - Calendário", ModalityType.APPLICATION_MODAL);
        this.listaExistente = listaExistente;
        this.osDAO = osDAO;
        this.cardapioDAO = cardapioDAO;
        this.onSucessoCallback = onSucessoCallback;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterface(dataInicial, horarioInicial);
        pack();
        setSize(Math.max(540, getWidth()), Math.min(620, getHeight() + 20));
        setLocationRelativeTo(owner);
    }

    private void construirInterface(LocalDate dataInicial, LocalTime horarioInicial) {
        setLayout(new BorderLayout());
        JPanel principal = new JPanel(new BorderLayout(10, 12));
        principal.setBorder(new EmptyBorder(16, 18, 16, 18));
        principal.setBackground(new Color(250, 252, 255));

        // Cabeçalho
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        JLabel lblTitulo = new JLabel("📅 Agendar Nova Reserva no Calendário");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(new Color(30, 58, 138));
        topo.add(lblTitulo, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Preencha os dados do evento. O sistema verificará automaticamente conflitos de horários.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.DARK_GRAY);
        topo.add(lblSub, BorderLayout.SOUTH);
        principal.add(topo, BorderLayout.NORTH);

        // Formulário em GridBagLayout
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cliente
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Cliente / Contratante: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        txtCliente = new JTextField();
        txtCliente.putClientProperty("JTextField.placeholderText", "Nome do cliente ou empresa");
        form.add(txtCliente, gbc);

        // Telefone
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Telefone / WhatsApp:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        txtTelefone = new JTextField();
        txtTelefone.putClientProperty("JTextField.placeholderText", "(00) 00000-0000");
        form.add(txtTelefone, gbc);

        // Data
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Data do Evento (dd/MM/yyyy): *"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        LocalDate dt = dataInicial != null ? dataInicial : LocalDate.now();
        txtData = new JTextField(dt.format(OrdemServico.FORMATO_DATA));
        form.add(txtData, gbc);

        // Horários
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Faixa de Horário: *"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        JPanel painelHoras = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        painelHoras.setOpaque(false);

        String[] horas = gerarOpcoesHoras();
        cbHorarioInicio = new JComboBox<>(horas);
        cbHorarioFim = new JComboBox<>(horas);

        if (horarioInicial != null) {
            cbHorarioInicio.setSelectedItem(horarioInicial.format(DateTimeFormatter.ofPattern("HH:mm")));
            cbHorarioFim.setSelectedItem(horarioInicial.plusHours(4).format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            cbHorarioInicio.setSelectedItem("18:00");
            cbHorarioFim.setSelectedItem("22:00");
        }

        painelHoras.add(new JLabel("Início:"));
        painelHoras.add(cbHorarioInicio);
        painelHoras.add(new JLabel("Término:"));
        painelHoras.add(cbHorarioFim);
        form.add(painelHoras, gbc);

        // Tipo de Evento
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(new JLabel("Tipo de Evento:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 0.7;
        String[] tipos = {"Casamento", "Aniversário", "15 Anos", "Corporativo", "Festa Infantil", "Formatura", "Outro"};
        cbTipoEvento = new JComboBox<>(tipos);
        form.add(cbTipoEvento, gbc);

        // Espaço / Local
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(new JLabel("Espaço / Local:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        txtLocal = new JTextField("Espaço Festalia Garden - Salão Nobre");
        form.add(txtLocal, gbc);

        // Convidados
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(new JLabel("Qtd. Convidados:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.7;
        spConvidados = new JSpinner(new SpinnerNumberModel(100, 1, 5000, 10));
        form.add(spConvidados, gbc);

        // Cardápio
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0.3;
        form.add(new JLabel("Cardápio Inicial:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 0.7;
        cbCardapio = new JComboBox<>();
        try {
            List<Cardapio> listaCardapios = cardapioDAO.listarTodos();
            for (Cardapio c : listaCardapios) {
                cbCardapio.addItem(c);
            }
        } catch (Exception ignored) {}
        form.add(cbCardapio, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.3;
        form.add(new JLabel("Status da Reserva:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; gbc.weightx = 0.7;
        String[] statusOpcoes = {"Orçamento", "Confirmado"};
        cbStatus = new JComboBox<>(statusOpcoes);
        form.add(cbStatus, gbc);

        // Observações
        gbc.gridx = 0; gbc.gridy = 9; gbc.weightx = 0.3;
        form.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1; gbc.gridy = 9; gbc.weightx = 0.7;
        txtObservacoes = new JTextArea(2, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        form.add(new JScrollPane(txtObservacoes), gbc);

        principal.add(new JScrollPane(form), BorderLayout.CENTER);

        // Botões Salvar e Cancelar
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rodape.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> dispose());

        JButton btnSalvar = new JButton("💾 Salvar Reserva");
        btnSalvar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalvar.setBackground(new Color(22, 101, 52));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> salvarReserva());

        rodape.add(btnCancelar);
        rodape.add(btnSalvar);
        principal.add(rodape, BorderLayout.SOUTH);

        add(principal);
    }

    private String[] gerarOpcoesHoras() {
        String[] opts = new String[48];
        int idx = 0;
        for (int h = 0; h < 24; h++) {
            opts[idx++] = String.format("%02d:00", h);
            opts[idx++] = String.format("%02d:30", h);
        }
        return opts;
    }

    private void salvarReserva() {
        String cliente = txtCliente.getText().trim();
        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o nome do cliente / contratante.", "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtCliente.requestFocus();
            return;
        }

        LocalDate data;
        try {
            data = LocalDate.parse(txtData.getText().trim(), OrdemServico.FORMATO_DATA);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Data do evento inválida! Use o formato dd/MM/yyyy.", "Data Inválida", JOptionPane.ERROR_MESSAGE);
            txtData.requestFocus();
            return;
        }

        LocalTime inicio = LocalTime.parse((String) cbHorarioInicio.getSelectedItem());
        LocalTime fim = LocalTime.parse((String) cbHorarioFim.getSelectedItem());

        Cardapio cardapio = (Cardapio) cbCardapio.getSelectedItem();
        int convidados = (Integer) spConvidados.getValue();
        double preco = cardapio != null ? cardapio.getPrecoPorPessoa() : 120.0;

        OrdemServico nova = new OrdemServico(
                0,
                0,
                "Festalia Buffet & Eventos",
                "Gestão Profissional de Buffets e Eventos",
                null,
                cliente,
                txtTelefone.getText().trim(),
                data,
                inicio,
                fim,
                (String) cbTipoEvento.getSelectedItem(),
                false,
                txtLocal.getText().trim(),
                convidados,
                preco,
                0.0,
                0.0,
                0.0,
                (String) cbStatus.getSelectedItem(),
                cardapio,
                txtObservacoes.getText().trim(),
                null
        );
        nova.calcularValorTotalAutomatico();

        // 1. Verificação Automática de Conflito de Reservas
        List<OrdemServico> conflitos = ReservaConflitoValidator.verificarConflitos(nova, listaExistente);
        if (!conflitos.isEmpty()) {
            ReservaConflitoValidator.RespostaConflito resp = ReservaConflitoValidator.exibirModalConflito(this, nova, conflitos);
            if (resp == ReservaConflitoValidator.RespostaConflito.CANCELAR) {
                return;
            } else if (resp == ReservaConflitoValidator.RespostaConflito.VOLTAR_ALTERAR_HORARIO) {
                cbHorarioInicio.requestFocus();
                return;
            } else {
                // Confirmou exceção de conflito
                nova.setConflitoConfirmado(true);
            }
        }

        // 2. Persistência no Banco de Dados
        try {
            osDAO.salvar(nova);
            JOptionPane.showMessageDialog(this,
                    "Reserva OS #" + nova.getNumeroOS() + " cadastrada com sucesso no Calendário!",
                    "Reserva Confirmada", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSucessoCallback != null) {
                onSucessoCallback.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar a reserva no banco de dados: " + ex.getMessage(),
                    "Erro ao Salvar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
