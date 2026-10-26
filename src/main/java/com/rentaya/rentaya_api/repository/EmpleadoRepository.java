package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
}
