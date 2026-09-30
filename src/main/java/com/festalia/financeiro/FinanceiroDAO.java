package com.festalia.financeiro;

import com.festalia.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object (DAO) para o Módulo de Gestão Financeira Avançada.
 * Gerencia parcelas, pagamentos parciais/totais, auditoria e consultas consolidadas.
 */
public class FinanceiroDAO {

    private static final DateTimeFormatter ISO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Retorna todas as parcelas vinculadas a uma OS, incluindo a lista de pagamentos de cada uma.
     */
    public List<ParcelaOS> listarParcelasPorOS(int osId) {
        List<ParcelaOS> lista = new ArrayList<>();
        String sql = "SELECT * FROM parcelas_os WHERE ordem_servico_id = ? ORDER BY numero_parcela ASC, data_vencimento ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ParcelaOS p = extrairParcela(rs);
                    p.setPagamentos(listarPagamentosPorParcela(conn, p.getId()));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar parcelas da OS #" + osId + ": " + e.getMessage());
        }
        return lista;
    }

    /**
     * Retorna os pagamentos associados a uma parcela específica.
     */
    public List<PagamentoParcela> listarPagamentosPorParcela(int parcelaId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            return listarPagamentosPorParcela(conn, parcelaId);
        } catch (SQLException e) {
            System.err.println("Erro de conexão ao listar pagamentos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<PagamentoParcela> listarPagamentosPorParcela(Connection conn, int parcelaId) {
        List<PagamentoParcela> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagamentos_parcela WHERE parcela_id = ? ORDER BY data_pagamento ASC, id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parcelaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairPagamento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos da parcela #" + parcelaId + ": " + e.getMessage());
        }
        return lista;
    }

    /**
     * Salva ou substitui o cronograma de parcelas para a OS.
     * Caso parcelas anteriores já tenham pagamentos registrados, estas são mantidas.
     */
    public void salvarCronogramaCompleto(int osId, List<ParcelaOS> novasParcelas, String usuario) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Recupera parcelas antigas para verificar se há pagamentos já efetuados
                List<ParcelaOS> antigas = listarParcelasPorOS(osId);
                boolean temPagamentos = antigas.stream().anyMatch(p -> !p.getPagamentos().isEmpty());

                if (!temPagamentos) {
                    // Sem pagamentos: pode limpar o cronograma anterior e recriar
                    try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM parcelas_os WHERE ordem_servico_id = ?")) {
                        psDel.setInt(1, osId);
                        psDel.executeUpdate();
                    }
                }

                // Insere as novas parcelas
                for (ParcelaOS p : novasParcelas) {
                    p.setOrdemServicoId(osId);
                    if (p.getId() > 0 && temPagamentos) {
                        atualizarParcela(conn, p);
                    } else {
                        inserirParcela(conn, p);
                    }
                }

                // Registra histórico
                registrarHistorico(conn, new HistoricoFinanceiro(
                        0, osId, "CRONOGRAMA_CRIADO",
                        "Cronograma financeiro gerado com " + novasParcelas.size() + " parcela(s).",
                        novasParcelas.stream().mapToDouble(ParcelaOS::getValor).sum(),
                        usuario, LocalDateTime.now()
                ));

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void inserirParcela(ParcelaOS p) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            inserirParcela(conn, p);
        }
    }

    private void inserirParcela(Connection conn, ParcelaOS p) throws SQLException {
        String sql = """
            INSERT INTO parcelas_os (
                ordem_servico_id, numero_parcela, descricao, valor, data_vencimento,
                forma_pagamento_sugerida, status, observacoes, data_criacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getOrdemServicoId());
            ps.setInt(2, p.getNumeroParcela());
            ps.setString(3, p.getDescricao());
            ps.setDouble(4, p.getValor());
            ps.setString(5, p.getDataVencimento() != null ? p.getDataVencimento().format(ISO_DATA) : LocalDate.now().format(ISO_DATA));
            ps.setString(6, p.getFormaPagamentoSugerida());
            ps.setString(7, p.getStatusManual());
            ps.setString(8, p.getObservacoes());
            ps.setString(9, p.getDataCriacao() != null ? p.getDataCriacao().format(ISO_DATA_HORA) : LocalDateTime.now().format(ISO_DATA_HORA));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizarParcela(ParcelaOS p) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            atualizarParcela(conn, p);
        }
    }

    private void atualizarParcela(Connection conn, ParcelaOS p) throws SQLException {
        String sql = """
            UPDATE parcelas_os SET
                numero_parcela = ?,
                descricao = ?,
                valor = ?,
                data_vencimento = ?,
                forma_pagamento_sugerida = ?,
                status = ?,
                observacoes = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getNumeroParcela());
            ps.setString(2, p.getDescricao());
            ps.setDouble(3, p.getValor());
            ps.setString(4, p.getDataVencimento() != null ? p.getDataVencimento().format(ISO_DATA) : LocalDate.now().format(ISO_DATA));
            ps.setString(5, p.getFormaPagamentoSugerida());
            ps.setString(6, p.getStatusManual());
            ps.setString(7, p.getObservacoes());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public void salvarParcela(ParcelaOS p) throws SQLException {
        if (p.getId() > 0) {
            atualizarParcela(p);
        } else {
            inserirParcela(p);
        }
    }

    public ParcelaOS buscarParcelaPorId(int id) {
        String sql = "SELECT * FROM parcelas_os WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ParcelaOS p = extrairParcela(rs);
                    p.setPagamentos(listarPagamentosPorParcela(conn, p.getId()));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar parcela #" + id + ": " + e.getMessage());
        }
        return null;
    }

    public void excluirParcela(int parcelaId) throws SQLException {
        List<PagamentoParcela> pags = listarPagamentosPorParcela(parcelaId);
        if (!pags.isEmpty()) {
            throw new SQLException("Esta parcela possui pagamentos vinculados e não pode ser excluída sem estornar os pagamentos.");
        }

        String sql = "DELETE FROM parcelas_os WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parcelaId);
            ps.executeUpdate();
        }
    }

    /**
     * Registra um pagamento (total ou parcial) para uma parcela de uma OS.
     */
    public void registrarPagamento(PagamentoParcela pag) throws SQLException {
        String sql = """
            INSERT INTO pagamentos_parcela (
                parcela_id, ordem_servico_id, valor_pago, data_pagamento, forma_pagamento,
                identificacao_transacao, observacoes, usuario_responsavel, data_registro
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, pag.getParcelaId());
                    ps.setInt(2, pag.getOrdemServicoId());
                    ps.setDouble(3, pag.getValorPago());
                    ps.setString(4, pag.getDataPagamento() != null ? pag.getDataPagamento().format(ISO_DATA) : LocalDate.now().format(ISO_DATA));
                    ps.setString(5, pag.getFormaPagamento());
                    ps.setString(6, pag.getIdentificacaoTransacao());
                    ps.setString(7, pag.getObservacoes());
                    ps.setString(8, pag.getUsuarioResponsavel());
                    ps.setString(9, pag.getDataRegistro() != null ? pag.getDataRegistro().format(ISO_DATA_HORA) : LocalDateTime.now().format(ISO_DATA_HORA));

                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            pag.setId(rs.getInt(1));
                        }
                    }
                }

                // Registra histórico
                registrarHistorico(conn, new HistoricoFinanceiro(
                        0, pag.getOrdemServicoId(), "PAGAMENTO_REGISTRADO",
                        String.format("Pagamento de R$ %.2f registrado via %s para a parcela #%d.",
                                pag.getValorPago(), pag.getFormaPagamento(), pag.getParcelaId()),
                        pag.getValorPago(), pag.getUsuarioResponsavel(), LocalDateTime.now()
                ));

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Estorna/exclui um pagamento e recalcula a situação.
     */
    public void excluirPagamento(int pagamentoId, String usuario) throws SQLException {
        String sqlSelect = "SELECT * FROM pagamentos_parcela WHERE id = ?";
        String sqlDelete = "DELETE FROM pagamentos_parcela WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PagamentoParcela pag = null;
                try (PreparedStatement psSel = conn.prepareStatement(sqlSelect)) {
                    psSel.setInt(1, pagamentoId);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (rs.next()) {
                            pag = extrairPagamento(rs);
                        }
                    }
                }

                if (pag == null) {
                    throw new SQLException("Pagamento não encontrado.");
                }

                try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                    psDel.setInt(1, pagamentoId);
                    psDel.executeUpdate();
                }

                // Registra histórico
                registrarHistorico(conn, new HistoricoFinanceiro(
                        0, pag.getOrdemServicoId(), "PAGAMENTO_EXCLUIDO",
                        String.format("Estorno/exclusão de pagamento no valor de R$ %.2f da parcela #%d.",
                                pag.getValorPago(), pag.getParcelaId()),
                        -pag.getValorPago(), usuario, LocalDateTime.now()
                ));

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void registrarHistorico(HistoricoFinanceiro hist) {
        try (Connection conn = DatabaseManager.getConnection()) {
            registrarHistorico(conn, hist);
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico financeiro: " + e.getMessage());
        }
    }

    private void registrarHistorico(Connection conn, HistoricoFinanceiro hist) throws SQLException {
        String sql = """
            INSERT INTO historico_financeiro_os (
                ordem_servico_id, tipo_operacao, descricao, valor, usuario, data_hora
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hist.getOrdemServicoId());
            ps.setString(2, hist.getTipoOperacao());
            ps.setString(3, hist.getDescricao());
            ps.setDouble(4, hist.getValor());
            ps.setString(5, hist.getUsuario());
            ps.setString(6, hist.getDataHora() != null ? hist.getDataHora().format(ISO_DATA_HORA) : LocalDateTime.now().format(ISO_DATA_HORA));
            ps.executeUpdate();
        }
    }

    public List<HistoricoFinanceiro> listarHistoricoPorOS(int osId) {
        List<HistoricoFinanceiro> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_financeiro_os WHERE ordem_servico_id = ? ORDER BY data_hora DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HistoricoFinanceiro(
                            rs.getInt("id"),
                            rs.getInt("ordem_servico_id"),
                            rs.getString("tipo_operacao"),
                            rs.getString("descricao"),
                            rs.getDouble("valor"),
                            rs.getString("usuario"),
                            LocalDateTime.parse(rs.getString("data_hora"), ISO_DATA_HORA)
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar histórico financeiro: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Calcula o resumo financeiro consolidado da OS.
     */
    public ResumoFinanceiroOS obterResumoFinanceiro(int osId, double valorTotalOS) {
        List<ParcelaOS> parcelas = listarParcelasPorOS(osId);

        double totalPago = 0.0;
        double totalPendente = 0.0;
        double totalVencido = 0.0;
        int vencidas = 0;
        int pagas = 0;

        for (ParcelaOS p : parcelas) {
            double pago = p.getValorPago();
            double saldo = p.getSaldoRestante();

            totalPago += pago;
            totalPendente += saldo;

            if (p.getStatusDinamico() == ParcelaOS.StatusParcela.VENCIDO) {
                totalVencido += saldo;
                vencidas++;
            } else if (p.getStatusDinamico() == ParcelaOS.StatusParcela.PAGO) {
                pagas++;
            }
        }

        // Se o cronograma cobre menos que o total da OS, o restante também fica como pendente
        double totalCronograma = parcelas.stream().mapToDouble(ParcelaOS::getValor).sum();
        if (totalCronograma < valorTotalOS && !parcelas.isEmpty()) {
            totalPendente += (valorTotalOS - totalCronograma);
        } else if (parcelas.isEmpty()) {
            totalPendente = valorTotalOS;
        }

        return new ResumoFinanceiroOS(valorTotalOS, totalPago, totalPendente, totalVencido,
                parcelas.size(), vencidas, pagas);
    }

    public ResumoFinanceiroOS obterResumoOS(int osId, double valorTotalOS) {
        return obterResumoFinanceiro(osId, valorTotalOS);
    }

    public static class TotaisGeraisInadimplencia {
        public final int parcelasVencidas;
        public final int parcelasAVencer;
        public final int parcelasPagas;
        public final double valorTotalVencido;

        public TotaisGeraisInadimplencia(int parcelasVencidas, int parcelasAVencer, int parcelasPagas, double valorTotalVencido) {
            this.parcelasVencidas = parcelasVencidas;
            this.parcelasAVencer = parcelasAVencer;
            this.parcelasPagas = parcelasPagas;
            this.valorTotalVencido = valorTotalVencido;
        }
    }

    /**
     * Retorna estatísticas globais de inadimplência em todas as OS do sistema.
     */
    public TotaisGeraisInadimplencia obterTotaisGeraisInadimplencia() {
        int countVencidas = 0;
        int countAVencer = 0;
        int countPagas = 0;
        double valorTotalVencido = 0.0;

        String sql = """
            SELECT p.*, COALESCE(SUM(pg.valor_pago), 0) AS total_pago
            FROM parcelas_os p
            LEFT JOIN pagamentos_parcela pg ON p.id = pg.parcela_id
            GROUP BY p.id
        """;

        LocalDate hoje = LocalDate.now();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                double valor = rs.getDouble("valor");
                double pago = rs.getDouble("total_pago");
                double saldo = Math.max(0.0, valor - pago);
                String dtVencStr = rs.getString("data_vencimento");
                LocalDate dtVenc = dtVencStr != null ? LocalDate.parse(dtVencStr, ISO_DATA) : hoje;
                String statusManual = rs.getString("status");

                if ("CANCELADO".equalsIgnoreCase(statusManual)) {
                    continue;
                }

                if (saldo <= 0.001) {
                    countPagas++;
                } else if (dtVenc.isBefore(hoje)) {
                    countVencidas++;
                    valorTotalVencido += saldo;
                } else {
                    countAVencer++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao calcular totais gerais de inadimplência: " + e.getMessage());
        }

        return new TotaisGeraisInadimplencia(countVencidas, countAVencer, countPagas, valorTotalVencido);
    }

    public Map<String, Object> obterTotaisGeraisInadimplenciaMap() {
        TotaisGeraisInadimplencia t = obterTotaisGeraisInadimplencia();
        Map<String, Object> stats = new HashMap<>();
        stats.put("vencidas", t.parcelasVencidas);
        stats.put("a_vencer", t.parcelasAVencer);
        stats.put("pagas", t.parcelasPagas);
        stats.put("total_vencido_reais", t.valorTotalVencido);
        return stats;
    }

    private ParcelaOS extrairParcela(ResultSet rs) throws SQLException {
        LocalDate dtVenc = null;
        String sVenc = rs.getString("data_vencimento");
        if (sVenc != null && !sVenc.isBlank()) {
            dtVenc = LocalDate.parse(sVenc, ISO_DATA);
        }

        LocalDateTime dtCriacao = null;
        String sCriacao = rs.getString("data_criacao");
        if (sCriacao != null && !sCriacao.isBlank()) {
            try {
                dtCriacao = LocalDateTime.parse(sCriacao, ISO_DATA_HORA);
            } catch (Exception e) {
                dtCriacao = LocalDateTime.now();
            }
        }

        return new ParcelaOS(
                rs.getInt("id"),
                rs.getInt("ordem_servico_id"),
                rs.getInt("numero_parcela"),
                rs.getString("descricao"),
                rs.getDouble("valor"),
                dtVenc,
                rs.getString("forma_pagamento_sugerida"),
                rs.getString("status"),
                rs.getString("observacoes"),
                dtCriacao
        );
    }

    private PagamentoParcela extrairPagamento(ResultSet rs) throws SQLException {
        LocalDate dtPag = null;
        String sPag = rs.getString("data_pagamento");
        if (sPag != null && !sPag.isBlank()) {
            dtPag = LocalDate.parse(sPag, ISO_DATA);
        }

        LocalDateTime dtReg = null;
        String sReg = rs.getString("data_registro");
        if (sReg != null && !sReg.isBlank()) {
            try {
                dtReg = LocalDateTime.parse(sReg, ISO_DATA_HORA);
            } catch (Exception e) {
                dtReg = LocalDateTime.now();
            }
        }

        return new PagamentoParcela(
                rs.getInt("id"),
                rs.getInt("parcela_id"),
                rs.getInt("ordem_servico_id"),
                rs.getDouble("valor_pago"),
                dtPag,
                rs.getString("forma_pagamento"),
                rs.getString("identificacao_transacao"),
                rs.getString("observacoes"),
                rs.getString("usuario_responsavel"),
                dtReg
        );
    }
}
