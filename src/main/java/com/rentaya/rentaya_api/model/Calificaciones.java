package com.rentaya.rentaya_api.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Reglas comunes de las calificaciones, usadas por Conductor y TallerExterno. */
final class Calificaciones {

    private Calificaciones() { }

    static void validar(int estrellas) {
        if (estrellas < 1 || estrellas > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
        }
    }

    static BigDecimal promedio(List<Integer> calificaciones) {
        if (calificaciones.isEmpty()) {
            return new BigDecimal("0.00");
        }
        int suma = calificaciones.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(suma)
                .divide(BigDecimal.valueOf(calificaciones.size()), 2, RoundingMode.HALF_UP);
    }
}
