package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.AlquilerResponse;
import com.rentaya.rentaya_api.dto.CotizacionResponse;
import com.rentaya.rentaya_api.dto.CrearAlquilerRequest;
import com.rentaya.rentaya_api.dto.DevolucionRequest;
import com.rentaya.rentaya_api.security.UsuarioActual;
import com.rentaya.rentaya_api.service.AlquilerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

/** ADMIN y EMPLEADO operan sobre cualquier alquiler; un CLIENTE solo sobre los suyos. */
@RestController
@RequestMapping("/api/v1/alquileres")
@Validated
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    @PostMapping
    public ResponseEntity<AlquilerResponse> alquilar(@Valid @RequestBody CrearAlquilerRequest request,
                                                     Authentication auth) {
        exigirMismoCliente(auth, request.clienteId());
        AlquilerResponse creado = alquilerService.alquilar(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri()).body(creado);
    }

    @PostMapping("/{id}/devolucion")
    public AlquilerResponse devolver(@PathVariable Long id, @Valid @RequestBody DevolucionRequest request,
                                     Authentication auth) {
        return alquilerService.devolver(id, request.kmRecorridos(), UsuarioActual.clienteId(auth));
    }

    @GetMapping("/cotizacion")
    public CotizacionResponse cotizar(@RequestParam Long clienteId, @RequestParam Long vehiculoId,
                                      @RequestParam @Positive int dias, Authentication auth) {
        exigirMismoCliente(auth, clienteId);
        return alquilerService.cotizar(clienteId, vehiculoId, dias);
    }

    @GetMapping("/{id}")
    public AlquilerResponse obtener(@PathVariable Long id, Authentication auth) {
        return alquilerService.obtener(id, UsuarioActual.clienteId(auth));
    }

    @GetMapping
    public Page<AlquilerResponse> listar(@RequestParam(required = false) Long clienteId,
                                         @PageableDefault(size = 20, sort = "fechaInicio",
                                                 direction = Sort.Direction.DESC) Pageable pageable,
                                         Authentication auth) {
        Long propio = UsuarioActual.clienteId(auth);
        return alquilerService.listar(propio != null ? propio : clienteId, pageable);
    }

    private static void exigirMismoCliente(Authentication auth, Long clienteId) {
        Long propio = UsuarioActual.clienteId(auth);
        if (propio != null && !propio.equals(clienteId)) {
            throw new AccessDeniedException("Solo puedes alquilar y cotizar a tu nombre");
        }
    }
}
