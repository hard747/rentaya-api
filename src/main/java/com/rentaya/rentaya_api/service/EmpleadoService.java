package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.CrearEmpleadoRequest;
import com.rentaya.rentaya_api.dto.EmpleadoResponse;
import com.rentaya.rentaya_api.dto.PagoResponse;
import com.rentaya.rentaya_api.dto.RevisionResponse;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.mapper.PersonaMapper;
import com.rentaya.rentaya_api.model.Conductor;
import com.rentaya.rentaya_api.model.Empleado;
import com.rentaya.rentaya_api.model.Mecanico;
import com.rentaya.rentaya_api.model.Vehiculo;
import com.rentaya.rentaya_api.repository.EmpleadoRepository;
import com.rentaya.rentaya_api.repository.PersonaRepository;
import com.rentaya.rentaya_api.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PersonaMapper mapper;

    public EmpleadoService(EmpleadoRepository empleadoRepository, PersonaRepository personaRepository,
                           VehiculoRepository vehiculoRepository, PersonaMapper mapper) {
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.mapper = mapper;
    }

    @Transactional
    public EmpleadoResponse crear(CrearEmpleadoRequest request) {
        if (personaRepository.existsByDni(request.getDni())) {
            throw new ConflictoException("Ya existe una persona con DNI " + request.getDni());
        }
        return mapper.toResponse(empleadoRepository.save(mapper.toEmpleado(request)));
    }

    @Transactional(readOnly = true)
    public EmpleadoResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResponse> listar() {
        return empleadoRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public EmpleadoResponse registrarViaje(Long id) {
        Conductor conductor = buscarConductor(id);
        conductor.registrarViaje();
        return mapper.toResponse(conductor);
    }

    @Transactional
    public EmpleadoResponse calificarConductor(Long id, int estrellas) {
        Conductor conductor = buscarConductor(id);
        conductor.calificar(estrellas);
        return mapper.toResponse(conductor);
    }

    @Transactional
    public EmpleadoResponse registrarHorasExtra(Long id, int horas) {
        Mecanico mecanico = buscarMecanico(id);
        mecanico.registrarHorasExtra(horas);
        return mapper.toResponse(mecanico);
    }

    @Transactional(readOnly = true)
    public RevisionResponse revisar(Long mecanicoId, Long vehiculoId) {
        Mecanico mecanico = buscarMecanico(mecanicoId);
        Vehiculo vehiculo = vehiculoRepository.findByIdAndActivoTrue(vehiculoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un vehículo activo con id " + vehiculoId));
        return new RevisionResponse(mecanicoId, vehiculoId, vehiculo.getDescripcion(), mecanico.revisar(vehiculo));
    }

    @Transactional(readOnly = true)
    public PagoResponse calcularPago(Long id) {
        return mapper.toPago(buscar(id));
    }

    Empleado buscar(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un empleado con id " + id));
    }

    private Conductor buscarConductor(Long id) {
        if (buscar(id) instanceof Conductor conductor) {
            return conductor;
        }
        throw new IllegalArgumentException("El empleado " + id + " no es conductor");
    }

    private Mecanico buscarMecanico(Long id) {
        if (buscar(id) instanceof Mecanico mecanico) {
            return mecanico;
        }
        throw new IllegalArgumentException("El empleado " + id + " no es mecánico");
    }
}
