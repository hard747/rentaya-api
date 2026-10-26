package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HorasExtraRequest(@NotNull @Positive Integer horas) { }
