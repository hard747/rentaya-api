package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.EstadisticaMarcaResponse;
import com.rentaya.rentaya_api.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<EstadisticaMarcaResponse> obtenerEstadisticasPorMarca() {
        return vehiculoRepository.obtenerEstadisticasPorMarca().stream()
                .map(e -> new EstadisticaMarcaResponse(
                        e.getMarca(),
                        e.getPromedioKilometraje(),
                        e.getPromedioCilindrada(),
                        e.getCantidad()))
                .toList();
    }
}