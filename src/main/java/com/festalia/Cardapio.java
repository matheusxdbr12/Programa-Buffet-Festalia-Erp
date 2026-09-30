package com.festalia;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Modelo de Cardápio para o Festalia ERP.
 * Representa uma opção de menu contendo identificador, título, descrição comercial,
 * tabela de preço base por pessoa e a lista de itens inclusos.
 */
public class Cardapio {

    private static final NumberFormat MOEDA_BRL = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private int id;
    private String nome;
    private String descricao;
    private double precoPorPessoa;
    private List<String> itens;

    public Cardapio(String nome, String descricao) {
        this(0, nome, descricao, 0.0, new ArrayList<>());
    }

    public Cardapio(String nome, String descricao, double precoPorPessoa) {
        this(0, nome, descricao, precoPorPessoa, new ArrayList<>());
    }

    public Cardapio(String nome, String descricao, List<String> itens) {
        this(0, nome, descricao, 0.0, itens);
    }

    public Cardapio(String nome, String descricao, double precoPorPessoa, List<String> itens) {
        this(0, nome, descricao, precoPorPessoa, itens);
    }

    public Cardapio(int id, String nome, String descricao, double precoPorPessoa, List<String> itens) {
        this.id = id;
        this.nome = Objects.requireNonNull(nome, "O nome do cardápio não pode ser nulo.");
        this.descricao = descricao != null ? descricao : "";
        this.precoPorPessoa = Math.max(0.0, precoPorPessoa);
        this.itens = new ArrayList<>();
        if (itens != null) {
            this.itens.addAll(itens);
        }
    }

    public void adicionarItem(String item) {
        if (item != null && !item.trim().isEmpty()) {
            this.itens.add(item.trim());
        }
    }

    public void removerItem(String item) {
        this.itens.remove(item);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Objects.requireNonNull(nome, "O nome do cardápio não pode ser nulo.");
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao != null ? descricao : "";
    }

    public double getPrecoPorPessoa() {
        return precoPorPessoa;
    }

    public void setPrecoPorPessoa(double precoPorPessoa) {
        this.precoPorPessoa = Math.max(0.0, precoPorPessoa);
    }

    public String getPrecoPorPessoaFormatado() {
        return MOEDA_BRL.format(precoPorPessoa);
    }

    public List<String> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void setItens(List<String> novosItens) {
        this.itens.clear();
        if (novosItens != null) {
            this.itens.addAll(novosItens);
        }
    }

    public String getItensComoTexto() {
        return String.join("\n", itens);
    }

    public void carregarItensDeTexto(String texto) {
        this.itens.clear();
        if (texto != null && !texto.trim().isEmpty()) {
            String[] linhas = texto.split("\\r?\\n");
            for (String linha : linhas) {
                String item = linha.trim();
                if (!item.isEmpty()) {
                    if (item.startsWith("•") || item.startsWith("-") || item.startsWith("*")) {
                        item = item.substring(1).trim();
                    }
                    if (!item.isEmpty()) {
                        this.itens.add(item);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (precoPorPessoa > 0) {
            return String.format("%s (%s / pessoa)", nome, getPrecoPorPessoaFormatado());
        }
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cardapio cardapio = (Cardapio) o;
        return Objects.equals(nome, cardapio.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}
