package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CrearAutoRequest extends CrearVehiculoRequest {

    @NotNull
    @Min(2)
    @Max(5)
    private Integer numeroPuertas;

    public Integer getNumeroPuertas() { return numeroPuertas; }
    public void setNumeroPuertas(Integer numeroPuertas) { this.numeroPuertas = numeroPuertas; }
}