package com.festalia.financeiro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma parcela do cronograma financeiro vinculado à Ordem de Serviço (OS).
 */
public class ParcelaOS {

    public enum StatusParcela {
        PAGO("Pago", "#15803d", "#dcfce7"),
        PAGAMENTO_PARCIAL("Pagamento Parcial", "#1d4ed8", "#dbeafe"),
        A_VENCER("A Vencer", "#b45309", "#fef3c7"),
        VENCIDO("Vencido", "#b91c1c", "#fee2e2"),
        CANCELADO("Cancelado", "#4b5563", "#f1f5f9");

        private final String descricao;
        private final String corTexto;
        private final String corFundo;

        StatusParcela(String descricao, String corTexto, String corFundo) {
            this.descricao = descricao;
            this.corTexto = corTexto;
            this.corFundo = corFundo;
        }

        public String getDescricao() { return descricao; }
        public String getCorTexto() { return corTexto; }
        public String getCorFundo() { return corFundo; }
    }

    private int id;
    private int ordemServicoId;
    private int numeroParcela;
    private String descricao;
    private double valor;
    private LocalDate dataVencimento;
    private String formaPagamentoSugerida;
    private String statusManual; // Opcional, caso queira forçar CANCELADO
    private String observacoes;
    private LocalDateTime dataCriacao;

    private List<PagamentoParcela> pagamentos = new ArrayList<>();

    public ParcelaOS() {
        this.dataCriacao = LocalDateTime.now();
        this.statusManual = "PENDENTE";
    }

    public ParcelaOS(int id, int ordemServicoId, int numeroParcela, String descricao, double valor,
                     LocalDate dataVencimento, String formaPagamentoSugerida, String statusManual,
                     String observacoes, LocalDateTime dataCriacao) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.numeroParcela = numeroParcela;
        this.descricao = descricao != null ? descricao : "Parcela " + numeroParcela;
        this.valor = Math.max(0.0, valor);
        this.dataVencimento = dataVencimento != null ? dataVencimento : LocalDate.now();
        this.formaPagamentoSugerida = formaPagamentoSugerida != null ? formaPagamentoSugerida : "PIX";
        this.statusManual = statusManual != null ? statusManual : "PENDENTE";
        this.observacoes = observacoes != null ? observacoes : "";
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
    }

    public ParcelaOS(int id, int ordemServicoId, int numeroParcela, String descricao, double valor,
                     LocalDate dataVencimento, String formaPagamentoSugerida, String observacoes,
                     double valorPagoInicial, String statusManual) {
        this(id, ordemServicoId, numeroParcela, descricao, valor, dataVencimento, formaPagamentoSugerida, statusManual, observacoes, LocalDateTime.now());
        if (valorPagoInicial > 0.0) {
            this.pagamentos.add(new PagamentoParcela(0, id, ordemServicoId, valorPagoInicial, LocalDate.now(), formaPagamentoSugerida, "Inicial", "Registro inicial", "Sistema", LocalDateTime.now()));
        }
    }

    /**
     * Calcula o total já pago especificamente para esta parcela.
     */
    public double getValorPago() {
        if (pagamentos == null || pagamentos.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (PagamentoParcela p : pagamentos) {
            total += p.getValorPago();
        }
        return total;
    }

    /**
     * Calcula o saldo que ainda resta pagar para quitar esta parcela.
     */
    public double getSaldoRestante() {
        return Math.max(0.0, valor - getValorPago());
    }

    /**
     * Calcula o status dinâmico com base nos pagamentos realizados, data de vencimento e data atual.
     */
    public StatusParcela getStatusDinamico() {
        if ("CANCELADO".equalsIgnoreCase(statusManual)) {
            return StatusParcela.CANCELADO;
        }

        double saldo = getSaldoRestante();
        double pago = getValorPago();

        if (saldo <= 0.001 && valor > 0.0) {
            return StatusParcela.PAGO;
        }

        if (pago > 0.001) {
            return StatusParcela.PAGAMENTO_PARCIAL;
        }

        LocalDate hoje = LocalDate.now();
        if (dataVencimento != null && dataVencimento.isBefore(hoje)) {
            return StatusParcela.VENCIDO;
        }

        return StatusParcela.A_VENCER;
    }

    /**
     * Retorna a quantidade de dias em atraso caso a parcela esteja vencida.
     */
    public long getDiasAtraso() {
        if (getStatusDinamico() == StatusParcela.VENCIDO && dataVencimento != null) {
            return Math.max(0, ChronoUnit.DAYS.between(dataVencimento, LocalDate.now()));
        }
        return 0;
    }

    public long getDiasEmAtraso() {
        return getDiasAtraso();
    }

    public String getStatusCalculado() {
        switch (getStatusDinamico()) {
            case PAGO: return "PAGO";
            case PAGAMENTO_PARCIAL: return "PAGAMENTO PARCIAL";
            case VENCIDO: return "VENCIDO";
            case A_VENCER: return "A VENCER";
            case CANCELADO: return "CANCELADO";
            default: return "PENDENTE";
        }
    }

    public boolean isTotalmentePaga() {
        return getStatusDinamico() == StatusParcela.PAGO;
    }

    public String getDataVencimentoFormatada() {
        return dataVencimento != null ? dataVencimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getValorFormatado() {
        return String.format("R$ %,.2f", valor);
    }

    public String getValorPagoFormatado() {
        return String.format("R$ %,.2f", getValorPago());
    }

    public String getSaldoRestanteFormatado() {
        return String.format("R$ %,.2f", getSaldoRestante());
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(int ordemServicoId) { this.ordemServicoId = ordemServicoId; }

    public int getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(int numeroParcela) { this.numeroParcela = numeroParcela; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public String getFormaPagamentoSugerida() { return formaPagamentoSugerida; }
    public void setFormaPagamentoSugerida(String formaPagamentoSugerida) { this.formaPagamentoSugerida = formaPagamentoSugerida; }

    public String getStatusManual() { return statusManual; }
    public void setStatusManual(String statusManual) { this.statusManual = statusManual; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public List<PagamentoParcela> getPagamentos() { return pagamentos; }
    public void setPagamentos(List<PagamentoParcela> pagamentos) { this.pagamentos = pagamentos != null ? pagamentos : new ArrayList<>(); }
}
