package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CrearMotoRequest extends CrearVehiculoRequest {

    @NotNull
    @Positive
    private Integer cilindrada;

    @NotNull
    private Boolean incluyeCasco;

    public Integer getCilindrada() { return cilindrada; }
    public void setCilindrada(Integer cilindrada) { this.cilindrada = cilindrada; }
    public Boolean getIncluyeCasco() { return incluyeCasco; }
    public void setIncluyeCasco(Boolean incluyeCasco) { this.incluyeCasco = incluyeCasco; }
}