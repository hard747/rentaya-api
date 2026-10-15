package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EstadisticaMarcaResponse(
        String marca,
        BigDecimal promedioKilometraje,
        BigDecimal promedioCilindrada,
        long cantidad) { }