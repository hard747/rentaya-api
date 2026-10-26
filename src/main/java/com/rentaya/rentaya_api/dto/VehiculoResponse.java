package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehiculoResponse(
        Long id,
        String tipo,
        String placa,
        String marca,
        String modelo,
        Integer anio,
        BigDecimal tarifaDiariaBase,
        BigDecimal kilometraje,
        String descripcion,
        Integer numeroPuertas,
        Integer cilindrada,
        Boolean incluyeCasco,
        BigDecimal capacidadToneladas) { }