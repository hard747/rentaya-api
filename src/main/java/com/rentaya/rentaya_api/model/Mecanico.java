package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "mecanicos")
@DiscriminatorValue("MECANICO")
public class Mecanico extends Empleado {

    private static final BigDecimal PAGO_POR_HORA_EXTRA = new BigDecimal("30");

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(name = "horas_extra_mes", nullable = false)
    private int horasExtraMes;

    protected Mecanico() { }

    public Mecanico(String nombre, String dni, BigDecimal sueldoBase, int aniosAntiguedad, String especialidad) {
        super(nombre, dni, sueldoBase, aniosAntiguedad);
        if (especialidad == null || especialidad.isBlank()) {
            throw new IllegalArgumentException("La especialidad no puede estar vacía");
        }
        this.especialidad = especialidad;
    }

    @Override
    public String getRol() { return "MECANICO"; }

    public void registrarHorasExtra(int horas) {
        if (horas <= 0) {
            throw new IllegalArgumentException("Las horas extra deben ser mayores que 0");
        }
        horasExtraMes += horas;
    }

    /** Devuelve el costo de mantenimiento de cualquier vehículo (polimorfismo). */
    public BigDecimal revisar(Vehiculo vehiculo) {
        return vehiculo.calcularCostoMantenimiento();
    }

    /** sueldoBase + bonoAntiguedad + horasExtra * 30 */
    @Override
    public BigDecimal calcularPago() {
        return getSueldoBase()
                .add(calcularBonoAntiguedad())
                .add(PAGO_POR_HORA_EXTRA.multiply(BigDecimal.valueOf(horasExtraMes)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void cerrarMes() {
        horasExtraMes = 0;
    }

    public String getEspecialidad() { return especialidad; }
    public int getHorasExtraMes() { return horasExtraMes; }
}
