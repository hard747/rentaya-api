package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Une un cliente con un vehículo alquilable durante una cantidad de días. */
@Entity
@Table(name = "alquileres")
public class Alquiler {

    public enum Estado { ACTIVO, FINALIZADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Column(nullable = false)
    private int dias;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @Column(name = "km_recorridos", precision = 12, scale = 2)
    private BigDecimal kmRecorridos;

    protected Alquiler() { }

    /**
     * Inicia un alquiler: solo vehículos alquilables (autos y motos) y solo si están disponibles.
     * El costo lo calcula el cliente, así el cliente premium aplica su descuento.
     */
    public static Alquiler iniciar(Cliente cliente, Vehiculo vehiculo, int dias) {
        if (!(vehiculo instanceof Alquilable alquilable)) {
            throw new IllegalArgumentException("Los camiones son de uso interno y no se alquilan");
        }
        if (!vehiculo.isActivo()) {
            throw new IllegalStateException("El vehículo " + vehiculo.getPlaca() + " fue dado de baja");
        }
        BigDecimal costo = cliente.calcularTotal(alquilable, dias);
        alquilable.alquilar();

        Alquiler alquiler = new Alquiler();
        alquiler.cliente = cliente;
        alquiler.vehiculo = vehiculo;
        alquiler.dias = dias;
        alquiler.costoTotal = costo;
        alquiler.estado = Estado.ACTIVO;
        alquiler.fechaInicio = LocalDateTime.now();
        return alquiler;
    }

    /** Devuelve el vehículo: registra los kilómetros y lo deja disponible otra vez. */
    public void finalizar(BigDecimal kmRecorridos) {
        if (estado == Estado.FINALIZADO) {
            throw new IllegalStateException("El alquiler " + id + " ya fue finalizado");
        }
        ((Alquilable) vehiculo).devolver(kmRecorridos);
        this.kmRecorridos = kmRecorridos;
        this.estado = Estado.FINALIZADO;
        this.fechaDevolucion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public int getDias() { return dias; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public Estado getEstado() { return estado; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaDevolucion() { return fechaDevolucion; }
    public BigDecimal getKmRecorridos() { return kmRecorridos; }
}
