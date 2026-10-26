package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "conductores")
@DiscriminatorValue("CONDUCTOR")
public class Conductor extends Empleado implements Calificable {

    private static final BigDecimal PAGO_POR_VIAJE = new BigDecimal("25");

    @Column(nullable = false, length = 1)
    private char licencia;

    @Column(name = "viajes_mes", nullable = false)
    private int viajesMes;

    @ElementCollection
    @CollectionTable(name = "conductor_calificaciones", joinColumns = @JoinColumn(name = "conductor_id"))
    @Column(name = "estrellas", nullable = false)
    private List<Integer> calificaciones = new ArrayList<>();

    protected Conductor() { }

    public Conductor(String nombre, String dni, BigDecimal sueldoBase, int aniosAntiguedad, char licencia) {
        super(nombre, dni, sueldoBase, aniosAntiguedad);
        if (licencia != 'A' && licencia != 'B' && licencia != 'C') {
            throw new IllegalArgumentException("La licencia debe ser A, B o C");
        }
        this.licencia = licencia;
    }

    @Override
    public String getRol() { return "CONDUCTOR"; }

    public void registrarViaje() {
        viajesMes++;
    }

    /** sueldoBase + bonoAntiguedad + viajes * 25 */
    @Override
    public BigDecimal calcularPago() {
        return getSueldoBase()
                .add(calcularBonoAntiguedad())
                .add(PAGO_POR_VIAJE.multiply(BigDecimal.valueOf(viajesMes)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void cerrarMes() {
        viajesMes = 0;
    }

    @Override
    public void calificar(int estrellas) {
        Calificaciones.validar(estrellas);
        calificaciones.add(estrellas);
    }

    @Override
    public BigDecimal getPromedioCalificacion() {
        return Calificaciones.promedio(calificaciones);
    }

    /** Solo lectura: las calificaciones no se modifican desde fuera de la clase. */
    public List<Integer> getCalificaciones() { return Collections.unmodifiableList(calificaciones); }
    public char getLicencia() { return licencia; }
    public int getViajesMes() { return viajesMes; }
}
