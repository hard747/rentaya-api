package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.AlquilerResponse;
import com.rentaya.rentaya_api.dto.CotizacionResponse;
import com.rentaya.rentaya_api.dto.CrearAlquilerRequest;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.model.Alquilable;
import com.rentaya.rentaya_api.model.Alquiler;
import com.rentaya.rentaya_api.model.Cliente;
import com.rentaya.rentaya_api.model.Vehiculo;
import com.rentaya.rentaya_api.repository.AlquilerRepository;
import com.rentaya.rentaya_api.repository.ClienteRepository;
import com.rentaya.rentaya_api.repository.VehiculoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;

    public AlquilerService(AlquilerRepository alquilerRepository, ClienteRepository clienteRepository,
                           VehiculoRepository vehiculoRepository) {
        this.alquilerRepository = alquilerRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    /**
     * Alquilar cambia el estado del vehículo y crea el alquiler en una sola transacción:
     * si algo falla, no queda ni el vehículo marcado como alquilado ni el alquiler a medias.
     */
    @Transactional
    public AlquilerResponse alquilar(CrearAlquilerRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        Vehiculo vehiculo = buscarVehiculo(request.vehiculoId());
        Alquiler alquiler = Alquiler.iniciar(cliente, vehiculo, request.dias());
        return toResponse(alquilerRepository.save(alquiler));
    }

    /**
     * Devolver registra los kilómetros, libera el vehículo y cierra el alquiler en la misma transacción.
     * Si clienteAutenticado no es null (rol CLIENTE), el alquiler debe ser suyo.
     */
    @Transactional
    public AlquilerResponse devolver(Long id, BigDecimal kmRecorridos, Long clienteAutenticado) {
        Alquiler alquiler = buscarPropio(id, clienteAutenticado);
        alquiler.finalizar(kmRecorridos);
        return toResponse(alquiler);
    }

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(Long clienteId, Long vehiculoId, int dias) {
        Cliente cliente = buscarCliente(clienteId);
        Vehiculo vehiculo = buscarVehiculo(vehiculoId);
        if (!(vehiculo instanceof Alquilable alquilable)) {
            throw new IllegalArgumentException("Los camiones son de uso interno y no se alquilan");
        }
        return new CotizacionResponse(cliente.getId(), cliente.getNombre(), vehiculo.getId(),
                vehiculo.getDescripcion(), dias, cliente.calcularTotal(alquilable, dias));
    }

    @Transactional(readOnly = true)
    public AlquilerResponse obtener(Long id, Long clienteAutenticado) {
        return toResponse(buscarPropio(id, clienteAutenticado));
    }

    private Alquiler buscarPropio(Long id, Long clienteAutenticado) {
        Alquiler alquiler = buscar(id);
        if (clienteAutenticado != null && !alquiler.getCliente().getId().equals(clienteAutenticado)) {
            throw new AccessDeniedException("El alquiler " + id + " no te pertenece");
        }
        return alquiler;
    }

    @Transactional(readOnly = true)
    public Page<AlquilerResponse> listar(Long clienteId, Pageable pageable) {
        Page<Alquiler> alquileres = clienteId == null
                ? alquilerRepository.findAll(pageable)
                : alquilerRepository.findByClienteId(clienteId, pageable);
        return alquileres.map(this::toResponse);
    }

    private Alquiler buscar(Long id) {
        return alquilerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un alquiler con id " + id));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + id));
    }

    private Vehiculo buscarVehiculo(Long id) {
        return vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un vehículo activo con id " + id));
    }

    private AlquilerResponse toResponse(Alquiler a) {
        return new AlquilerResponse(a.getId(), a.getCliente().getId(), a.getCliente().getNombre(),
                a.getVehiculo().getId(), a.getVehiculo().getDescripcion(), a.getDias(), a.getCostoTotal(),
                a.getEstado().name(), a.getFechaInicio(), a.getFechaDevolucion(), a.getKmRecorridos());
    }
}
