package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Registro público: crea un cliente normal y su cuenta con rol CLIENTE. */
public record RegistroRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String nombre,
        @NotBlank @Pattern(regexp = "^\\d{8}$", message = "debe tener exactamente 8 dígitos") String dni,
        @NotBlank @Email String email) { }
