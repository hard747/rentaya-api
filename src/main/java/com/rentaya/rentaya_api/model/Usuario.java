package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;

/** Cuenta de acceso a la API. La contraseña se guarda solo como hash BCrypt. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    public enum Rol { ADMIN, EMPLEADO, CLIENTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Solo para usuarios CLIENTE: el cliente al que pertenece la cuenta. */
    @OneToOne
    @JoinColumn(name = "cliente_id", unique = true)
    private Cliente cliente;

    protected Usuario() { }

    public Usuario(String username, String passwordHash, Rol rol, Cliente cliente) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }
        if (rol == Rol.CLIENTE && cliente == null) {
            throw new IllegalArgumentException("Un usuario CLIENTE debe estar asociado a un cliente");
        }
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.cliente = cliente;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public Cliente getCliente() { return cliente; }
}
