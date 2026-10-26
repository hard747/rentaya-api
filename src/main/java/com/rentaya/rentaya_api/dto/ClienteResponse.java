package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClienteResponse(
        Long id,
        String rol,
        String nombre,
        String dni,
        String email,
        BigDecimal porcentajeDescuento,
        String info) { }
