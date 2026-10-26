package com.rentaya.rentaya_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("AUTO")
public class Auto extends Vehiculo implements Alquilable {

    private static final BigDecimal DESCUENTO_SEMANAL = new BigDecimal("0.90");

    @Column(name = "numero_puertas")
    private Integer numeroPuertas;

    @Column(name = "disponible")
    private boolean disponible = true;

    protected Auto() { }

    public Auto(String placa, String marca, String modelo, int anio,
                BigDecimal tarifaDiariaBase, Integer numeroPuertas) {
        super(placa, marca, modelo, anio, tarifaDiariaBase);
        if (numeroPuertas == null || numeroPuertas < 2 || numeroPuertas > 5) {
            throw new IllegalArgumentException("El número de puertas debe estar entre 2 y 5");
        }
        this.numeroPuertas = numeroPuertas;
    }

    @Override
    public String getTipo() { return "AUTO"; }

    @Override
    public BigDecimal calcularCostoMantenimiento() {
        return new BigDecimal("100")
                .add(getKilometraje().multiply(new BigDecimal("0.05")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** dias * tarifa; desde 7 días se aplica 10% de descuento. */
    @Override
    public BigDecimal calcularCostoAlquiler(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días de alquiler deben ser mayores que 0");
        }
        BigDecimal costo = getTarifaDiariaBase().multiply(BigDecimal.valueOf(dias));
        if (dias >= 7) {
            costo = costo.multiply(DESCUENTO_SEMANAL);
        }
        return costo.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean estaDisponible() { return disponible; }

    @Override
    public void alquilar() {
        if (!disponible) {
            throw new IllegalStateException("El auto " + getPlaca() + " ya está alquilado");
        }
        disponible = false;
    }

    @Override
    public void devolver(BigDecimal kmRecorridos) {
        if (disponible) {
            throw new IllegalStateException("El auto " + getPlaca() + " no está alquilado");
        }
        registrarKilometros(kmRecorridos);
        disponible = true;
    }

    public Integer getNumeroPuertas() { return numeroPuertas; }
}
