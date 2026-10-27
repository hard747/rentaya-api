package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByPeriodo(String periodo);

    List<Pago> findByPeriodoOrderByTipoAscBeneficiarioAsc(String periodo);
}
