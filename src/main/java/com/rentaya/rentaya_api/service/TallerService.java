package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.CrearTallerRequest;
import com.rentaya.rentaya_api.dto.PagoResponse;
import com.rentaya.rentaya_api.dto.TallerResponse;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.mapper.PersonaMapper;
import com.rentaya.rentaya_api.model.TallerExterno;
import com.rentaya.rentaya_api.repository.TallerExternoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TallerService {

    private final TallerExternoRepository tallerRepository;
    private final PersonaMapper mapper;

    public TallerService(TallerExternoRepository tallerRepository, PersonaMapper mapper) {
        this.tallerRepository = tallerRepository;
        this.mapper = mapper;
    }

    @Transactional
    public TallerResponse crear(CrearTallerRequest request) {
        if (tallerRepository.existsByRuc(request.ruc())) {
            throw new ConflictoException("Ya existe un taller con RUC " + request.ruc());
        }
        return toResponse(tallerRepository.save(new TallerExterno(request.razonSocial(), request.ruc())));
    }

    @Transactional(readOnly = true)
    public TallerResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<TallerResponse> listar() {
        return tallerRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TallerResponse registrarFactura(Long id, BigDecimal monto) {
        TallerExterno taller = buscar(id);
        taller.registrarFactura(monto);
        return toResponse(taller);
    }

    @Transactional
    public TallerResponse calificar(Long id, int estrellas) {
        TallerExterno taller = buscar(id);
        taller.calificar(estrellas);
        return toResponse(taller);
    }

    @Transactional(readOnly = true)
    public PagoResponse calcularPago(Long id) {
        return mapper.toPago(buscar(id));
    }

    private TallerExterno buscar(Long id) {
        return tallerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un taller con id " + id));
    }

    private TallerResponse toResponse(TallerExterno t) {
        return new TallerResponse(t.getId(), t.getRazonSocial(), t.getRuc(), t.getMontoFacturadoMes(),
                t.calcularPago(), t.getPromedioCalificacion());
    }
}
