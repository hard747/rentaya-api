package com.rentaya.rentaya_api.model;

import java.math.BigDecimal;

/** Lo que la empresa paga cada mes: empleados y taller externo. */
public interface Pagable {

    BigDecimal calcularPago();

    String getIdentificadorPago();

    /** Reinicia los contadores del mes (viajes, horas extra, facturas) después de pagar. */
    void cerrarMes();
}
