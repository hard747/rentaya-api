package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrearConductorRequest.class, name = "CONDUCTOR"),
        @JsonSubTypes.Type(value = CrearMecanicoRequest.class, name = "MECANICO")
})
public abstract class CrearEmpleadoRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "debe tener exactamente 8 dígitos")
    private String dni;

    @NotNull
    @Positive
    private BigDecimal sueldoBase;

    @NotNull
    @PositiveOrZero
    private Integer aniosAntiguedad;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public BigDecimal getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(BigDecimal sueldoBase) { this.sueldoBase = sueldoBase; }
    public Integer getAniosAntiguedad() { return aniosAntiguedad; }
    public void setAniosAntiguedad(Integer aniosAntiguedad) { this.aniosAntiguedad = aniosAntiguedad; }
}
