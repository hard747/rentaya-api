package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CrearCamionRequest extends CrearVehiculoRequest {

    @NotNull
    @Positive
    private BigDecimal capacidadToneladas;

    public BigDecimal getCapacidadToneladas() { return capacidadToneladas; }
    public void setCapacidadToneladas(BigDecimal capacidadToneladas) { this.capacidadToneladas = capacidadToneladas; }
}