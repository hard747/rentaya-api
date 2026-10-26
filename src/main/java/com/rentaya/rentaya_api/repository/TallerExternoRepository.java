package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.TallerExterno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TallerExternoRepository extends JpaRepository<TallerExterno, Long> {

    boolean existsByRuc(String ruc);
}
