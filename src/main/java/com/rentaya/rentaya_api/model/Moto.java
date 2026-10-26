package com.rentaya.rentaya_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("MOTO")
public class Moto extends Vehiculo {

    @Column(name = "cilindrada")
    private Integer cilindrada;

    @Column(name = "incluye_casco")
    private Boolean incluyeCasco;

    protected Moto() { }

    public Moto(String placa, String marca, String modelo, int anio,
                BigDecimal tarifaDiariaBase, Integer cilindrada, Boolean incluyeCasco) {
        super(placa, marca, modelo, anio, tarifaDiariaBase);
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

    public Integer getCilindrada() { return cilindrada; }
    public Boolean getIncluyeCasco() { return incluyeCasco; }
}