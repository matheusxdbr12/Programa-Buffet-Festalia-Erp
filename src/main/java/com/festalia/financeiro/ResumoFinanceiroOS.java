package com.festalia.financeiro;

/**
 * Modelo de consolidação e métricas do Dashboard Financeiro de uma Ordem de Serviço.
 */
public class ResumoFinanceiroOS {

    private final double valorTotalOS;
    private final double totalPago;
    private final double totalPendente;
    private final double totalVencido;
    private final int quantidadeParcelas;
    private final int quantidadeVencidas;
    private final int quantidadePagas;
    private final double percentualPago;

    public ResumoFinanceiroOS(double valorTotalOS, double totalPago, double totalPendente,
                              double totalVencido, int quantidadeParcelas, int quantidadeVencidas,
                              int quantidadePagas) {
        this.valorTotalOS = Math.max(0.0, valorTotalOS);
        this.totalPago = Math.max(0.0, totalPago);
        this.totalPendente = Math.max(0.0, totalPendente);
        this.totalVencido = Math.max(0.0, totalVencido);
        this.quantidadeParcelas = Math.max(0, quantidadeParcelas);
        this.quantidadeVencidas = Math.max(0, quantidadeVencidas);
        this.quantidadePagas = Math.max(0, quantidadePagas);

        if (this.valorTotalOS > 0) {
            this.percentualPago = Math.min(100.0, (this.totalPago / this.valorTotalOS) * 100.0);
        } else if (this.totalPago > 0) {
            this.percentualPago = 100.0;
        } else {
            this.percentualPago = 0.0;
        }
    }

    public boolean isTotalmenteQuitada() {
        return totalPendente <= 0.001 && valorTotalOS > 0 && totalPago >= (valorTotalOS - 0.01);
    }

    public boolean temParcelasVencidas() {
        return quantidadeVencidas > 0 || totalVencido > 0.001;
    }

    public String getValorTotalOSFormatado() {
        return String.format("R$ %,.2f", valorTotalOS);
    }

    public String getTotalPagoFormatado() {
        return String.format("R$ %,.2f", totalPago);
    }

    public String getTotalPendenteFormatado() {
        return String.format("R$ %,.2f", totalPendente);
    }

    public String getTotalVencidoFormatado() {
        return String.format("R$ %,.2f", totalVencido);
    }

    public String getPercentualPagoFormatado() {
        return String.format("%.2f%%", percentualPago);
    }

    // Getters
    public double getValorTotalOS() { return valorTotalOS; }
    public double getTotalPrevisto() { return valorTotalOS; }
    public double getTotalPago() { return totalPago; }
    public double getTotalPendente() { return totalPendente; }
    public double getTotalVencido() { return totalVencido; }
    public double getSaldoPendente() { return totalPendente; }
    public int getQuantidadeParcelas() { return quantidadeParcelas; }
    public int getTotalParcelas() { return quantidadeParcelas; }
    public int getQuantidadeVencidas() { return quantidadeVencidas; }
    public int getParcelasVencidas() { return quantidadeVencidas; }
    public int getQuantidadePagas() { return quantidadePagas; }
    public int getParcelasPagas() { return quantidadePagas; }
    public double getPercentualPago() { return percentualPago; }
}
