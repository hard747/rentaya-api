package com.rentaya.rentaya_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("MOTO")
public class Moto extends Vehiculo implements Alquilable {

    private static final BigDecimal FACTOR_MOTO = new BigDecimal("0.8");
    private static final BigDecimal CASCO_POR_DIA = new BigDecimal("5");

    @Column(name = "cilindrada")
    private Integer cilindrada;

    @Column(name = "incluye_casco")
    private Boolean incluyeCasco;

    @Column(name = "disponible")
    private boolean disponible = true;

    protected Moto() { }

    public Moto(String placa, String marca, String modelo, int anio,
                BigDecimal tarifaDiariaBase, Integer cilindrada, Boolean incluyeCasco) {
        super(placa, marca, modelo, anio, tarifaDiariaBase);
        if (cilindrada == null || cilindrada <= 0) {
            throw new IllegalArgumentException("La cilindrada debe ser mayor que 0");
        }
        if (incluyeCasco == null) {
            throw new IllegalArgumentException("Debe indicar si la moto incluye casco");
        }
        this.cilindrada = cilindrada;
        this.incluyeCasco = incluyeCasco;
    }

    @Override
    public String getTipo() { return "MOTO"; }

    @Override
    public BigDecimal calcularCostoMantenimiento() {
        return new BigDecimal("50")
                .add(getKilometraje().multiply(new BigDecimal("0.03")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** dias * tarifa * 0.8; con casco se suman 5 por día. Sin descuento semanal. */
    @Override
    public BigDecimal calcularCostoAlquiler(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días de alquiler deben ser mayores que 0");
        }
        BigDecimal costo = getTarifaDiariaBase().multiply(BigDecimal.valueOf(dias)).multiply(FACTOR_MOTO);
        if (incluyeCasco) {
            costo = costo.add(CASCO_POR_DIA.multiply(BigDecimal.valueOf(dias)));
        }
        return costo.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean estaDisponible() { return disponible; }

    @Override
    public void alquilar() {
        if (!disponible) {
            throw new IllegalStateException("La moto " + getPlaca() + " ya está alquilada");
        }
        disponible = false;
    }

    @Override
    public void devolver(BigDecimal kmRecorridos) {
        if (disponible) {
            throw new IllegalStateException("La moto " + getPlaca() + " no está alquilada");
        }
        registrarKilometros(kmRecorridos);
        disponible = true;
    }

    public Integer getCilindrada() { return cilindrada; }
    public Boolean getIncluyeCasco() { return incluyeCasco; }
}
