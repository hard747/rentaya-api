package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.PagoResponse;
import com.rentaya.rentaya_api.dto.PlanillaResponse;
import com.rentaya.rentaya_api.exception.ConflictoException;
import com.rentaya.rentaya_api.mapper.PersonaMapper;
import com.rentaya.rentaya_api.model.Pagable;
import com.rentaya.rentaya_api.model.Pago;
import com.rentaya.rentaya_api.repository.EmpleadoRepository;
import com.rentaya.rentaya_api.repository.PagoRepository;
import com.rentaya.rentaya_api.repository.TallerExternoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * La empresa paga cada mes a todo lo que es Pagable: conductores, mecánicos y el taller externo.
 * El servicio trabaja con List&lt;Pagable&gt; y no necesita saber de qué tipo es cada uno.
 */
@Service
public class PagoService {

    private final EmpleadoRepository empleadoRepository;
    private final TallerExternoRepository tallerRepository;
    private final PagoRepository pagoRepository;
    private final PersonaMapper mapper;

    public PagoService(EmpleadoRepository empleadoRepository, TallerExternoRepository tallerRepository,
                       PagoRepository pagoRepository, PersonaMapper mapper) {
        this.empleadoRepository = empleadoRepository;
        this.tallerRepository = tallerRepository;
        this.pagoRepository = pagoRepository;
        this.mapper = mapper;
    }

    /** Lo que se pagaría hoy, sin registrar nada. */
    @Transactional(readOnly = true)
    public PlanillaResponse calcularPendiente() {
        List<PagoResponse> pagos = pagables().stream().map(mapper::toPago).toList();
        return new PlanillaResponse(YearMonth.now().toString(), "PENDIENTE", pagos, total(pagos));
    }

    /**
     * Registra el pago de un mes y reinicia los contadores (viajes, horas extra, facturas).
     * Todo en una transacción: o se liquida el mes completo, o no se liquida nada.
     */
    @Transactional
    public PlanillaResponse liquidar(YearMonth periodo) {
        if (pagoRepository.existsByPeriodo(periodo.toString())) {
            throw new ConflictoException("El periodo " + periodo + " ya fue liquidado");
        }
        List<PagoResponse> pagos = new ArrayList<>();
        for (Pagable pagable : pagables()) {
            PagoResponse pago = mapper.toPago(pagable);
            pagoRepository.save(new Pago(periodo, pago.tipo(), pago.beneficiario(), pago.identificador(), pago.monto()));
            pagable.cerrarMes();
            pagos.add(pago);
        }
        return new PlanillaResponse(periodo.toString(), "PAGADO", pagos, total(pagos));
    }

    @Transactional(readOnly = true)
    public PlanillaResponse historial(YearMonth periodo) {
        List<PagoResponse> pagos = pagoRepository.findByPeriodoOrderByTipoAscBeneficiarioAsc(periodo.toString())
                .stream()
                .map(p -> new PagoResponse(p.getTipo(), p.getBeneficiario(), p.getIdentificador(), p.getMonto()))
                .toList();
        return new PlanillaResponse(periodo.toString(), pagos.isEmpty() ? "SIN_PAGOS" : "PAGADO", pagos, total(pagos));
    }

    private List<Pagable> pagables() {
        List<Pagable> pagables = new ArrayList<>(empleadoRepository.findAll());
        pagables.addAll(tallerRepository.findAll());
        return pagables;
    }

    private static BigDecimal total(List<PagoResponse> pagos) {
        return pagos.stream().map(PagoResponse::monto).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }
}
