package com.auditoria.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class Formatador {
    
    private static final NumberFormat DINHEIRO = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public static String moeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        return DINHEIRO.format(valor);
    }
}