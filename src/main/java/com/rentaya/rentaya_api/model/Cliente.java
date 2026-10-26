package com.rentaya.rentaya_api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@DiscriminatorValue("CLIENTE")
public class Cliente extends Persona {

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    protected Cliente() { }

    public Cliente(String nombre, String dni, String email) {
        super(nombre, dni);
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        this.email = email;
    }

    @Override
    public String getRol() { return "CLIENTE"; }

    /** Costo del alquiler para un cliente normal: lo que calcule el vehículo. */
    public BigDecimal calcularTotal(Alquilable vehiculo, int dias) {
        return vehiculo.calcularCostoAlquiler(dias);
    }

    public String getEmail() { return email; }
}
