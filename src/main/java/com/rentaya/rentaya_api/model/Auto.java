package com.rentaya.rentaya_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("AUTO")
public class Auto extends Vehiculo {

    @Column(name = "numero_puertas")
    private Integer numeroPuertas;

    protected Auto() { }

    public Auto(String placa, String marca, String modelo, int anio,
                BigDecimal tarifaDiariaBase, Integer numeroPuertas) {
        super(placa, marca, modelo, anio, tarifaDiariaBase);
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

    public Integer getNumeroPuertas() { return numeroPuertas; }
}