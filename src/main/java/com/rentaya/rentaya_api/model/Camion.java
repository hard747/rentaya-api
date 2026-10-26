package com.rentaya.rentaya_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("CAMION")
public class Camion extends Vehiculo {

    @Column(name = "capacidad_toneladas", precision = 8, scale = 2)
    private BigDecimal capacidadToneladas;

    protected Camion() { }

    public Camion(String placa, String marca, String modelo, int anio,
                  BigDecimal tarifaDiariaBase, BigDecimal capacidadToneladas) {
        super(placa, marca, modelo, anio, tarifaDiariaBase);
        this.capacidadToneladas = capacidadToneladas;
    }

    @Override
    public String getTipo() { return "CAMION"; }

    @Override
    public BigDecimal calcularCostoMantenimiento() {
        return new BigDecimal("300")
                .add(getKilometraje().multiply(new BigDecimal("0.10")))
                .add(capacidadToneladas.multiply(new BigDecimal("20")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getCapacidadToneladas() { return capacidadToneladas; }
}