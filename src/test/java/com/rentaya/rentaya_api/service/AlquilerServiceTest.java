package com.rentaya.rentaya_api.service;

import com.rentaya.rentaya_api.dto.AlquilerResponse;
import com.rentaya.rentaya_api.dto.CrearAlquilerRequest;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.model.Alquiler;
import com.rentaya.rentaya_api.model.Auto;
import com.rentaya.rentaya_api.model.Camion;
import com.rentaya.rentaya_api.model.ClientePremium;
import com.rentaya.rentaya_api.repository.AlquilerRepository;
import com.rentaya.rentaya_api.repository.ClienteRepository;
import com.rentaya.rentaya_api.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Reglas del servicio de alquileres con los repositorios simulados (sin base de datos). */
@ExtendWith(MockitoExtension.class)
class AlquilerServiceTest {

    @Mock
    private AlquilerRepository alquilerRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private AlquilerService alquilerService;

    private final ClientePremium luis =
            new ClientePremium("Luis Gómez", "71234567", "luis@mail.com", new BigDecimal("20"));

    @Test
    @DisplayName("Alquilar calcula el total con el descuento premium y deja el auto no disponible")
    void alquilarConDescuentoPremium() {
        Auto auto = new Auto("ABC-123", "Toyota", "Yaris", 2022, new BigDecimal("120"), 4);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(luis));
        when(vehiculoRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.of(auto));
        when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(inv -> inv.getArgument(0));

        AlquilerResponse respuesta = alquilerService.alquilar(new CrearAlquilerRequest(1L, 2L, 7));

        assertThat(respuesta.costoTotal()).isEqualByComparingTo("604.80");
        assertThat(respuesta.estado()).isEqualTo("ACTIVO");
        assertThat(auto.estaDisponible()).isFalse();
        verify(alquilerRepository).save(any(Alquiler.class));
    }

    @Test
    @DisplayName("Un camión no se alquila y no se guarda nada")
    void camionNoSeAlquila() {
        Camion camion = new Camion("CAM-001", "Volvo", "FH", 2020, new BigDecimal("400"), new BigDecimal("12"));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(luis));
        when(vehiculoRepository.findByIdAndActivoTrue(3L)).thenReturn(Optional.of(camion));

        assertThatThrownBy(() -> alquilerService.alquilar(new CrearAlquilerRequest(1L, 3L, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no se alquilan");
        verify(alquilerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un cliente inexistente devuelve 404 antes de tocar el vehículo")
    void clienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alquilerService.alquilar(new CrearAlquilerRequest(99L, 2L, 3)))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(vehiculoRepository, never()).findByIdAndActivoTrue(any());
        verify(alquilerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devolver un alquiler finalizado lanza IllegalStateException")
    void devolverDosVeces() {
        Auto auto = new Auto("ABC-123", "Toyota", "Yaris", 2022, new BigDecimal("120"), 4);
        Alquiler alquiler = Alquiler.iniciar(luis, auto, 3);
        when(alquilerRepository.findById(5L)).thenReturn(Optional.of(alquiler));

        alquilerService.devolver(5L, new BigDecimal("350"), null);

        assertThat(auto.estaDisponible()).isTrue();
        assertThat(auto.getKilometraje()).isEqualByComparingTo("350.00");
        assertThatThrownBy(() -> alquilerService.devolver(5L, new BigDecimal("10"), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
