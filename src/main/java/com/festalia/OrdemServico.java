package com.festalia;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Modelo de Ordem de Serviço (OS) para o Festalia ERP.
 * Centraliza as informações de contratação, logística, valores financeiros (incluindo taxas) e itens do menu.
 */
public class OrdemServico {

    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MOEDA_BRL = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private int id;
    private int numeroOS;
    private String cliente;
    private String telefone;
    private LocalDate dataEvento;
    private String local;
    private int quantidadeConvidados;
    private double precoPorPessoa;
    private double taxaServico;
    private double taxaTransporte;
    private double valorTotal;
    private String status;
    private String nomeBuffet = "Festalia Buffet & Eventos";
    private String subtituloBuffet = "Gestão Profissional de Buffets e Eventos";
    private String caminhoLogoBuffet;
    private Cardapio cardapioSelecionado;
    private String observacoes;
    private LocalDateTime dataCriacao;

    public OrdemServico(String cliente,
                        String telefone,
                        LocalDate dataEvento,
                        String local,
                        int quantidadeConvidados,
                        Cardapio cardapioSelecionado,
                        String observacoes) {
        this(0, DatabaseManager.gerarProximoNumeroOS(), cliente, telefone, dataEvento, local,
                quantidadeConvidados,
                cardapioSelecionado != null ? cardapioSelecionado.getPrecoPorPessoa() : 0.0,
                0.0, 0.0, 0.0,
                "Orçamento",
                cardapioSelecionado, observacoes, LocalDateTime.now());
        calcularValorTotalAutomatico();
    }

    public OrdemServico(int id,
                        int numeroOS,
                        String cliente,
                        String telefone,
                        LocalDate dataEvento,
                        String local,
                        int quantidadeConvidados,
                        double precoPorPessoa,
                        double taxaServico,
                        double taxaTransporte,
                        double valorTotal,
                        String status,
                        Cardapio cardapioSelecionado,
                        String observacoes,
                        LocalDateTime dataCriacao) {
        this(id, numeroOS, "Festalia Buffet & Eventos", "Gestão Profissional de Buffets e Eventos", null,
                cliente, telefone, dataEvento, local, quantidadeConvidados, precoPorPessoa,
                taxaServico, taxaTransporte, valorTotal, status, cardapioSelecionado, observacoes, dataCriacao);
    }

    public OrdemServico(int id,
                        int numeroOS,
                        String nomeBuffet,
                        String subtituloBuffet,
                        String cliente,
                        String telefone,
                        LocalDate dataEvento,
                        String local,
                        int quantidadeConvidados,
                        double precoPorPessoa,
                        double taxaServico,
                        double taxaTransporte,
                        double valorTotal,
                        String status,
                        Cardapio cardapioSelecionado,
                        String observacoes,
                        LocalDateTime dataCriacao) {
        this(id, numeroOS, nomeBuffet, subtituloBuffet, null,
                cliente, telefone, dataEvento, local, quantidadeConvidados, precoPorPessoa,
                taxaServico, taxaTransporte, valorTotal, status, cardapioSelecionado, observacoes, dataCriacao);
    }

    public OrdemServico(int id,
                        int numeroOS,
                        String nomeBuffet,
                        String subtituloBuffet,
                        String caminhoLogoBuffet,
                        String cliente,
                        String telefone,
                        LocalDate dataEvento,
                        String local,
                        int quantidadeConvidados,
                        double precoPorPessoa,
                        double taxaServico,
                        double taxaTransporte,
                        double valorTotal,
                        String status,
                        Cardapio cardapioSelecionado,
                        String observacoes,
                        LocalDateTime dataCriacao) {
        this.id = id;
        this.numeroOS = numeroOS > 0 ? numeroOS : DatabaseManager.gerarProximoNumeroOS();
        this.nomeBuffet = (nomeBuffet != null && !nomeBuffet.trim().isEmpty()) ? nomeBuffet.trim() : "Festalia Buffet & Eventos";
        this.subtituloBuffet = (subtituloBuffet != null && !subtituloBuffet.trim().isEmpty()) ? subtituloBuffet.trim() : "Gestão Profissional de Buffets e Eventos";
        this.caminhoLogoBuffet = (caminhoLogoBuffet != null && !caminhoLogoBuffet.trim().isEmpty()) ? caminhoLogoBuffet.trim() : null;
        this.cliente = Objects.requireNonNull(cliente, "O nome do cliente é obrigatório.");
        this.telefone = telefone != null ? telefone : "";
        this.dataEvento = dataEvento != null ? dataEvento : LocalDate.now();
        this.local = local != null ? local : "";
        this.quantidadeConvidados = Math.max(0, quantidadeConvidados);
        this.precoPorPessoa = Math.max(0.0, precoPorPessoa);
        this.taxaServico = Math.max(0.0, taxaServico);
        this.taxaTransporte = Math.max(0.0, taxaTransporte);

        double subtotal = this.quantidadeConvidados * this.precoPorPessoa;
        double valorTaxa = subtotal * (this.taxaServico / 100.0);
        double totalCalculado = subtotal + valorTaxa + this.taxaTransporte;
        this.valorTotal = valorTotal > 0 ? valorTotal : totalCalculado;

        this.status = (status != null && !status.trim().isEmpty()) ? status : "Orçamento";
        this.cardapioSelecionado = cardapioSelecionado;
        this.observacoes = observacoes != null ? observacoes : "";
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
    }

