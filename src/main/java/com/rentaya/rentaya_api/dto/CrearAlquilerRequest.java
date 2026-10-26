package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearAlquilerRequest(
        @NotNull Long clienteId,
        @NotNull Long vehiculoId,
        @NotNull @Positive Integer dias) { }
