package com.rentaya.rentaya_api.mapper;

import com.rentaya.rentaya_api.dto.*;
import com.rentaya.rentaya_api.model.*;
import org.springframework.stereotype.Component;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(CrearVehiculoRequest request) {
        return switch (request) {
            case CrearAutoRequest a -> new Auto(a.getPlaca(), a.getMarca(), a.getModelo(),
                    a.getAnio(), a.getTarifaDiariaBase(), a.getNumeroPuertas());
            case CrearMotoRequest m -> new Moto(m.getPlaca(), m.getMarca(), m.getModelo(),
                    m.getAnio(), m.getTarifaDiariaBase(), m.getCilindrada(), m.getIncluyeCasco());
            case CrearCamionRequest c -> new Camion(c.getPlaca(), c.getMarca(), c.getModelo(),
                    c.getAnio(), c.getTarifaDiariaBase(), c.getCapacidadToneladas());
            default -> throw new IllegalArgumentException("Tipo de vehículo no soportado");
        };
    }

    public VehiculoResponse toResponse(Vehiculo v) {
        Integer numeroPuertas = null;
        Integer cilindrada = null;
        Boolean incluyeCasco = null;
        java.math.BigDecimal capacidadToneladas = null;

        if (v instanceof Auto a) {
            numeroPuertas = a.getNumeroPuertas();
        } else if (v instanceof Moto m) {
            cilindrada = m.getCilindrada();
            incluyeCasco = m.getIncluyeCasco();
        } else if (v instanceof Camion c) {
            capacidadToneladas = c.getCapacidadToneladas();
        }

        Boolean disponible = v instanceof Alquilable a ? a.estaDisponible() : null;

        return new VehiculoResponse(
                v.getId(), v.getTipo(), v.getPlaca(), v.getMarca(), v.getModelo(),
                v.getAnio(), v.getTarifaDiariaBase(), v.getKilometraje(), v.getDescripcion(),
                numeroPuertas, cilindrada, incluyeCasco, capacidadToneladas, disponible);
    }
}