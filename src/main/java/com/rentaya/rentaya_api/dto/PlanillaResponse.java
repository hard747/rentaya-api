package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;
import java.util.List;

/** Pagos del mes a todos los Pagable (empleados y taller) y el total. */
public record PlanillaResponse(
        String periodo,
        String estado,
        List<PagoResponse> pagos,
        BigDecimal total) { }
