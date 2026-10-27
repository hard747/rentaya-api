package com.rentaya.rentaya_api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** El ejercicio de RentaYa recorrido de punta a punta por la API, con los valores esperados del enunciado. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class FlujoRentaYaApiTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long crear(String url, String json) throws Exception {
        String body = postJson(url, json).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    @DisplayName("Ejercicio completo: alquileres, revisiones, pagos y calificaciones por la API")
    void ejercicioCompleto() throws Exception {
        long auto = crear("/api/v1/vehiculos", """
                {"tipo":"AUTO","placa":"ABC-123","marca":"Toyota","modelo":"Yaris","anio":2022,
                 "tarifaDiariaBase":120,"numeroPuertas":4}""");
        long moto = crear("/api/v1/vehiculos", """
                {"tipo":"MOTO","placa":"XYZ-789","marca":"Honda","modelo":"CB190R","anio":2023,
                 "tarifaDiariaBase":60,"cilindrada":190,"incluyeCasco":true}""");
        long camion = crear("/api/v1/vehiculos", """
                {"tipo":"CAMION","placa":"CAM-001","marca":"Volvo","modelo":"FH","anio":2020,
                 "tarifaDiariaBase":400,"capacidadToneladas":12}""");
        long ana = crear("/api/v1/clientes", """
                {"tipo":"CLIENTE","nombre":"Ana Torres","dni":"45678912","email":"ana@mail.com"}""");
        long luis = crear("/api/v1/clientes", """
                {"tipo":"CLIENTE_PREMIUM","nombre":"Luis Gómez","dni":"71234567","email":"luis@mail.com",
                 "porcentajeDescuento":20}""");
        long conductor = crear("/api/v1/empleados", """
                {"tipo":"CONDUCTOR","nombre":"Carlos Ruiz","dni":"40123456","sueldoBase":2000,
                 "aniosAntiguedad":5,"licencia":"C"}""");
        long mecanica = crear("/api/v1/empleados", """
                {"tipo":"MECANICO","nombre":"María Quispe","dni":"42345678","sueldoBase":2200,
                 "aniosAntiguedad":2,"especialidad":"Motores"}""");
        long taller = crear("/api/v1/talleres", """
                {"razonSocial":"AutoFix SAC","ruc":"20123456789"}""");

        // Paso 2: cotizaciones (Ana 3 días 360.00, Luis 7 días 604.80)
        mockMvc.perform(get("/api/v1/alquileres/cotizacion")
                        .param("clienteId", "" + ana).param("vehiculoId", "" + auto).param("dias", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"costoTotal\":360.00")));
        mockMvc.perform(get("/api/v1/alquileres/cotizacion")
                        .param("clienteId", "" + luis).param("vehiculoId", "" + auto).param("dias", "7"))
                .andExpect(content().string(containsString("\"costoTotal\":604.80")));

        // Paso 3: alquilar y devolver el auto con 350 km y la moto con 120 km; 1000 km al camión
        String alquilerAuto = postJson("/api/v1/alquileres",
                "{\"clienteId\":" + ana + ",\"vehiculoId\":" + auto + ",\"dias\":3}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andReturn().getResponse().getContentAsString();
        long idAlquilerAuto = ((Number) JsonPath.read(alquilerAuto, "$.id")).longValue();

        // Paso 8: alquilar el mismo auto otra vez -> 409; dar de baja un vehículo alquilado -> 409
        postJson("/api/v1/alquileres", "{\"clienteId\":" + luis + ",\"vehiculoId\":" + auto + ",\"dias\":2}")
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/vehiculos/" + auto)).andExpect(status().isConflict());

        postJson("/api/v1/alquileres/" + idAlquilerAuto + "/devolucion", "{\"kmRecorridos\":350}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"));
        postJson("/api/v1/alquileres/" + idAlquilerAuto + "/devolucion", "{\"kmRecorridos\":10}")
                .andExpect(status().isConflict());

        String alquilerMoto = postJson("/api/v1/alquileres",
                "{\"clienteId\":" + luis + ",\"vehiculoId\":" + moto + ",\"dias\":1}")
                .andReturn().getResponse().getContentAsString();
        postJson("/api/v1/alquileres/" + JsonPath.read(alquilerMoto, "$.id") + "/devolucion",
                "{\"kmRecorridos\":120}").andExpect(status().isOk());
        postJson("/api/v1/vehiculos/" + camion + "/kilometros", "{\"kilometros\":1000}").andExpect(status().isOk());

        // Los camiones no se alquilan
        postJson("/api/v1/alquileres", "{\"clienteId\":" + ana + ",\"vehiculoId\":" + camion + ",\"dias\":2}")
                .andExpect(status().isBadRequest());

        // Paso 4: revisiones de la mecánica (117.50, 53.60 y 640.00)
        String revisiones = "/api/v1/empleados/" + mecanica + "/revisiones/";
        mockMvc.perform(get(revisiones + auto)).andExpect(content().string(containsString("117.50")));
        mockMvc.perform(get(revisiones + moto)).andExpect(content().string(containsString("53.60")));
        mockMvc.perform(get(revisiones + camion)).andExpect(content().string(containsString("640.00")));

        // Paso 5: pagos (conductor 2450.00, mecánica 2528.00, taller 1180.00)
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/empleados/" + conductor + "/viajes")).andExpect(status().isOk());
        }
        postJson("/api/v1/empleados/" + mecanica + "/horas-extra", "{\"horas\":8}").andExpect(status().isOk());
        postJson("/api/v1/talleres/" + taller + "/facturas", "{\"monto\":1000}").andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/empleados/" + conductor + "/pago"))
                .andExpect(content().string(containsString("\"monto\":2450.00")));
        mockMvc.perform(get("/api/v1/empleados/" + mecanica + "/pago"))
                .andExpect(content().string(containsString("\"monto\":2528.00")));
        mockMvc.perform(get("/api/v1/talleres/" + taller + "/pago"))
                .andExpect(content().string(containsString("\"monto\":1180.00")))
                .andExpect(jsonPath("$.identificador").value("20123456789"));

        // Paso 6: calificaciones (conductor 4.67, taller 3.50); los mecánicos no se califican
        for (int estrellas : new int[] {5, 4, 5}) {
            postJson("/api/v1/empleados/" + conductor + "/calificaciones", "{\"estrellas\":" + estrellas + "}")
                    .andExpect(status().isOk());
        }
        postJson("/api/v1/talleres/" + taller + "/calificaciones", "{\"estrellas\":3}");
        postJson("/api/v1/talleres/" + taller + "/calificaciones", "{\"estrellas\":4}")
                .andExpect(content().string(containsString("\"promedioCalificacion\":3.50")));
        mockMvc.perform(get("/api/v1/empleados/" + conductor))
                .andExpect(content().string(containsString("\"promedioCalificacion\":4.67")));
        postJson("/api/v1/empleados/" + mecanica + "/calificaciones", "{\"estrellas\":5}")
                .andExpect(status().isBadRequest());
        postJson("/api/v1/empleados/" + conductor + "/calificaciones", "{\"estrellas\":6}")
                .andExpect(status().isBadRequest());

        // Paso 7: mostrarInfo de cada persona
        mockMvc.perform(get("/api/v1/clientes/" + luis))
                .andExpect(jsonPath("$.info").value("[CLIENTE PREMIUM] Luis Gómez - DNI 71234567"));
        mockMvc.perform(get("/api/v1/empleados/" + conductor))
                .andExpect(jsonPath("$.info").value("[CONDUCTOR] Carlos Ruiz - DNI 40123456"));
    }

    @Test
    @DisplayName("Validaciones y conflictos de personas y talleres")
    void validaciones() throws Exception {
        crear("/api/v1/clientes", """
                {"tipo":"CLIENTE","nombre":"Ana Torres","dni":"45678912","email":"ana@mail.com"}""");
        // DNI repetido entre cliente y empleado -> 409
        postJson("/api/v1/empleados", """
                {"tipo":"MECANICO","nombre":"Otra","dni":"45678912","sueldoBase":1000,
                 "aniosAntiguedad":0,"especialidad":"Frenos"}""").andExpect(status().isConflict());
        postJson("/api/v1/clientes", """
                {"tipo":"CLIENTE_PREMIUM","nombre":"X","dni":"123","email":"no-es-email",
                 "porcentajeDescuento":45}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.dni").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.porcentajeDescuento").exists());
        postJson("/api/v1/empleados", """
                {"tipo":"CONDUCTOR","nombre":"X","dni":"11111111","sueldoBase":1000,
                 "aniosAntiguedad":0,"licencia":"Z"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.licencia").exists());
        postJson("/api/v1/talleres", "{\"razonSocial\":\"X\",\"ruc\":\"123\"}")
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/clientes/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/alquileres/cotizacion").param("clienteId", "1").param("vehiculoId", "1"))
                .andExpect(status().isBadRequest());
    }
}
