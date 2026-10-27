package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solo ADMIN: crea cuentas de ADMIN o EMPLEADO (los clientes usan el registro). */
public record CrearUsuarioRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Pattern(regexp = "^(ADMIN|EMPLEADO)$", message = "debe ser ADMIN o EMPLEADO") String rol) { }
