package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** El taller no es persona ni empleado, pero se le paga y se le califica. */
@Entity
@Table(name = "talleres_externos")
public class TallerExterno implements Pagable, Calificable {

    private static final BigDecimal IMPUESTO = new BigDecimal("1.18");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @Column(name = "monto_facturado_mes", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoFacturadoMes = new BigDecimal("0.00");

    @ElementCollection
    @CollectionTable(name = "taller_calificaciones", joinColumns = @JoinColumn(name = "taller_id"))
    @Column(name = "estrellas", nullable = false)
    private List<Integer> calificaciones = new ArrayList<>();

    protected TallerExterno() { }

    public TallerExterno(String razonSocial, String ruc) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalArgumentException("La razón social no puede estar vacía");
        }
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener exactamente 11 dígitos");
        }
        this.razonSocial = razonSocial;
        this.ruc = ruc;
    }

    public void registrarFactura(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la factura debe ser mayor que 0");
        }
        montoFacturadoMes = montoFacturadoMes.add(monto).setScale(2, RoundingMode.HALF_UP);
    }

    /** montoFacturadoMes * 1.18 (incluye 18% de impuesto) */
    @Override
    public BigDecimal calcularPago() {
        return montoFacturadoMes.multiply(IMPUESTO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getIdentificadorPago() { return ruc; }

    @Override
    public void cerrarMes() {
        montoFacturadoMes = new BigDecimal("0.00");
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

    public List<Integer> getCalificaciones() { return Collections.unmodifiableList(calificaciones); }
    public Long getId() { return id; }
    public String getRazonSocial() { return razonSocial; }
    public String getRuc() { return ruc; }
    public BigDecimal getMontoFacturadoMes() { return montoFacturadoMes; }
}
