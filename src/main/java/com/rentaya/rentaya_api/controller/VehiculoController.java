package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.EstadisticaMarcaResponse;
import com.rentaya.rentaya_api.service.VehiculoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/estadisticas/por-marca")
    public List<EstadisticaMarcaResponse> estadisticasPorMarca() {
        return vehiculoService.obtenerEstadisticasPorMarca();
    }
}