package com.rentaya.rentaya_api.repository;

import com.rentaya.rentaya_api.model.Alquiler;
import com.rentaya.rentaya_api.model.Auto;
import com.rentaya.rentaya_api.model.Camion;
import com.rentaya.rentaya_api.model.Cliente;
import com.rentaya.rentaya_api.model.ClientePremium;
import com.rentaya.rentaya_api.model.Moto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repositorios contra un PostgreSQL real en Docker: valida las migraciones de Flyway,
 * las consultas nativas y las restricciones de la base. Se salta solo si no hay Docker.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class RepositoriosPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private VehiculoRepository vehiculoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AlquilerRepository alquilerRepository;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Flyway aplica todas las migraciones sobre PostgreSQL")
    void migraciones() {
        Integer aplicadas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success", Integer.class);
        assertThat(aplicadas).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("TYPE(v) filtra por subtipo y la estadística nativa agrupa por marca")
    void consultasPorTipoYEstadisticas() {
        vehiculoRepository.save(new Auto("AAA-001", "Toyota", "Yaris", 2022, new BigDecimal("120"), 4));
        vehiculoRepository.save(new Moto("MMM-001", "Honda", "CB190R", 2023, new BigDecimal("60"), 190, true));
        vehiculoRepository.save(new Moto("MMM-002", "Honda", "XR150", 2021, new BigDecimal("50"), 150, false));
        vehiculoRepository.save(new Camion("CAM-001", "Volvo", "FH", 2020, new BigDecimal("400"), new BigDecimal("12")));

        assertThat(vehiculoRepository.findActivosPorTipo(Moto.class, Pageable.unpaged())).hasSize(2);
        assertThat(vehiculoRepository.obtenerEstadisticasPorMarca())
                .filteredOn(e -> e.getMarca().equals("Honda"))
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getCantidad()).isEqualTo(2L);
                    assertThat(e.getPromedioCilindrada()).isEqualByComparingTo("170.00");
                });
    }

    @Test
    @DisplayName("Un alquiler guarda sus claves foráneas y el cliente vuelve con su subtipo real")
    void alquilerConRelaciones() {
        Auto auto = vehiculoRepository.save(new Auto("ABC-123", "Toyota", "Yaris", 2022, new BigDecimal("120"), 4));
        Cliente luis = clienteRepository.save(
                new ClientePremium("Luis Gómez", "71234567", "luis@mail.com", new BigDecimal("20")));

        Alquiler guardado = alquilerRepository.saveAndFlush(Alquiler.iniciar(luis, auto, 7));

        assertThat(guardado.getCostoTotal()).isEqualByComparingTo("604.80");
        assertThat(clienteRepository.findById(luis.getId())).get().isInstanceOf(ClientePremium.class);
        assertThat(alquilerRepository.findByClienteIdOrderByFechaInicioDesc(luis.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Las restricciones CHECK de la base rechazan datos inválidos")
    void restricciones() {
        jdbc.update("INSERT INTO talleres_externos (razon_social, ruc) VALUES ('AutoFix SAC', '20123456789')");
        Long tallerId = jdbc.queryForObject("SELECT id FROM talleres_externos WHERE ruc = '20123456789'", Long.class);

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO taller_calificaciones (taller_id, estrellas) VALUES (?, 9)", tallerId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
