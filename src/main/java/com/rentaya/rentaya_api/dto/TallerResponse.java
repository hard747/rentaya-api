package com.rentaya.rentaya_api.dto;

import java.math.BigDecimal;

public record TallerResponse(
        Long id,
        String razonSocial,
        String ruc,
        BigDecimal montoFacturadoMes,
        BigDecimal pagoMes,
        BigDecimal promedioCalificacion) { }
