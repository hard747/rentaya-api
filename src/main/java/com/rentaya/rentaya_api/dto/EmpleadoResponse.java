package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmpleadoResponse(
        Long id,
        String rol,
        String nombre,
        String dni,
        BigDecimal sueldoBase,
        Integer aniosAntiguedad,
        BigDecimal bonoAntiguedad,
        BigDecimal pagoMes,
        String licencia,
        Integer viajesMes,
        BigDecimal promedioCalificacion,
        String especialidad,
        Integer horasExtraMes,
        String info) { }
