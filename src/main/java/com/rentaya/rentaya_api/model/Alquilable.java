package com.rentaya.rentaya_api.model;

import java.math.BigDecimal;

/** Lo que se puede alquilar a un cliente: autos y motos (los camiones no). */
public interface Alquilable {

    BigDecimal calcularCostoAlquiler(int dias);

    boolean estaDisponible();

    void alquilar();

    void devolver(BigDecimal kmRecorridos);
}
