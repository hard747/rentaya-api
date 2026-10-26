package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** No existe un empleado genérico: todo empleado es conductor o mecánico. */
@Entity
@Table(name = "empleados")
public abstract class Empleado extends Persona implements Pagable {

    @Column(name = "sueldo_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal sueldoBase;

    @Column(name = "anios_antiguedad", nullable = false)
    private int aniosAntiguedad;

    protected Empleado() { }

    protected Empleado(String nombre, String dni, BigDecimal sueldoBase, int aniosAntiguedad) {
        super(nombre, dni);
        if (sueldoBase == null || sueldoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El sueldo base debe ser mayor que 0");
        }
        if (aniosAntiguedad < 0) {
            throw new IllegalArgumentException("Los años de antigüedad no pueden ser negativos");
        }
        this.sueldoBase = sueldoBase;
        this.aniosAntiguedad = aniosAntiguedad;
    }

    /** sueldoBase * 0.02 * aniosAntiguedad */
    public BigDecimal calcularBonoAntiguedad() {
        return sueldoBase.multiply(new BigDecimal("0.02"))
                .multiply(BigDecimal.valueOf(aniosAntiguedad))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getIdentificadorPago() { return getDni(); }

    public BigDecimal getSueldoBase() { return sueldoBase; }
    public int getAniosAntiguedad() { return aniosAntiguedad; }
}
