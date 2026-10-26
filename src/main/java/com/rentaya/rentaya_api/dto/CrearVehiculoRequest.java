package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrearAutoRequest.class, name = "AUTO"),
        @JsonSubTypes.Type(value = CrearMotoRequest.class, name = "MOTO"),
        @JsonSubTypes.Type(value = CrearCamionRequest.class, name = "CAMION")
})
public abstract class CrearVehiculoRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "debe tener el formato AAA-999")
    private String placa;

    @NotBlank
    private String marca;

    @NotBlank
    private String modelo;

    @NotNull
    @Min(1990)
    @Max(2027)
    private Integer anio;

    @NotNull
    @Positive
    private BigDecimal tarifaDiariaBase;

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public BigDecimal getTarifaDiariaBase() { return tarifaDiariaBase; }
    public void setTarifaDiariaBase(BigDecimal tarifaDiariaBase) { this.tarifaDiariaBase = tarifaDiariaBase; }
}