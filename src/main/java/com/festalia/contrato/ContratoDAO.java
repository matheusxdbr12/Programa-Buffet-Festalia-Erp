package com.festalia.contrato;

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
 * Data Access Object (DAO) para o Módulo de Contratos de Prestação de Serviços.
 * Gerencia persistência de contratos, controle rigoroso de versões e auditoria.
 */
public class ContratoDAO {

    private static final DateTimeFormatter ISO_DATA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void salvar(Contrato c, String usuario) throws SQLException {
        if (c.getId() > 0) {
            atualizar(c, usuario);
        } else {
            inserir(c, usuario);
        }
    }

    private void inserir(Contrato c, String usuario) throws SQLException {
        String sql = """
            INSERT INTO contratos (
                ordem_servico_id, numero_contrato, modelo_tipo, versao_atual, status,
                contratante_nome, contratante_cpf_cnpj, contratante_rg, contratante_endereco,
                contratante_telefone, contratante_email, empresa_razao_social, empresa_nome_fantasia,
                empresa_cnpj, empresa_endereco, empresa_telefone, empresa_email,
                empresa_representante_nome, empresa_representante_cpf, empresa_cidade_foro,
                valor_total, data_evento, data_emissao, data_assinatura, data_cancelamento,
                observacoes_especiais, caminho_pdf, data_criacao, data_atualizacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        LocalDateTime agora = LocalDateTime.now();
        c.setDataCriacao(agora);
        c.setDataAtualizacao(agora);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, c);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }

            registrarAuditoria(conn, new HistoricoContrato(
                    0, c.getId(), "CONTRATO_CRIADO",
                    "Contrato " + c.getNumeroContrato() + " criado no sistema para OS #" + c.getOrdemServicoId() + ".",
                    c.getVersaoAtual(), usuario, agora
            ));
        }
    }

    private void atualizar(Contrato c, String usuario) throws SQLException {
        String sql = """
            UPDATE contratos SET
                modelo_tipo = ?, versao_atual = ?, status = ?,
                contratante_nome = ?, contratante_cpf_cnpj = ?, contratante_rg = ?, contratante_endereco = ?,
                contratante_telefone = ?, contratante_email = ?, empresa_razao_social = ?, empresa_nome_fantasia = ?,
                empresa_cnpj = ?, empresa_endereco = ?, empresa_telefone = ?, empresa_email = ?,
                empresa_representante_nome = ?, empresa_representante_cpf = ?, empresa_cidade_foro = ?,
                valor_total = ?, data_evento = ?, data_emissao = ?, data_assinatura = ?, data_cancelamento = ?,
                observacoes_especiais = ?, caminho_pdf = ?, data_atualizacao = ?
            WHERE id = ?
        """;

        LocalDateTime agora = LocalDateTime.now();
        c.setDataAtualizacao(agora);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getModeloTipo() != null ? c.getModeloTipo().name() : "BUFFET");
            ps.setString(2, c.getVersaoAtual());
            ps.setString(3, c.getStatus() != null ? c.getStatus().name() : "RASCUNHO");
            ps.setString(4, c.getContratanteNome());
            ps.setString(5, c.getContratanteCpfCnpj());
            ps.setString(6, c.getContratanteRg());
            ps.setString(7, c.getContratanteEndereco());
            ps.setString(8, c.getContratanteTelefone());
            ps.setString(9, c.getContratanteEmail());
            ps.setString(10, c.getEmpresaRazaoSocial());
            ps.setString(11, c.getEmpresaNomeFantasia());
            ps.setString(12, c.getEmpresaCnpj());
            ps.setString(13, c.getEmpresaEndereco());
            ps.setString(14, c.getEmpresaTelefone());
            ps.setString(15, c.getEmpresaEmail());
            ps.setString(16, c.getEmpresaRepresentanteNome());
            ps.setString(17, c.getEmpresaRepresentanteCpf());
            ps.setString(18, c.getEmpresaCidadeForo());
            ps.setDouble(19, c.getValorTotal());
            ps.setString(20, c.getDataEvento() != null ? c.getDataEvento().format(ISO_DATA) : null);
            ps.setString(21, c.getDataEmissao() != null ? c.getDataEmissao().format(ISO_DATA) : null);
            ps.setString(22, c.getDataAssinatura() != null ? c.getDataAssinatura().format(ISO_DATA) : null);
            ps.setString(23, c.getDataCancelamento() != null ? c.getDataCancelamento().format(ISO_DATA) : null);
            ps.setString(24, c.getObservacoesEspeciais());
            ps.setString(25, c.getCaminhoPdf());
            ps.setString(26, agora.format(ISO_DATA_HORA));
            ps.setInt(27, c.getId());

            ps.executeUpdate();

            registrarAuditoria(conn, new HistoricoContrato(
                    0, c.getId(), "DADOS_ATUALIZADOS",
                    "Dados cadastrais ou parâmetros da minuta foram atualizados.",
                    c.getVersaoAtual(), usuario, agora
            ));
        }
    }

    private void preencherStatement(PreparedStatement ps, Contrato c) throws SQLException {
        ps.setInt(1, c.getOrdemServicoId());
        ps.setString(2, c.getNumeroContrato());
        ps.setString(3, c.getModeloTipo() != null ? c.getModeloTipo().name() : "BUFFET");
        ps.setString(4, c.getVersaoAtual());
        ps.setString(5, c.getStatus() != null ? c.getStatus().name() : "RASCUNHO");
        ps.setString(6, c.getContratanteNome());
        ps.setString(7, c.getContratanteCpfCnpj());
        ps.setString(8, c.getContratanteRg());
        ps.setString(9, c.getContratanteEndereco());
        ps.setString(10, c.getContratanteTelefone());
        ps.setString(11, c.getContratanteEmail());
        ps.setString(12, c.getEmpresaRazaoSocial());
        ps.setString(13, c.getEmpresaNomeFantasia());
        ps.setString(14, c.getEmpresaCnpj());
        ps.setString(15, c.getEmpresaEndereco());
        ps.setString(16, c.getEmpresaTelefone());
        ps.setString(17, c.getEmpresaEmail());
        ps.setString(18, c.getEmpresaRepresentanteNome());
        ps.setString(19, c.getEmpresaRepresentanteCpf());
        ps.setString(20, c.getEmpresaCidadeForo());
        ps.setDouble(21, c.getValorTotal());
        ps.setString(22, c.getDataEvento() != null ? c.getDataEvento().format(ISO_DATA) : null);
        ps.setString(23, c.getDataEmissao() != null ? c.getDataEmissao().format(ISO_DATA) : null);
        ps.setString(24, c.getDataAssinatura() != null ? c.getDataAssinatura().format(ISO_DATA) : null);
        ps.setString(25, c.getDataCancelamento() != null ? c.getDataCancelamento().format(ISO_DATA) : null);
        ps.setString(26, c.getObservacoesEspeciais());
        ps.setString(27, c.getCaminhoPdf());
        ps.setString(28, c.getDataCriacao().format(ISO_DATA_HORA));
        ps.setString(29, c.getDataAtualizacao().format(ISO_DATA_HORA));
    }

    /**
     * Registra uma nova versão formal do contrato, mantendo as versões anteriores imutáveis.
     */
    public void registrarNovaVersao(int contratoId, VersaoContrato versao, String usuario) throws SQLException {
        String sqlMarcaSubstituida = """
            UPDATE contratos_versoes SET status = 'SUBSTITUIDO'
            WHERE contrato_id = ? AND status IN ('GERADO', 'AGUARDANDO_ASSINATURA', 'RASCUNHO')
        """;

        String sqlInsereVersao = """
            INSERT INTO contratos_versoes (
                contrato_id, versao, status, conteudo_minuta, parametros_config,
                motivo_alteracao, usuario_responsavel, data_registro, caminho_pdf
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String sqlAtualizaContrato = """
            UPDATE contratos SET
                versao_atual = ?,
                status = ?,
                caminho_pdf = ?,
                data_atualizacao = ?
            WHERE id = ?
        """;

        LocalDateTime agora = LocalDateTime.now();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Marca versões ativas anteriores como substituídas
                try (PreparedStatement psSub = conn.prepareStatement(sqlMarcaSubstituida)) {
                    psSub.setInt(1, contratoId);
                    psSub.executeUpdate();
                }

                // 2. Insere a nova versão
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsereVersao, Statement.RETURN_GENERATED_KEYS)) {
                    psIns.setInt(1, contratoId);
                    psIns.setString(2, versao.getVersao());
                    psIns.setString(3, versao.getStatus() != null ? versao.getStatus().name() : "GERADO");
                    psIns.setString(4, versao.getConteudoMinuta());
                    psIns.setString(5, versao.getParametrosConfig());
                    psIns.setString(6, versao.getMotivoAlteracao());
                    psIns.setString(7, usuario != null ? usuario : "Operador");
                    psIns.setString(8, agora.format(ISO_DATA_HORA));
                    psIns.setString(9, versao.getCaminhoPdf());
                    psIns.executeUpdate();

                    try (ResultSet rs = psIns.getGeneratedKeys()) {
                        if (rs.next()) {
                            versao.setId(rs.getInt(1));
                        }
                    }
                }

