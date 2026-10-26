package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;

public record CotizacionResponse(
        Long clienteId,
        String cliente,
        Long vehiculoId,
        String vehiculo,
        Integer dias,
        BigDecimal costoTotal) { }
