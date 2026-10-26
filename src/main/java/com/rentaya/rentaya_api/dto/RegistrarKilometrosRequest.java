package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RegistrarKilometrosRequest(
        @NotNull @Positive BigDecimal kilometros) { }
