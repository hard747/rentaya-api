package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.CalificacionRequest;
import com.rentaya.rentaya_api.dto.CrearEmpleadoRequest;
import com.rentaya.rentaya_api.dto.EmpleadoResponse;
import com.rentaya.rentaya_api.dto.HorasExtraRequest;
import com.rentaya.rentaya_api.dto.PagoResponse;
import com.rentaya.rentaya_api.dto.RevisionResponse;
import com.rentaya.rentaya_api.service.EmpleadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody CrearEmpleadoRequest request) {
        EmpleadoResponse creado = empleadoService.crear(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @GetMapping("/{id}")
    public EmpleadoResponse obtener(@PathVariable Long id) {
        return empleadoService.obtener(id);
    }

    @GetMapping
    public List<EmpleadoResponse> listar() {
        return empleadoService.listar();
    }

    @PostMapping("/{id}/viajes")
    public EmpleadoResponse registrarViaje(@PathVariable Long id) {
        return empleadoService.registrarViaje(id);
    }

    @PostMapping("/{id}/calificaciones")
    public EmpleadoResponse calificar(@PathVariable Long id, @Valid @RequestBody CalificacionRequest request) {
        return empleadoService.calificarConductor(id, request.estrellas());
    }

    @PostMapping("/{id}/horas-extra")
    public EmpleadoResponse registrarHorasExtra(@PathVariable Long id, @Valid @RequestBody HorasExtraRequest request) {
        return empleadoService.registrarHorasExtra(id, request.horas());
    }

    @GetMapping("/{id}/revisiones/{vehiculoId}")
    public RevisionResponse revisar(@PathVariable Long id, @PathVariable Long vehiculoId) {
        return empleadoService.revisar(id, vehiculoId);
    }

    @GetMapping("/{id}/pago")
    public PagoResponse pago(@PathVariable Long id) {
        return empleadoService.calcularPago(id);
    }
}
