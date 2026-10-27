package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.ActualizarTarifaRequest;
import com.rentaya.rentaya_api.dto.CrearVehiculoRequest;
import com.rentaya.rentaya_api.dto.EstadisticaMarcaResponse;
import com.rentaya.rentaya_api.dto.MantenimientoResponse;
import com.rentaya.rentaya_api.dto.RegistrarKilometrosRequest;
import com.rentaya.rentaya_api.dto.VehiculoResponse;
import com.rentaya.rentaya_api.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody CrearVehiculoRequest request) {
        VehiculoResponse creado = vehiculoService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return vehiculoService.obtener(id);
    }

    @GetMapping
    public Page<VehiculoResponse> listar(@RequestParam(required = false) String tipo,
                                         @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return vehiculoService.listarActivos(tipo, pageable);
    }

    @PutMapping("/{id}/tarifa")
    public VehiculoResponse actualizarTarifa(@PathVariable Long id,
                                             @Valid @RequestBody ActualizarTarifaRequest request) {
        return vehiculoService.actualizarTarifa(id, request.tarifaDiariaBase());
    }

    @PostMapping("/{id}/kilometros")
    public VehiculoResponse registrarKilometros(@PathVariable Long id,
                                                @Valid @RequestBody RegistrarKilometrosRequest request) {
        return vehiculoService.registrarKilometros(id, request.kilometros());
    }

    @GetMapping("/{id}/mantenimiento")
    public MantenimientoResponse mantenimiento(@PathVariable Long id) {
        return vehiculoService.calcularMantenimiento(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estadisticas/por-marca")
    public List<EstadisticaMarcaResponse> estadisticasPorMarca() {
        return vehiculoService.obtenerEstadisticasPorMarca();
    }
}
