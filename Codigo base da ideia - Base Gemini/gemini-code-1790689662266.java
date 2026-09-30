package com.festalia;

import java.util.ArrayList;
import java.util.List;

public class Cardapio {
    private String nome;
    private String descricao;
    private List<String> itens;

    public Cardapio(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.itens = new ArrayList<>();
    }

    public void adicionarItem(String item) {
        this.itens.add(item);
    }

    public String getNome() { 
        return nome; 
    }

    public String getDescricao() { 
        return descricao; 
    }

    public List<String> getItens() { 
        return itens; 
    }

    @Override
    public String toString() {
        return nome;
    }
}