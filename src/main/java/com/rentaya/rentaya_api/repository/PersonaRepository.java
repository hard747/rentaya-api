package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {

    boolean existsByDni(String dni);
}
