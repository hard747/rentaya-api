package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

/** Devuelve clientes normales y premium: la consulta es polimórfica. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);
}
