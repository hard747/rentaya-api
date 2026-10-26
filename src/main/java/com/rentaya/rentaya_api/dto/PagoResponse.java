package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;

/** Lo que la empresa debe pagar este mes a un empleado o al taller. */
public record PagoResponse(
        String tipo,
        String beneficiario,
        String identificador,
        BigDecimal monto) { }
