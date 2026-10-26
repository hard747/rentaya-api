package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CrearTallerRequest(
        @NotBlank String razonSocial,
        @NotBlank @Pattern(regexp = "^\\d{11}$", message = "debe tener exactamente 11 dígitos") String ruc) { }
