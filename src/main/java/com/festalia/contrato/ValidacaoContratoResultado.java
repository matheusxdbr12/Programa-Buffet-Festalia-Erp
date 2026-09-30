package com.festalia.contrato;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado da auditoria e validação automática prévia antes da emissão de contratos.
 */
public class ValidacaoContratoResultado {

    private final List<String> pendencias = new ArrayList<>();
    private final List<String> avisos = new ArrayList<>();

    public void adicionarPendencia(String msg) {
        pendencias.add(msg);
    }

    public void adicionarAviso(String msg) {
        avisos.add(msg);
    }

    public boolean isValido() {
        return pendencias.isEmpty();
    }

    public boolean temAvisos() {
        return !avisos.isEmpty();
    }

    public List<String> getPendencias() {
        return pendencias;
    }

    public List<String> getErros() {
        return pendencias;
    }

    public List<String> getAvisos() {
        return avisos;
    }
}
