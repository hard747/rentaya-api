package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;

/** No existe una persona genérica: todo es cliente, cliente premium, conductor o mecánico. */
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo")
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    protected Persona() { }

    protected Persona(String nombre, String dni) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
        this.nombre = nombre;
        this.dni = dni;
    }

    public abstract String getRol();

    /** Escrito una sola vez: funciona para todo tipo de persona gracias a getRol(). */
    public final String mostrarInfo() {
        return "[" + getRol() + "] " + nombre + " - DNI " + dni;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
}