    public void calcularValorTotalAutomatico() {
        double subtotal = this.quantidadeConvidados * this.precoPorPessoa;
        double valorTaxa = subtotal * (this.taxaServico / 100.0);
        this.valorTotal = subtotal + valorTaxa + this.taxaTransporte;
    }

    public double getSubtotalBuffet() {
        return this.quantidadeConvidados * this.precoPorPessoa;
    }

    public String getSubtotalBuffetFormatado() {
        return MOEDA_BRL.format(getSubtotalBuffet());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroOS() {
        return numeroOS;
    }

    public void setNumeroOS(int numeroOS) {
        this.numeroOS = numeroOS;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = Objects.requireNonNull(cliente, "O nome do cliente é obrigatório.");
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone != null ? telefone : "";
    }

    public LocalDate getDataEvento() {
        return dataEvento;
    }

    public String getDataEventoFormatada() {
        return dataEvento != null ? dataEvento.format(FORMATO_DATA) : "";
    }

    public void setDataEvento(LocalDate dataEvento) {
        this.dataEvento = dataEvento != null ? dataEvento : LocalDate.now();
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local != null ? local : "";
    }

    public int getQuantidadeConvidados() {
        return quantidadeConvidados;
    }

    public void setQuantidadeConvidados(int quantidadeConvidados) {
        this.quantidadeConvidados = Math.max(0, quantidadeConvidados);
        calcularValorTotalAutomatico();
    }

    public double getPrecoPorPessoa() {
        return precoPorPessoa;
    }

    public void setPrecoPorPessoa(double precoPorPessoa) {
        this.precoPorPessoa = Math.max(0.0, precoPorPessoa);
        calcularValorTotalAutomatico();
    }

    public String getPrecoPorPessoaFormatado() {
        return MOEDA_BRL.format(precoPorPessoa);
    }

    public double getTaxaServico() {
        return taxaServico;
    }

    public void setTaxaServico(double taxaServico) {
        this.taxaServico = Math.max(0.0, taxaServico);
        calcularValorTotalAutomatico();
    }

    public double getValorTaxaServico() {
        return (this.quantidadeConvidados * this.precoPorPessoa) * (this.taxaServico / 100.0);
    }

    public String getValorTaxaServicoFormatado() {
        return MOEDA_BRL.format(getValorTaxaServico());
    }

    public String getTaxaServicoPercentualFormatada() {
        if (this.taxaServico == (long) this.taxaServico) {
            return String.format(Locale.forLanguageTag("pt-BR"), "%d%%", (long) this.taxaServico);
        } else {
            return String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", this.taxaServico);
        }
    }

    public String getTaxaServicoFormatada() {
        if (this.taxaServico <= 0.0) {
            return "0% (R$ 0,00)";
        }
        return getTaxaServicoPercentualFormatada() + " (" + getValorTaxaServicoFormatado() + ")";
    }

    public double getTaxaTransporte() {
        return taxaTransporte;
    }

    public void setTaxaTransporte(double taxaTransporte) {
        this.taxaTransporte = Math.max(0.0, taxaTransporte);
        calcularValorTotalAutomatico();
    }

    public String getTaxaTransporteFormatada() {
        return MOEDA_BRL.format(taxaTransporte);
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = Math.max(0.0, valorTotal);
    }

    public String getValorTotalFormatado() {
        return MOEDA_BRL.format(valorTotal);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null && !status.trim().isEmpty()) ? status : "Orçamento";
    }

    public Cardapio getCardapioSelecionado() {
        return cardapioSelecionado;
    }

    public void setCardapioSelecionado(Cardapio cardapioSelecionado) {
        this.cardapioSelecionado = cardapioSelecionado;
        if (cardapioSelecionado != null && this.precoPorPessoa == 0.0) {
            this.precoPorPessoa = cardapioSelecionado.getPrecoPorPessoa();
            calcularValorTotalAutomatico();
        }
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes != null ? observacoes : "";
    }

    public String getNomeBuffet() {
        return nomeBuffet != null && !nomeBuffet.trim().isEmpty() ? nomeBuffet : "Festalia Buffet & Eventos";
    }

    public void setNomeBuffet(String nomeBuffet) {
        this.nomeBuffet = (nomeBuffet != null && !nomeBuffet.trim().isEmpty()) ? nomeBuffet.trim() : "Festalia Buffet & Eventos";
    }

    public String getSubtituloBuffet() {
        return subtituloBuffet != null && !subtituloBuffet.trim().isEmpty() ? subtituloBuffet : "Gestão Profissional de Buffets e Eventos";
    }

    public void setSubtituloBuffet(String subtituloBuffet) {
        this.subtituloBuffet = (subtituloBuffet != null && !subtituloBuffet.trim().isEmpty()) ? subtituloBuffet.trim() : "Gestão Profissional de Buffets e Eventos";
    }

    public String getCaminhoLogoBuffet() {
        return caminhoLogoBuffet;
    }

    public void setCaminhoLogoBuffet(String caminhoLogoBuffet) {
        this.caminhoLogoBuffet = (caminhoLogoBuffet != null && !caminhoLogoBuffet.trim().isEmpty()) ? caminhoLogoBuffet.trim() : null;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getDataCriacaoFormatada() {
        return dataCriacao != null ? dataCriacao.format(FORMATO_DATA_HORA) : "";
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    @Override
    public String toString() {
        return String.format("OS #%d - %s (%s) - %s", numeroOS, cliente, getDataEventoFormatada(), getValorTotalFormatado());
    }
}
