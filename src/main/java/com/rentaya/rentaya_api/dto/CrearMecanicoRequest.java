package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotBlank;

public class CrearMecanicoRequest extends CrearEmpleadoRequest {

    @NotBlank
    private String especialidad;

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
