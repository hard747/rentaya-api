package com.rentaya.rentaya_api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cliente normal (tipo CLIENTE); la subclase agrega los datos del cliente premium. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrearClienteRequest.class, name = "CLIENTE"),
        @JsonSubTypes.Type(value = CrearClientePremiumRequest.class, name = "CLIENTE_PREMIUM")
})
public class CrearClienteRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank
    @Email
    private String email;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
