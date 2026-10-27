package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Alquiler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    List<Alquiler> findByClienteIdOrderByFechaInicioDesc(Long clienteId);

    Page<Alquiler> findByClienteId(Long clienteId, Pageable pageable);
}
