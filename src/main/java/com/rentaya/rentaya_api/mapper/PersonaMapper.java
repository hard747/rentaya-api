package com.rentaya.rentaya_api.mapper;

import com.rentaya.rentaya_api.dto.*;
import com.rentaya.rentaya_api.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PersonaMapper {

    public Cliente toCliente(CrearClienteRequest r) {
        if (r instanceof CrearClientePremiumRequest p) {
            return new ClientePremium(p.getNombre(), p.getDni(), p.getEmail(), p.getPorcentajeDescuento());
        }
        return new Cliente(r.getNombre(), r.getDni(), r.getEmail());
    }

    public Empleado toEmpleado(CrearEmpleadoRequest r) {
        return switch (r) {
            case CrearConductorRequest c -> new Conductor(c.getNombre(), c.getDni(), c.getSueldoBase(),
                    c.getAniosAntiguedad(), c.getLicencia().charAt(0));
            case CrearMecanicoRequest m -> new Mecanico(m.getNombre(), m.getDni(), m.getSueldoBase(),
                    m.getAniosAntiguedad(), m.getEspecialidad());
            default -> throw new IllegalArgumentException("Tipo de empleado no soportado");
        };
    }

    public ClienteResponse toResponse(Cliente c) {
        BigDecimal descuento = c instanceof ClientePremium p ? p.getPorcentajeDescuento() : null;
        return new ClienteResponse(c.getId(), c.getRol(), c.getNombre(), c.getDni(), c.getEmail(),
                descuento, c.mostrarInfo());
    }

    public EmpleadoResponse toResponse(Empleado e) {
        String licencia = null;
        Integer viajes = null;
        BigDecimal promedio = null;
        String especialidad = null;
        Integer horasExtra = null;

        if (e instanceof Conductor c) {
            licencia = String.valueOf(c.getLicencia());
            viajes = c.getViajesMes();
            promedio = c.getPromedioCalificacion();
        } else if (e instanceof Mecanico m) {
            especialidad = m.getEspecialidad();
            horasExtra = m.getHorasExtraMes();
        }

        return new EmpleadoResponse(e.getId(), e.getRol(), e.getNombre(), e.getDni(), e.getSueldoBase(),
                e.getAniosAntiguedad(), e.calcularBonoAntiguedad(), e.calcularPago(),
                licencia, viajes, promedio, especialidad, horasExtra, e.mostrarInfo());
    }

    public PagoResponse toPago(Pagable p) {
        return switch (p) {
            case Empleado e -> new PagoResponse(e.getRol(), e.getNombre(), e.getIdentificadorPago(), e.calcularPago());
            case TallerExterno t -> new PagoResponse("TALLER", t.getRazonSocial(), t.getIdentificadorPago(),
                    t.calcularPago());
            default -> throw new IllegalArgumentException("Pagable no soportado");
        };
    }
}
