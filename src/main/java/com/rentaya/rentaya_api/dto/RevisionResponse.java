package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;

public record RevisionResponse(
        Long mecanicoId,
        Long vehiculoId,
        String vehiculo,
        BigDecimal costoMantenimiento) { }
