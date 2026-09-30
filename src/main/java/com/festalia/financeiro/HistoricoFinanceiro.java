package com.festalia.financeiro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de auditoria temporal de todas as movimentações financeiras da OS.
 */
public class HistoricoFinanceiro {

    private int id;
    private int ordemServicoId;
    private String tipoOperacao;
    private String descricao;
    private double valor;
    private String usuario;
    private LocalDateTime dataHora;

    public HistoricoFinanceiro() {
        this.dataHora = LocalDateTime.now();
        this.usuario = "Operador";
    }

    public HistoricoFinanceiro(int id, int ordemServicoId, String tipoOperacao, String descricao,
                               double valor, String usuario, LocalDateTime dataHora) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.tipoOperacao = tipoOperacao != null ? tipoOperacao : "OPERACAO";
        this.descricao = descricao != null ? descricao : "";
        this.valor = valor;
        this.usuario = (usuario != null && !usuario.isBlank()) ? usuario : "Operador";
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
    }

    public String getDataHoraFormatada() {
        return dataHora != null ? dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
    }

    public String getValorFormatado() {
        return String.format("R$ %,.2f", valor);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(int ordemServicoId) { this.ordemServicoId = ordemServicoId; }

    public String getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
