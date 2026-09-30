package com.festalia.contrato;

/**
 * Representa uma cláusula jurídica individual de uma minuta contratual.
 */
public class ClausulaContrato {

    private int numero;
    private String chave;
    private String titulo;
    private String conteudo;
    private boolean obrigatoria;
    private boolean ativa;

    public ClausulaContrato() {
        this.obrigatoria = false;
        this.ativa = true;
    }

    public ClausulaContrato(int numero, String chave, String titulo, String conteudo, boolean obrigatoria, boolean ativa) {
        this.numero = numero;
        this.chave = chave;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.obrigatoria = obrigatoria;
        this.ativa = ativa;
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public boolean isObrigatoria() { return obrigatoria; }
    public void setObrigatoria(boolean obrigatoria) { this.obrigatoria = obrigatoria; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public String getTituloFormatado() {
        return String.format("CLÁUSULA %dª — %s", numero, titulo.toUpperCase());
    }
}
