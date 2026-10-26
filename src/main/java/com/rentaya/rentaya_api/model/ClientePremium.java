package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "clientes_premium")
@DiscriminatorValue("CLIENTE_PREMIUM")
public class ClientePremium extends Cliente {

    private static final BigDecimal CIEN = new BigDecimal("100");

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    protected ClientePremium() { }

    public ClientePremium(String nombre, String dni, String email, BigDecimal porcentajeDescuento) {
        super(nombre, dni, email);
        if (porcentajeDescuento == null
                || porcentajeDescuento.compareTo(BigDecimal.ZERO) < 0
                || porcentajeDescuento.compareTo(new BigDecimal("30")) > 0) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 30");
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public String getRol() { return "CLIENTE PREMIUM"; }

    /** Reutiliza el total de un cliente normal y le aplica el descuento. */
    @Override
    public BigDecimal calcularTotal(Alquilable vehiculo, int dias) {
        BigDecimal factor = CIEN.subtract(porcentajeDescuento).divide(CIEN);
        return super.calcularTotal(vehiculo, dias).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
}
