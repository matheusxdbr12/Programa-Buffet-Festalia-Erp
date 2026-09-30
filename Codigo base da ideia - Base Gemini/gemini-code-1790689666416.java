package com.festalia;

import java.time.LocalDate;

public class OrdemServico {
    private static int contador = 1;
    private int numeroOS;
    private String cliente;
    private String telefone;
    private LocalDate dataEvento;
    private String local;
    private int quantidadeConvidados;
    private Cardapio cardapioSelecionado;
    private String observacoes;

    public OrdemServico(String cliente, String telefone, LocalDate dataEvento, 
                        String local, int quantidadeConvidados, Cardapio cardapio, String observacoes) {
        this.numeroOS = contador++;
        this.cliente = cliente;
        this.telefone = telefone;
        this.dataEvento = dataEvento;
        this.local = local;
        this.quantidadeConvidados = quantidadeConvidados;
        this.cardapioSelecionado = cardapio;
        this.observacoes = observacoes;
    }

    public int getNumeroOS() { return numeroOS; }
    public String getCliente() { return cliente; }
    public String getTelefone() { return telefone; }
    public LocalDate getDataEvento() { return dataEvento; }
    public String getLocal() { return local; }
    public int getQuantidadeConvidados() { return quantidadeConvidados; }
    public Cardapio getCardapioSelecionado() { return cardapioSelecionado; }
    public String getObservacoes() { return observacoes; }
}