                // 3. Atualiza os dados de cabeçalho do contrato mestre
                try (PreparedStatement psUpd = conn.prepareStatement(sqlAtualizaContrato)) {
                    psUpd.setString(1, versao.getVersao());
                    psUpd.setString(2, versao.getStatus() != null ? versao.getStatus().name() : "GERADO");
                    psUpd.setString(3, versao.getCaminhoPdf());
                    psUpd.setString(4, agora.format(ISO_DATA_HORA));
                    psUpd.setInt(5, contratoId);
                    psUpd.executeUpdate();
                }

                // 4. Grava trilha de auditoria
                registrarAuditoria(conn, new HistoricoContrato(
                        0, contratoId, "VERSAO_EMITIDA",
                        "Versão " + versao.getVersao() + " emitida formalmente. Motivo: " + versao.getMotivoAlteracao(),
                        versao.getVersao(), usuario, agora
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

    public void atualizarStatusNoBanco(int contratoId, StatusContrato novoStatus, String motivo, String usuario) throws SQLException {
        String sql = "UPDATE contratos SET status = ?, data_atualizacao = ? WHERE id = ?";
        LocalDateTime agora = LocalDateTime.now();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, novoStatus.name());
                    ps.setString(2, agora.format(ISO_DATA_HORA));
                    ps.setInt(3, contratoId);
                    ps.executeUpdate();
                }

                if (novoStatus == StatusContrato.ASSINADO) {
                    try (PreparedStatement psAss = conn.prepareStatement("UPDATE contratos SET data_assinatura = ? WHERE id = ?")) {
                        psAss.setString(1, LocalDate.now().format(ISO_DATA));
                        psAss.setInt(2, contratoId);
                        psAss.executeUpdate();
                    }
                } else if (novoStatus == StatusContrato.CANCELADO) {
                    try (PreparedStatement psCanc = conn.prepareStatement("UPDATE contratos SET data_cancelamento = ? WHERE id = ?")) {
                        psCanc.setString(1, LocalDate.now().format(ISO_DATA));
                        psCanc.setInt(2, contratoId);
                        psCanc.executeUpdate();
                    }
                }

                // Atualiza também a última versão cadastrada
                try (PreparedStatement psVer = conn.prepareStatement(
                        "UPDATE contratos_versoes SET status = ? WHERE id = (SELECT MAX(id) FROM contratos_versoes WHERE contrato_id = ?)")) {
                    psVer.setString(1, novoStatus.name());
                    psVer.setInt(2, contratoId);
                    psVer.executeUpdate();
                }

                registrarAuditoria(conn, new HistoricoContrato(
                        0, contratoId, "STATUS_ALTERADO",
                        "Status do contrato alterado para " + novoStatus.getDescricao() + (motivo != null && !motivo.isBlank() ? " (" + motivo + ")" : ""),
                        null, usuario, agora
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

    public Contrato buscarPorId(int id) {
        String sql = """
            SELECT c.*, o.numero_os, o.tipo_evento AS os_tipo_evento, o.data_evento AS os_data_evento
            FROM contratos c
            LEFT JOIN ordens_servico o ON c.ordem_servico_id = o.id
            WHERE c.id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = extrairContrato(rs);
                    c.setVersoes(listarVersoes(conn, c.getId()));
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar contrato #" + id + ": " + e.getMessage());
        }
        return null;
    }

    public Contrato buscarPorOrdemServicoId(int osId) {
        String sql = """
            SELECT c.*, o.numero_os, o.tipo_evento AS os_tipo_evento, o.data_evento AS os_data_evento
            FROM contratos c
            LEFT JOIN ordens_servico o ON c.ordem_servico_id = o.id
            WHERE c.ordem_servico_id = ?
            ORDER BY c.id DESC LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = extrairContrato(rs);
                    c.setVersoes(listarVersoes(conn, c.getId()));
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar contrato por OS #" + osId + ": " + e.getMessage());
        }
        return null;
    }

    public List<Contrato> listarTodos() {
        return buscarPorTermo("");
    }

    public List<Contrato> buscarPorTermo(String termo) {
        List<Contrato> lista = new ArrayList<>();
        String sql = """
            SELECT c.*, o.numero_os, o.tipo_evento AS os_tipo_evento, o.data_evento AS os_data_evento
            FROM contratos c
            LEFT JOIN ordens_servico o ON c.ordem_servico_id = o.id
            WHERE (? = '' OR c.numero_contrato LIKE ? OR c.contratante_nome LIKE ? OR c.contratante_cpf_cnpj LIKE ?
                   OR CAST(o.numero_os AS TEXT) LIKE ? OR c.status LIKE ?)
            ORDER BY c.data_atualizacao DESC, c.id DESC
        """;

        String p = "%" + (termo != null ? termo.trim() : "") + "%";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, termo != null ? termo.trim() : "");
            ps.setString(2, p);
            ps.setString(3, p);
            ps.setString(4, p);
            ps.setString(5, p);
            ps.setString(6, p);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairContrato(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar/buscar contratos: " + e.getMessage());
        }
        return lista;
    }

    public List<VersaoContrato> listarVersoesDoContrato(int contratoId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            return listarVersoes(conn, contratoId);
        } catch (SQLException e) {
            System.err.println("Erro ao listar versões: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<VersaoContrato> listarVersoes(Connection conn, int contratoId) {
        List<VersaoContrato> lista = new ArrayList<>();
        String sql = "SELECT * FROM contratos_versoes WHERE contrato_id = ? ORDER BY id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, contratoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new VersaoContrato(
                            rs.getInt("id"),
                            rs.getInt("contrato_id"),
                            rs.getString("versao"),
                            StatusContrato.valueOf(rs.getString("status")),
                            rs.getString("conteudo_minuta"),
                            rs.getString("parametros_config"),
                            rs.getString("motivo_alteracao"),
                            rs.getString("usuario_responsavel"),
                            LocalDateTime.parse(rs.getString("data_registro"), ISO_DATA_HORA),
                            rs.getString("caminho_pdf")
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair versões do contrato #" + contratoId + ": " + e.getMessage());
        }
        return lista;
    }

    public List<HistoricoContrato> listarAuditoriaDoContrato(int contratoId) {
        List<HistoricoContrato> lista = new ArrayList<>();
        String sql = "SELECT * FROM contratos_auditoria WHERE contrato_id = ? ORDER BY data_hora DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contratoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HistoricoContrato(
                            rs.getInt("id"),
                            rs.getInt("contrato_id"),
                            rs.getString("tipo_operacao"),
                            rs.getString("descricao"),
                            rs.getString("versao"),
                            rs.getString("usuario"),
                            LocalDateTime.parse(rs.getString("data_hora"), ISO_DATA_HORA)
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar auditoria do contrato #" + contratoId + ": " + e.getMessage());
        }
        return lista;
    }

