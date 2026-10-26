package com.rentaya.rentaya_api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reproduce la clase Main del ejercicio POO de RentaYa y compara con sus valores esperados.
 * Prueba solo el dominio, sin Spring ni base de datos.
 */
class EjercicioRentaYaTest {

    private Auto auto;
    private Moto moto;
    private Camion camion;
    private Cliente ana;
    private ClientePremium luis;
    private Conductor conductor;
    private Mecanico mecanica;
    private TallerExterno taller;

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    @BeforeEach
    void crearObjetos() {
        auto = new Auto("ABC-123", "Toyota", "Yaris", 2022, bd("120"), 4);
        moto = new Moto("XYZ-789", "Honda", "CB190R", 2023, bd("60"), 190, true);
        camion = new Camion("CAM-001", "Volvo", "FH", 2020, bd("400"), bd("12"));
        ana = new Cliente("Ana Torres", "45678912", "ana@mail.com");
        luis = new ClientePremium("Luis Gómez", "71234567", "luis@mail.com", bd("20"));
        conductor = new Conductor("Carlos Ruiz", "40123456", bd("2000"), 5, 'C');
        mecanica = new Mecanico("María Quispe", "42345678", bd("2200"), 2, "Motores");
        taller = new TallerExterno("AutoFix SAC", "20123456789");
    }

    @Test
    @DisplayName("Paso 1: alquiler de 7 días -> auto 756.00, moto 371.00")
    void paso1() {
        List<Alquilable> alquilables = List.of(auto, moto);
        assertThat(alquilables).extracting(a -> a.calcularCostoAlquiler(7))
                .containsExactly(bd("756.00"), bd("371.00"));
    }

    @Test
    @DisplayName("Paso 2: Ana por el auto 3 días 360.00, Luis por el auto 7 días 604.80")
    void paso2() {
        assertThat(ana.calcularTotal(auto, 3)).isEqualByComparingTo("360.00");
        assertThat(luis.calcularTotal(auto, 7)).isEqualByComparingTo("604.80");
    }

    @Test
    @DisplayName("Pasos 3 y 4: tras alquilar y devolver, la revisión -> 117.50, 53.60 y 640.00")
    void pasos3y4() {
        auto.alquilar();
        auto.devolver(bd("350"));
        moto.alquilar();
        moto.devolver(bd("120"));
        camion.registrarKilometros(bd("1000"));

        List<Vehiculo> vehiculos = List.of(auto, moto, camion);
        assertThat(vehiculos).extracting(mecanica::revisar)
                .containsExactly(bd("117.50"), bd("53.60"), bd("640.00"));
        assertThat(auto.estaDisponible()).isTrue();
    }

    @Test
    @DisplayName("Paso 5: pagos -> conductor 2450.00, mecánica 2528.00, taller 1180.00, total 6158.00")
    void paso5() {
        for (int i = 0; i < 10; i++) {
            conductor.registrarViaje();
        }
        mecanica.registrarHorasExtra(8);
        taller.registrarFactura(bd("1000"));

        List<Pagable> pagables = List.of(conductor, mecanica, taller);
        assertThat(pagables).extracting(Pagable::calcularPago)
                .containsExactly(bd("2450.00"), bd("2528.00"), bd("1180.00"));
        BigDecimal total = pagables.stream().map(Pagable::calcularPago).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("6158.00");
        assertThat(taller.getIdentificadorPago()).isEqualTo("20123456789");
        assertThat(conductor.getIdentificadorPago()).isEqualTo("40123456");
    }

    @Test
    @DisplayName("Paso 6: promedios -> conductor 4.67, taller 3.50")
    void paso6() {
        conductor.calificar(5);
        conductor.calificar(4);
        conductor.calificar(5);
        taller.calificar(3);
        taller.calificar(4);

        assertThat(conductor.getPromedioCalificacion()).isEqualByComparingTo("4.67");
        assertThat(taller.getPromedioCalificacion()).isEqualByComparingTo("3.50");
        assertThat(mecanica).isNotInstanceOf(Calificable.class);
    }

    @Test
    @DisplayName("Paso 7: mostrarInfo funciona igual para las cuatro personas")
    void paso7() {
        List<Persona> personas = List.of(ana, luis, conductor, mecanica);
        assertThat(personas).extracting(Persona::mostrarInfo).containsExactly(
                "[CLIENTE] Ana Torres - DNI 45678912",
                "[CLIENTE PREMIUM] Luis Gómez - DNI 71234567",
                "[CONDUCTOR] Carlos Ruiz - DNI 40123456",
                "[MECANICO] María Quispe - DNI 42345678");
    }

    @Test
    @DisplayName("Paso 8: alquilar dos veces y calificar con 6 lanzan excepción")
    void paso8() {
        auto.alquilar();
        assertThatThrownBy(auto::alquilar).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> conductor.calificar(6)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Validaciones: DNI, RUC, licencia, descuento, puertas y días")
    void validaciones() {
        assertThatThrownBy(() -> new Cliente("X", "123", "x@mail.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TallerExterno("X", "123")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Conductor("X", "11111111", bd("1000"), 0, 'D'))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ClientePremium("X", "11111111", "x@mail.com", bd("31")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Auto("AAA-111", "M", "N", 2020, bd("10"), 6))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> auto.calcularCostoAlquiler(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> moto.devolver(bd("10"))).isInstanceOf(IllegalStateException.class);
        assertThat(camion).isNotInstanceOf(Alquilable.class);
    }
}
