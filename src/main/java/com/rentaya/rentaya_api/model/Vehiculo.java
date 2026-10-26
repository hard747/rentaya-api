package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "vehiculos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
public abstract class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String placa;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private int anio;

    @Column(name = "tarifa_diaria_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaDiariaBase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal kilometraje = new BigDecimal("0.00");

    @Column(nullable = false)
    private boolean activo = true;

    protected Vehiculo() { }

    protected Vehiculo(String placa, String marca, String modelo, int anio, BigDecimal tarifaDiariaBase) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.tarifaDiariaBase = tarifaDiariaBase;
    }

    public void registrarKilometros(BigDecimal km) {
        if (km == null || km.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los kilómetros deben ser mayores que 0");
        }
        this.kilometraje = this.kilometraje.add(km).setScale(2, RoundingMode.HALF_UP);
    }

    public void actualizarTarifa(BigDecimal nuevaTarifa) {
        if (nuevaTarifa == null || nuevaTarifa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser mayor que 0");
        }
        this.tarifaDiariaBase = nuevaTarifa.setScale(2, RoundingMode.HALF_UP);
    }

    public void desactivar() {
        this.activo = false;
    }

    public String getDescripcion() {
        return marca + " " + modelo + " (" + anio + ") - " + placa;
    }

    public abstract String getTipo();

    public abstract BigDecimal calcularCostoMantenimiento();

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAnio() { return anio; }
    public BigDecimal getTarifaDiariaBase() { return tarifaDiariaBase; }
    public BigDecimal getKilometraje() { return kilometraje; }
    public boolean isActivo() { return activo; }
}