package com.festalia.contrato;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Trilha de auditoria das movimentações e emissões de contratos.
 */
public class HistoricoContrato {

    private int id;
    private int contratoId;
    private String tipoOperacao;
    private String descricao;
    private String versao;
    private String usuario;
    private LocalDateTime dataHora;

    public HistoricoContrato() {
        this.dataHora = LocalDateTime.now();
        this.usuario = "Operador";
    }

    public HistoricoContrato(int id, int contratoId, String tipoOperacao, String descricao,
                             String versao, String usuario, LocalDateTime dataHora) {
        this.id = id;
        this.contratoId = contratoId;
        this.tipoOperacao = tipoOperacao;
        this.descricao = descricao;
        this.versao = versao;
        this.usuario = usuario != null ? usuario : "Operador";
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContratoId() { return contratoId; }
    public void setContratoId(int contratoId) { this.contratoId = contratoId; }

    public String getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getDataHoraFormatada() {
        return dataHora != null ? dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—";
    }

    public String getAcao() { return tipoOperacao; }
    public String getDetalhes() { return descricao; }
}
