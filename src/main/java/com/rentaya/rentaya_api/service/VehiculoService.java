package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.CrearVehiculoRequest;
import com.rentaya.rentaya_api.dto.EstadisticaMarcaResponse;
import com.rentaya.rentaya_api.dto.MantenimientoResponse;
import com.rentaya.rentaya_api.dto.VehiculoResponse;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.mapper.VehiculoMapper;
import com.rentaya.rentaya_api.model.Alquilable;
import com.rentaya.rentaya_api.model.Auto;
import com.rentaya.rentaya_api.model.Camion;
import com.rentaya.rentaya_api.model.Moto;
import com.rentaya.rentaya_api.model.Vehiculo;
import com.rentaya.rentaya_api.repository.VehiculoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoService(VehiculoRepository vehiculoRepository, VehiculoMapper vehiculoMapper) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vehiculoMapper;
    }

    @Transactional
    public VehiculoResponse crear(CrearVehiculoRequest request) {
        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new ConflictoException("Ya existe un vehículo con placa " + request.getPlaca());
        }
        Vehiculo vehiculo = vehiculoMapper.toEntity(request);
        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return vehiculoMapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtener(Long id) {
        return vehiculoMapper.toResponse(buscarActivo(id));
    }

    @Transactional(readOnly = true)
    public Page<VehiculoResponse> listarActivos(String tipo, Pageable pageable) {
        Page<Vehiculo> vehiculos = (tipo == null || tipo.isBlank())
                ? vehiculoRepository.findByActivoTrue(pageable)
                : vehiculoRepository.findActivosPorTipo(claseDeTipo(tipo), pageable);
        return vehiculos.map(vehiculoMapper::toResponse);
    }

    @Transactional
    public VehiculoResponse actualizarTarifa(Long id, BigDecimal nuevaTarifa) {
        Vehiculo vehiculo = buscarActivo(id);
        vehiculo.actualizarTarifa(nuevaTarifa);
        return vehiculoMapper.toResponse(vehiculo);
    }

    @Transactional
    public VehiculoResponse registrarKilometros(Long id, BigDecimal kilometros) {
        Vehiculo vehiculo = buscarActivo(id);
        vehiculo.registrarKilometros(kilometros);
        return vehiculoMapper.toResponse(vehiculo);
    }

    @Transactional(readOnly = true)
    public MantenimientoResponse calcularMantenimiento(Long id) {
        Vehiculo vehiculo = buscarActivo(id);
        return new MantenimientoResponse(vehiculo.getId(), vehiculo.getTipo(), vehiculo.getPlaca(),
                vehiculo.getKilometraje(), vehiculo.calcularCostoMantenimiento());
    }

    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = buscarActivo(id);
        if (vehiculo instanceof Alquilable alquilable && !alquilable.estaDisponible()) {
            throw new IllegalStateException("No se puede dar de baja el vehículo " + vehiculo.getPlaca()
                    + " porque está alquilado");
        }
        vehiculo.desactivar();
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

    private Vehiculo buscarActivo(Long id) {
        return vehiculoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un vehículo activo con id " + id));
    }

    private Class<? extends Vehiculo> claseDeTipo(String tipo) {
        return switch (tipo.trim().toUpperCase(Locale.ROOT)) {
            case "AUTO" -> Auto.class;
            case "MOTO" -> Moto.class;
            case "CAMION" -> Camion.class;
            default -> throw new IllegalArgumentException(
                    "Tipo de vehículo no válido: " + tipo + ". Use AUTO, MOTO o CAMION");
        };
    }
}
