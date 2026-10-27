package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.PlanillaResponse;
import com.rentaya.rentaya_api.service.PagoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** Planilla del mes en curso, sin registrar el pago. */
    @GetMapping("/pendientes")
    public PlanillaResponse pendientes() {
        return pagoService.calcularPendiente();
    }

    /** Liquida el mes indicado (por ejemplo 2026-10): registra los pagos y reinicia los contadores. */
    @PostMapping("/liquidaciones/{periodo}")
    public PlanillaResponse liquidar(@PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo) {
        return pagoService.liquidar(periodo);
    }

    @GetMapping("/liquidaciones/{periodo}")
    public PlanillaResponse historial(@PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth periodo) {
        return pagoService.historial(periodo);
    }
}
