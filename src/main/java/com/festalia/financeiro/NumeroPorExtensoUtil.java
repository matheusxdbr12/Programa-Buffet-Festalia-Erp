package com.festalia.financeiro;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utilitário para conversão de valores monetários em texto por extenso (Português - Brasil).
 * Utilizado na emissão de Recibos de Pagamento e Termos de Quitação do Festalia ERP.
 */
public final class NumeroPorExtensoUtil {

    private static final String[] UNIDADES = {
            "", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove",
            "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"
    };

    private static final String[] DEZENAS = {
            "", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"
    };

    private static final String[] CENTENAS = {
            "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos"
    };

    private NumeroPorExtensoUtil() {}

    /**
     * Converte um valor numérico em reais por extenso.
     * Exemplo: 2500.50 -> "dois mil e quinhentos reais e cinquenta centavos"
     */
    public static String converterParaExtenso(double valor) {
        if (valor < 0) {
            return "menos " + converterParaExtenso(-valor);
        }
        if (Math.abs(valor) < 0.001) {
            return "zero reais";
        }

        BigDecimal bd = BigDecimal.valueOf(valor).setScale(2, java.math.RoundingMode.HALF_EVEN);
        BigInteger parteInteira = bd.toBigInteger();
        int centavos = bd.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        StringBuilder sb = new StringBuilder();

        if (parteInteira.compareTo(BigInteger.ZERO) > 0) {
            sb.append(converterNumeroInteiro(parteInteira.longValue()));
            if (parteInteira.equals(BigInteger.ONE)) {
                sb.append(" real");
            } else {
                sb.append(" reais");
            }
        }

        if (centavos > 0) {
            if (sb.length() > 0) {
                sb.append(" e ");
            }
            sb.append(converterCentena(centavos));
            if (centavos == 1) {
                sb.append(" centavo");
            } else {
                sb.append(" centavos");
            }
        }

        if (sb.length() == 0) {
            return "zero reais";
        }

        // Capitaliza a primeira letra
        return Character.toUpperCase(sb.charAt(0)) + sb.substring(1);
    }

    private static String converterNumeroInteiro(long valor) {
        if (valor == 0) return "";
        if (valor == 100) return "cem";

        StringBuilder sb = new StringBuilder();

        long bilhoes = valor / 1_000_000_000L;
        long restoBilhoes = valor % 1_000_000_000L;
        if (bilhoes > 0) {
            sb.append(converterCentena((int) bilhoes)).append(bilhoes == 1 ? " bilhão" : " bilhões");
            if (restoBilhoes > 0) {
                sb.append(restoBilhoes < 100 || restoBilhoes % 100 == 0 ? " e " : ", ");
            }
        }

        long milhoes = restoBilhoes / 1_000_000L;
        long restoMilhoes = restoBilhoes % 1_000_000L;
        if (milhoes > 0) {
            sb.append(converterCentena((int) milhoes)).append(milhoes == 1 ? " milhão" : " milhões");
            if (restoMilhoes > 0) {
                sb.append(restoMilhoes < 100 || restoMilhoes % 100 == 0 ? " e " : ", ");
            }
        }

        long milhares = restoMilhoes / 1000L;
        long restoMilhares = restoMilhoes % 1000L;
        if (milhares > 0) {
            if (milhares == 1) {
                sb.append("mil");
            } else {
                sb.append(converterCentena((int) milhares)).append(" mil");
            }
            if (restoMilhares > 0) {
                sb.append(restoMilhares < 100 || restoMilhares % 100 == 0 ? " e " : " ");
            }
        }

        if (restoMilhares > 0) {
            sb.append(converterCentena((int) restoMilhares));
        }

        return sb.toString().trim();
    }

    private static String converterCentena(int n) {
        if (n == 0) return "";
        if (n == 100) return "cem";

        StringBuilder sb = new StringBuilder();
        int c = n / 100;
        int d = (n % 100) / 10;
        int u = n % 10;

        if (c > 0) {
            sb.append(CENTENAS[c]);
        }

        int resto = n % 100;
        if (resto > 0) {
            if (c > 0) sb.append(" e ");
            if (resto < 20) {
                sb.append(UNIDADES[resto]);
            } else {
                sb.append(DEZENAS[d]);
                if (u > 0) {
                    sb.append(" e ").append(UNIDADES[u]);
                }
            }
        }

        return sb.toString();
    }
}
