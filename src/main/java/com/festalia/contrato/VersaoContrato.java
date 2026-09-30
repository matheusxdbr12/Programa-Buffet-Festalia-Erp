package com.festalia.contrato;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Snapshot histórico imutável de uma versão específica do contrato.
 */
public class VersaoContrato {

    private int id;
    private int contratoId;
    private String versao;
    private StatusContrato status;
    private String conteudoMinuta;
    private String parametrosConfig;
    private String motivoAlteracao;
    private String usuarioResponsavel;
    private LocalDateTime dataRegistro;
    private String caminhoPdf;

    public VersaoContrato() {
        this.versao = "1.0";
        this.status = StatusContrato.GERADO;
        this.dataRegistro = LocalDateTime.now();
        this.usuarioResponsavel = "Operador";
    }

    public VersaoContrato(int id, int contratoId, String versao, StatusContrato status,
                          String conteudoMinuta, String parametrosConfig, String motivoAlteracao,
                          String usuarioResponsavel, LocalDateTime dataRegistro, String caminhoPdf) {
        this.id = id;
        this.contratoId = contratoId;
        this.versao = versao != null ? versao : "1.0";
        this.status = status != null ? status : StatusContrato.GERADO;
        this.conteudoMinuta = conteudoMinuta != null ? conteudoMinuta : "";
        this.parametrosConfig = parametrosConfig != null ? parametrosConfig : "";
        this.motivoAlteracao = motivoAlteracao != null ? motivoAlteracao : "Emissão inicial da minuta";
        this.usuarioResponsavel = usuarioResponsavel != null ? usuarioResponsavel : "Operador";
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.caminhoPdf = caminhoPdf;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContratoId() { return contratoId; }
    public void setContratoId(int contratoId) { this.contratoId = contratoId; }

    public String getVersao() { return versao; }
    public void setVersao(String versao) { this.versao = versao; }

    public StatusContrato getStatus() { return status; }
    public void setStatus(StatusContrato status) { this.status = status; }

    public String getConteudoMinuta() { return conteudoMinuta; }
    public void setConteudoMinuta(String conteudoMinuta) { this.conteudoMinuta = conteudoMinuta; }

    public String getParametrosConfig() { return parametrosConfig; }
    public void setParametrosConfig(String parametrosConfig) { this.parametrosConfig = parametrosConfig; }

    public String getMotivoAlteracao() { return motivoAlteracao; }
    public void setMotivoAlteracao(String motivoAlteracao) { this.motivoAlteracao = motivoAlteracao; }

    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }

    public String getCaminhoPdf() { return caminhoPdf; }
    public void setCaminhoPdf(String caminhoPdf) { this.caminhoPdf = caminhoPdf; }

    public String getDataRegistroFormatada() {
        return dataRegistro != null ? dataRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—";
    }

    // Métodos e aliases de compatibilidade
    public String getCriadoPor() { return usuarioResponsavel; }
    public String getDataCriacao() { return getDataRegistroFormatada(); }
    public String getResumoAlteracoes() { return motivoAlteracao; }
    public String getTextoMinuta() { return conteudoMinuta; }
    public String getArquivoPdfPath() { return caminhoPdf; }
}
