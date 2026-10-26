package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CrearConductorRequest extends CrearEmpleadoRequest {

    @NotBlank
    @Pattern(regexp = "^[ABC]$", message = "debe ser A, B o C")
    private String licencia;

    public String getLicencia() { return licencia; }
    public void setLicencia(String licencia) { this.licencia = licencia; }
}
