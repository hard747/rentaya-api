package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.AlquilerResponse;
import com.rentaya.rentaya_api.dto.CotizacionResponse;
import com.rentaya.rentaya_api.dto.CrearAlquilerRequest;
import com.rentaya.rentaya_api.dto.DevolucionRequest;
import com.rentaya.rentaya_api.service.AlquilerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alquileres")
@Validated
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    @PostMapping
    public ResponseEntity<AlquilerResponse> alquilar(@Valid @RequestBody CrearAlquilerRequest request) {
        AlquilerResponse creado = alquilerService.alquilar(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PostMapping("/{id}/devolucion")
    public AlquilerResponse devolver(@PathVariable Long id, @Valid @RequestBody DevolucionRequest request) {
        return alquilerService.devolver(id, request.kmRecorridos());
    }

    @GetMapping("/cotizacion")
    public CotizacionResponse cotizar(@RequestParam Long clienteId, @RequestParam Long vehiculoId,
                                      @RequestParam @Positive int dias) {
        return alquilerService.cotizar(clienteId, vehiculoId, dias);
    }

    @GetMapping("/{id}")
    public AlquilerResponse obtener(@PathVariable Long id) {
        return alquilerService.obtener(id);
    }

    @GetMapping
    public List<AlquilerResponse> listar(@RequestParam(required = false) Long clienteId) {
        return alquilerService.listar(clienteId);
    }
}
