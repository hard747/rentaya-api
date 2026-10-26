package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;

public record MantenimientoResponse(
        Long id,
        String tipo,
        String placa,
        BigDecimal kilometraje,
        BigDecimal costoMantenimiento) { }
