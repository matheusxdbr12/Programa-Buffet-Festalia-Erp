package com.festalia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para o modelo OrdemServico.
 * Gerencia o histórico de eventos, taxas de serviço/transporte e faturamento no SQLite.
 */
public class OrdemServicoDAO {

    private static final DateTimeFormatter ISO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATA_HORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void salvar(OrdemServico os) throws SQLException {
        if (os.getId() > 0) {
            atualizar(os);
        } else {
            inserir(os);
        }
    }

    private void inserir(OrdemServico os) throws SQLException {
        String sql = """
            INSERT INTO ordens_servico (
                numero_os, nome_buffet, subtitulo_buffet, caminho_logo_buffet, cliente, telefone, data_evento, local_evento,
                qtd_convidados, preco_por_pessoa, taxa_servico, taxa_transporte, valor_total, status,
                cardapio_nome, cardapio_descricao, cardapio_itens, observacoes, data_criacao,
                horario_inicio, horario_fim, tipo_evento, conflito_confirmado
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Cardapio c = os.getCardapioSelecionado();
            String cardapioNome = c != null ? c.getNome() : "Cardápio Personalizado";
            String cardapioDesc = c != null ? c.getDescricao() : "";
            String cardapioItens = c != null ? c.getItensComoTexto() : "";

            ps.setInt(1, os.getNumeroOS());
            ps.setString(2, os.getNomeBuffet());
            ps.setString(3, os.getSubtituloBuffet());
            ps.setString(4, os.getCaminhoLogoBuffet());
            ps.setString(5, os.getCliente());
            ps.setString(6, os.getTelefone());
            ps.setString(7, os.getDataEvento().format(ISO_DATA));
            ps.setString(8, os.getLocal());
            ps.setInt(9, os.getQuantidadeConvidados());
            ps.setDouble(10, os.getPrecoPorPessoa());
            ps.setDouble(11, os.getTaxaServico());
            ps.setDouble(12, os.getTaxaTransporte());
            ps.setDouble(13, os.getValorTotal());
            ps.setString(14, os.getStatus());
            ps.setString(15, cardapioNome);
            ps.setString(16, cardapioDesc);
            ps.setString(17, cardapioItens);
            ps.setString(18, os.getObservacoes());
            ps.setString(19, (os.getDataCriacao() != null ? os.getDataCriacao() : LocalDateTime.now()).format(ISO_DATA_HORA));
            ps.setString(20, os.getHorarioInicioFormatado());
            ps.setString(21, os.getHorarioFimFormatado());
            ps.setString(22, os.getTipoEvento());
            ps.setInt(23, os.isConflitoConfirmado() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    os.setId(rs.getInt(1));
                }
            }
        }
    }

    private void atualizar(OrdemServico os) throws SQLException {
        String sql = """
            UPDATE ordens_servico SET
                nome_buffet = ?, subtitulo_buffet = ?, caminho_logo_buffet = ?, cliente = ?, telefone = ?, data_evento = ?, local_evento = ?,
                qtd_convidados = ?, preco_por_pessoa = ?, taxa_servico = ?, taxa_transporte = ?,
                valor_total = ?, status = ?, cardapio_nome = ?, cardapio_descricao = ?,
                cardapio_itens = ?, observacoes = ?,
                horario_inicio = ?, horario_fim = ?, tipo_evento = ?, conflito_confirmado = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Cardapio c = os.getCardapioSelecionado();
            ps.setString(1, os.getNomeBuffet());
            ps.setString(2, os.getSubtituloBuffet());
            ps.setString(3, os.getCaminhoLogoBuffet());
            ps.setString(4, os.getCliente());
            ps.setString(5, os.getTelefone());
            ps.setString(6, os.getDataEvento().format(ISO_DATA));
            ps.setString(7, os.getLocal());
            ps.setInt(8, os.getQuantidadeConvidados());
            ps.setDouble(9, os.getPrecoPorPessoa());
            ps.setDouble(10, os.getTaxaServico());
            ps.setDouble(11, os.getTaxaTransporte());
            ps.setDouble(12, os.getValorTotal());
            ps.setString(13, os.getStatus());
            ps.setString(14, c != null ? c.getNome() : "Cardápio Personalizado");
            ps.setString(15, c != null ? c.getDescricao() : "");
            ps.setString(16, c != null ? c.getItensComoTexto() : "");
            ps.setString(17, os.getObservacoes());
            ps.setString(18, os.getHorarioInicioFormatado());
            ps.setString(19, os.getHorarioFimFormatado());
            ps.setString(20, os.getTipoEvento());
            ps.setInt(21, os.isConflitoConfirmado() ? 1 : 0);
            ps.setInt(22, os.getId());

            ps.executeUpdate();
        }
    }

    public List<OrdemServico> listarTodas() {
        List<OrdemServico> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordens_servico ORDER BY numero_os DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(extrairOrdemServico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar histórico de OS: " + e.getMessage());
        }
        return lista;
    }

    public List<OrdemServico> buscarPorTermo(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodas();
        }

        List<OrdemServico> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM ordens_servico
            WHERE cliente LIKE ? OR local_evento LIKE ? OR status LIKE ? OR CAST(numero_os AS TEXT) LIKE ?
            ORDER BY numero_os DESC
        """;

        String filtro = "%" + termo.trim() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setString(4, filtro);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairOrdemServico(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ordens de serviço por termo: " + e.getMessage());
        }
        return lista;
    }

    public void atualizarStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE ordens_servico SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM ordens_servico WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public double calcularFaturamentoTotal() {
        String sql = "SELECT SUM(valor_total) FROM ordens_servico WHERE status != 'Cancelado'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular faturamento: " + e.getMessage());
        }
        return 0.0;
    }

    private OrdemServico extrairOrdemServico(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int numeroOS = rs.getInt("numero_os");
        String cliente = rs.getString("cliente");
        String telefone = rs.getString("telefone");

        String strDataEvento = rs.getString("data_evento");
        LocalDate dataEvento = LocalDate.now();
        try {
            dataEvento = LocalDate.parse(strDataEvento, ISO_DATA);
        } catch (Exception ignored) {}

        String local = rs.getString("local_evento");
        int qtdConvidados = rs.getInt("qtd_convidados");
        double precoPorPessoa = rs.getDouble("preco_por_pessoa");

        double taxaServico = 0.0;
        try {
            taxaServico = rs.getDouble("taxa_servico");
        } catch (SQLException ignored) {}

        double taxaTransporte = 0.0;
        try {
            taxaTransporte = rs.getDouble("taxa_transporte");
        } catch (SQLException ignored) {}

        double valorTotal = rs.getDouble("valor_total");
        String status = rs.getString("status");

        String cardapioNome = rs.getString("cardapio_nome");
        String cardapioDesc = rs.getString("cardapio_descricao");
        String cardapioItens = rs.getString("cardapio_itens");

        List<String> itens = new ArrayList<>();
        if (cardapioItens != null && !cardapioItens.isBlank()) {
            itens.addAll(Arrays.asList(cardapioItens.split("\\r?\\n")));
        }
        Cardapio cardapio = new Cardapio(0, cardapioNome, cardapioDesc, precoPorPessoa, itens);

        String observacoes = rs.getString("observacoes");

        String strDataCriacao = rs.getString("data_criacao");
        LocalDateTime dataCriacao = LocalDateTime.now();
        try {
            dataCriacao = LocalDateTime.parse(strDataCriacao, ISO_DATA_HORA);
        } catch (Exception ignored) {}

        String nomeBuffet = "Festalia Buffet & Eventos";
        try {
            String nb = rs.getString("nome_buffet");
            if (nb != null && !nb.trim().isEmpty()) {
                nomeBuffet = nb.trim();
            }
        } catch (SQLException ignored) {}

        String subtituloBuffet = "Gestão Profissional de Buffets e Eventos";
        try {
            String sb = rs.getString("subtitulo_buffet");
            if (sb != null && !sb.trim().isEmpty()) {
                subtituloBuffet = sb.trim();
            }
        } catch (SQLException ignored) {}

        String caminhoLogoBuffet = null;
        try {
            String cl = rs.getString("caminho_logo_buffet");
            if (cl != null && !cl.trim().isEmpty()) {
                caminhoLogoBuffet = cl.trim();
            }
        } catch (SQLException ignored) {}

        LocalTime horarioInicio = LocalTime.of(18, 0);
        try {
            String hi = rs.getString("horario_inicio");
            if (hi != null && !hi.trim().isEmpty()) {
                horarioInicio = LocalTime.parse(hi.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception ignored) {}

        LocalTime horarioFim = LocalTime.of(22, 0);
        try {
            String hf = rs.getString("horario_fim");
            if (hf != null && !hf.trim().isEmpty()) {
                horarioFim = LocalTime.parse(hf.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception ignored) {}

        String tipoEvento = "Casamento";
        try {
            String te = rs.getString("tipo_evento");
            if (te != null && !te.trim().isEmpty()) {
                tipoEvento = te.trim();
            }
        } catch (SQLException ignored) {}

        boolean conflitoConfirmado = false;
        try {
            conflitoConfirmado = rs.getInt("conflito_confirmado") == 1;
        } catch (SQLException ignored) {}

        return new OrdemServico(id, numeroOS, nomeBuffet, subtituloBuffet, caminhoLogoBuffet, cliente, telefone, dataEvento,
                horarioInicio, horarioFim, tipoEvento, conflitoConfirmado, local,
                qtdConvidados, precoPorPessoa, taxaServico, taxaTransporte, valorTotal, status, cardapio, observacoes, dataCriacao);
    }

    public OrdemServico buscarPorId(int id) {
        String sql = "SELECT * FROM ordens_servico WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairOrdemServico(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar OS por ID: " + e.getMessage());
        }
        return null;
    }

    public OrdemServico buscarPorNumeroOS(int numeroOS) {
        String sql = "SELECT * FROM ordens_servico WHERE numero_os = ? OR id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numeroOS);
            ps.setInt(2, numeroOS);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairOrdemServico(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar OS por número: " + e.getMessage());
        }
        return null;
    }

    /**
     * Busca todas as reservas que colidem em data e faixa horária com a reserva informada.
     * Ignora reservas com status 'Cancelado' e ignora o próprio ID da OS que está sendo editada.
     */
    public List<OrdemServico> buscarConflitos(LocalDate data, LocalTime inicio, LocalTime fim, int idExcluir) {
        List<OrdemServico> conflitos = new ArrayList<>();
        if (data == null || inicio == null || fim == null) {
            return conflitos;
        }
        String sql = "SELECT * FROM ordens_servico WHERE data_evento = ? AND status != 'Cancelado'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data.format(ISO_DATA));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdemServico existente = extrairOrdemServico(rs);
                    if (existente.getId() != idExcluir) {
                        LocalTime exInicio = existente.getHorarioInicio();
                        LocalTime exFim = existente.getHorarioFim();
                        if (inicio.isBefore(exFim) && exInicio.isBefore(fim)) {
                            conflitos.add(existente);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar conflitos de reservas no banco: " + e.getMessage());
        }
        return conflitos;
    }

    /**
     * Retorna reservas cadastradas em um intervalo de datas (para navegação no calendário).
     */
    public List<OrdemServico> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<OrdemServico> lista = new ArrayList<>();
        if (inicio == null || fim == null) {
            return listarTodas();
        }
        String sql = "SELECT * FROM ordens_servico WHERE data_evento >= ? AND data_evento <= ? ORDER BY data_evento ASC, horario_inicio ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio.format(ISO_DATA));
            ps.setString(2, fim.format(ISO_DATA));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairOrdemServico(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar OS por período: " + e.getMessage());
        }
        return lista;
    }
}
