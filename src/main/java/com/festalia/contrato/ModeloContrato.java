package com.festalia.contrato;

/**
 * Modelos de Minutas Contratuais disponíveis para eventos e buffets.
 */
public enum ModeloContrato {
    BUFFET("Contrato Geral de Buffet & Gastronomia", "Padrão aplicável a almoços, jantares e recepções gerais"),
    BUFFET_COMPLETO("Contrato Geral de Buffet & Gastronomia", "Padrão aplicável a almoços, jantares e recepções gerais"),
    CASAMENTO("Contrato de Buffet para Casamento", "Cláusulas especializadas para cerimônias, recepções e montagens estendidas"),
    ANIVERSARIO("Contrato de Aniversário & Debutante", "Diretrizes para festas de 15 anos, infantis e celebrações familiares"),
    CORPORATIVO("Contrato de Evento Corporativo", "Termos para coffee breaks, coquetéis empresariais e convenções"),
    CONFRATERNIZACAO("Contrato de Confraternização", "Formato dinâmico para festas de fim de ano e encontros de empresas"),
    FORMATURA("Contrato de Buffet para Formatura", "Regras e serviços para bailes, colações e eventos acadêmicos");

    private final String titulo;
    private final String descricao;

    ModeloContrato(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }

    @Override
    public String toString() {
        return titulo;
    }
}
