package com.festalia.financeiro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entidade que registra um pagamento (total ou parcial) efetuado para uma parcela de uma OS.
 */
public class PagamentoParcela {

    private int id;
    private int parcelaId;
    private int ordemServicoId;
    private double valorPago;
    private LocalDate dataPagamento;
    private String formaPagamento;
    private String identificacaoTransacao;
    private String observacoes;
    private String usuarioResponsavel;
    private LocalDateTime dataRegistro;

    public PagamentoParcela() {
        this.dataPagamento = LocalDate.now();
        this.dataRegistro = LocalDateTime.now();
        this.usuarioResponsavel = "Operador";
    }

    public PagamentoParcela(int id, int parcelaId, int ordemServicoId, double valorPago,
                            LocalDate dataPagamento, String formaPagamento, String identificacaoTransacao,
                            String observacoes, String usuarioResponsavel, LocalDateTime dataRegistro) {
        this.id = id;
        this.parcelaId = parcelaId;
        this.ordemServicoId = ordemServicoId;
        this.valorPago = Math.max(0.0, valorPago);
        this.dataPagamento = dataPagamento != null ? dataPagamento : LocalDate.now();
        this.formaPagamento = formaPagamento != null ? formaPagamento : "PIX";
        this.identificacaoTransacao = identificacaoTransacao != null ? identificacaoTransacao : "";
        this.observacoes = observacoes != null ? observacoes : "";
        this.usuarioResponsavel = (usuarioResponsavel != null && !usuarioResponsavel.isBlank()) ? usuarioResponsavel : "Operador";
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
    }

    public String getDataPagamentoFormatada() {
        return dataPagamento != null ? dataPagamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getDataRegistroFormatada() {
        return dataRegistro != null ? dataRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getValorPagoFormatado() {
        return String.format("R$ %,.2f", valorPago);
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getParcelaId() { return parcelaId; }
    public void setParcelaId(int parcelaId) { this.parcelaId = parcelaId; }

    public int getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(int ordemServicoId) { this.ordemServicoId = ordemServicoId; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getIdentificacaoTransacao() { return identificacaoTransacao; }
    public void setIdentificacaoTransacao(String identificacaoTransacao) { this.identificacaoTransacao = identificacaoTransacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }

    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
}
