package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** Registro de lo que se pagó a un empleado o al taller en un mes (periodo "YYYY-MM"). */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String beneficiario;

    @Column(nullable = false, length = 11)
    private String identificador;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    protected Pago() { }

    public Pago(YearMonth periodo, String tipo, String beneficiario, String identificador, BigDecimal monto) {
        this.periodo = periodo.toString();
        this.tipo = tipo;
        this.beneficiario = beneficiario;
        this.identificador = identificador;
        this.monto = monto;
        this.fechaPago = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getPeriodo() { return periodo; }
    public String getTipo() { return tipo; }
    public String getBeneficiario() { return beneficiario; }
    public String getIdentificador() { return identificador; }
    public BigDecimal getMonto() { return monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
}
