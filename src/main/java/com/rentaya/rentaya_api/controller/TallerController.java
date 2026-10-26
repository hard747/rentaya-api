package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.CalificacionRequest;
import com.rentaya.rentaya_api.dto.CrearTallerRequest;
import com.rentaya.rentaya_api.dto.FacturaRequest;
import com.rentaya.rentaya_api.dto.PagoResponse;
import com.rentaya.rentaya_api.dto.TallerResponse;
import com.rentaya.rentaya_api.service.TallerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/talleres")
public class TallerController {

    private final TallerService tallerService;

    public TallerController(TallerService tallerService) {
        this.tallerService = tallerService;
    }

    @PostMapping
    public ResponseEntity<TallerResponse> crear(@Valid @RequestBody CrearTallerRequest request) {
        TallerResponse creado = tallerService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @GetMapping("/{id}")
    public TallerResponse obtener(@PathVariable Long id) {
        return tallerService.obtener(id);
    }

    @GetMapping
    public List<TallerResponse> listar() {
        return tallerService.listar();
    }

    @PostMapping("/{id}/facturas")
    public TallerResponse registrarFactura(@PathVariable Long id, @Valid @RequestBody FacturaRequest request) {
        return tallerService.registrarFactura(id, request.monto());
    }

    @PostMapping("/{id}/calificaciones")
    public TallerResponse calificar(@PathVariable Long id, @Valid @RequestBody CalificacionRequest request) {
        return tallerService.calificar(id, request.estrellas());
    }

    @GetMapping("/{id}/pago")
    public PagoResponse pago(@PathVariable Long id) {
        return tallerService.calcularPago(id);
    }
}
