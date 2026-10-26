package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlquilerResponse(
        Long id,
        Long clienteId,
        String cliente,
        Long vehiculoId,
        String vehiculo,
        Integer dias,
        BigDecimal costoTotal,
        String estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaDevolucion,
        BigDecimal kmRecorridos) { }
