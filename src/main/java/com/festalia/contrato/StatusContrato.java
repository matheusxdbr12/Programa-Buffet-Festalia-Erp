package com.festalia.contrato;

/**
 * Status possíveis para um Contrato de Prestação de Serviços no Festalia ERP.
 */
public enum StatusContrato {
    RASCUNHO("Rascunho", "#d97706", "#fef3c7", "Minuta em elaboração / prévia não emitida"),
    GERADO("Gerado", "#2563eb", "#dbeafe", "Documento e minuta formal gerados"),
    AGUARDANDO_ASSINATURA("Aguardando Assinatura", "#ea580c", "#ffedd5", "Enviado / impresso aguardando firma das partes"),
    ASSINADO("Assinado", "#16a34a", "#dcfce7", "Contrato formalmente assinado e em vigor"),
    CANCELADO("Cancelado", "#dc2626", "#fee2e2", "Cancelado ou rescindido formalmente"),
    SUBSTITUIDO("Substituído", "#475569", "#f1f5f9", "Substituído por aditivo ou nova versão");

    private final String descricao;
    private final String corTexto;
    private final String corFundo;
    private final String detalhe;

    StatusContrato(String descricao, String corTexto, String corFundo, String detalhe) {
        this.descricao = descricao;
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.detalhe = detalhe;
    }

    public String getDescricao() { return descricao; }
    public String getCorTexto() { return corTexto; }
    public String getCorFundo() { return corFundo; }
    public java.awt.Color getColorTexto() { return java.awt.Color.decode(corTexto); }
    public java.awt.Color getColorFundo() { return java.awt.Color.decode(corFundo); }
    public String getDetalhe() { return detalhe; }

    @Override
    public String toString() {
        return descricao;
    }
}