    public void registrarAuditoria(HistoricoContrato hist) {
        try (Connection conn = DatabaseManager.getConnection()) {
            registrarAuditoria(conn, hist);
        } catch (SQLException e) {
            System.err.println("Erro ao registrar auditoria de contrato: " + e.getMessage());
        }
    }

    private void registrarAuditoria(Connection conn, HistoricoContrato hist) throws SQLException {
        String sql = """
            INSERT INTO contratos_auditoria (contrato_id, tipo_operacao, descricao, versao, usuario, data_hora)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hist.getContratoId());
            ps.setString(2, hist.getTipoOperacao());
            ps.setString(3, hist.getDescricao());
            ps.setString(4, hist.getVersao());
            ps.setString(5, hist.getUsuario());
            ps.setString(6, hist.getDataHora() != null ? hist.getDataHora().format(ISO_DATA_HORA) : LocalDateTime.now().format(ISO_DATA_HORA));
            ps.executeUpdate();
        }
    }

    /**
     * Retorna contadores para os 5 cards da guia de Contratos:
     * - "gerados", "pendentes", "assinados", "aguardando", "cancelados"
     */
    public Map<String, Integer> obterContadoresCards() {
        Map<String, Integer> map = new HashMap<>();
        map.put("gerados", 0);
        map.put("pendentes", 0);
        map.put("assinados", 0);
        map.put("aguardando", 0);
        map.put("cancelados", 0);

        String sql = "SELECT status, COUNT(*) AS total FROM contratos GROUP BY status";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int totalGeradosGerais = 0;
            while (rs.next()) {
                String stStr = rs.getString("status");
                int tot = rs.getInt("total");
                StatusContrato st;
                try {
                    st = StatusContrato.valueOf(stStr);
                } catch (Exception e) {
                    st = StatusContrato.RASCUNHO;
                }

                if (st != StatusContrato.RASCUNHO && st != StatusContrato.CANCELADO) {
                    totalGeradosGerais += tot;
                }

                switch (st) {
                    case RASCUNHO -> map.put("pendentes", map.get("pendentes") + tot);
                    case ASSINADO -> map.put("assinados", map.get("assinados") + tot);
                    case AGUARDANDO_ASSINATURA, GERADO -> map.put("aguardando", map.get("aguardando") + tot);
                    case CANCELADO -> map.put("cancelados", map.get("cancelados") + tot);
                    default -> {}
                }
            }
            map.put("gerados", totalGeradosGerais);
        } catch (SQLException e) {
            System.err.println("Erro ao obter contadores de contratos: " + e.getMessage());
        }
        return map;
    }

    private Contrato extrairContrato(ResultSet rs) throws SQLException {
        LocalDate dtEv = rs.getString("data_evento") != null ? LocalDate.parse(rs.getString("data_evento"), ISO_DATA) : null;
        LocalDate dtEm = rs.getString("data_emissao") != null ? LocalDate.parse(rs.getString("data_emissao"), ISO_DATA) : null;
        LocalDate dtAss = rs.getString("data_assinatura") != null ? LocalDate.parse(rs.getString("data_assinatura"), ISO_DATA) : null;
        LocalDate dtCanc = rs.getString("data_cancelamento") != null ? LocalDate.parse(rs.getString("data_cancelamento"), ISO_DATA) : null;

        LocalDateTime dtCriac = null;
        try {
            dtCriac = LocalDateTime.parse(rs.getString("data_criacao"), ISO_DATA_HORA);
        } catch (Exception e) {
            dtCriac = LocalDateTime.now();
        }

        LocalDateTime dtAtual = null;
        try {
            dtAtual = LocalDateTime.parse(rs.getString("data_atualizacao"), ISO_DATA_HORA);
        } catch (Exception e) {
            dtAtual = LocalDateTime.now();
        }

        ModeloContrato mod;
        try {
            mod = ModeloContrato.valueOf(rs.getString("modelo_tipo"));
        } catch (Exception e) {
            mod = ModeloContrato.BUFFET;
        }

        StatusContrato st;
        try {
            st = StatusContrato.valueOf(rs.getString("status"));
        } catch (Exception e) {
            st = StatusContrato.RASCUNHO;
        }

        Contrato c = new Contrato(
                rs.getInt("id"),
                rs.getInt("ordem_servico_id"),
                rs.getString("numero_contrato"),
                mod,
                rs.getString("versao_atual"),
                st,
                rs.getString("contratante_nome"),
                rs.getString("contratante_cpf_cnpj"),
                rs.getString("contratante_rg"),
                rs.getString("contratante_endereco"),
                rs.getString("contratante_telefone"),
                rs.getString("contratante_email"),
                rs.getString("empresa_razao_social"),
                rs.getString("empresa_nome_fantasia"),
                rs.getString("empresa_cnpj"),
                rs.getString("empresa_endereco"),
                rs.getString("empresa_telefone"),
                rs.getString("empresa_email"),
                rs.getString("empresa_representante_nome"),
                rs.getString("empresa_representante_cpf"),
                rs.getString("empresa_cidade_foro"),
                rs.getDouble("valor_total"),
                dtEv,
                dtEm,
                dtAss,
                dtCanc,
                rs.getString("observacoes_especiais"),
                rs.getString("caminho_pdf"),
                dtCriac,
                dtAtual
        );

        try {
            c.setNumeroOS(rs.getInt("numero_os"));
            if (c.getTipoEvento() == null || c.getTipoEvento().isBlank()) {
                c.setTipoEvento(rs.getString("os_tipo_evento"));
            }
        } catch (SQLException ignored) {}

        return c;
    }

    // =========================================================================
    // MÉTODOS ESTÁTICOS DE CONVENIÊNCIA / FACHADA
    // =========================================================================
    private static final ContratoDAO INSTANCIA = new ContratoDAO();

    public static ContratoDAO getInstancia() {
        return INSTANCIA;
    }

    public static Contrato obterPorOsId(int osId) {
        return INSTANCIA.buscarPorOrdemServicoId(osId);
    }

    public static boolean salvarOuAtualizarContrato(Contrato c, String motivo, String usuario) {
        try {
            INSTANCIA.salvar(c, usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar contrato: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registrarNovaVersao(int contratoId, String versao, String textoMinuta, String pdfPath, String motivo, String usuario) {
        try {
            VersaoContrato vc = new VersaoContrato(0, contratoId, versao, StatusContrato.GERADO, textoMinuta, "", motivo, usuario, LocalDateTime.now(), pdfPath);
            INSTANCIA.registrarNovaVersao(contratoId, vc, usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar nova versão: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean atualizarStatus(int contratoId, StatusContrato novoStatus, String motivo, String usuario) {
        try {
            INSTANCIA.atualizarStatusNoBanco(contratoId, novoStatus, motivo, usuario);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, Integer> obterContadoresCardsEstatico() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("GERADO", 0);
        counts.put("RASCUNHO", 0);
        counts.put("AGUARDANDO_ASSINATURA", 0);
        counts.put("ASSINADO", 0);
        counts.put("CANCELADO", 0);

        String sql = "SELECT status, count(*) AS qtd FROM contratos GROUP BY status";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String st = rs.getString("status");
                int q = rs.getInt("qtd");
                counts.put(st, q);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter contadores de cards: " + e.getMessage());
        }
        return counts;
    }

    public static List<Contrato> listarTodos(String busca, StatusContrato status) {
        List<Contrato> base = INSTANCIA.buscarPorTermo(busca != null ? busca : "");
        if (status == null) {
            return base;
        }
        List<Contrato> filtrados = new ArrayList<>();
        for (Contrato c : base) {
            if (c.getStatus() == status) {
                filtrados.add(c);
            }
        }
        return filtrados;
    }

    public static List<VersaoContrato> listarVersoes(int contratoId) {
        return INSTANCIA.listarVersoesDoContrato(contratoId);
    }

    public static List<HistoricoContrato> listarAuditoria(int contratoId) {
        return INSTANCIA.listarAuditoriaDoContrato(contratoId);
    }
}
