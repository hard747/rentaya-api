package com.rentaya.rentaya_api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CrearClientePremiumRequest extends CrearClienteRequest {

    @NotNull
    @DecimalMin("0")
    @DecimalMax("30")
    private BigDecimal porcentajeDescuento;

    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
}
