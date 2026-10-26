package com.rentaya.rentaya_api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los 12 escenarios de la Etapa 1 contra una base H2 en memoria.
 * Cada test corre en una transacción que se revierte al final.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VehiculoControllerTest {

    private static final String URL = "/api/v1/vehiculos";

    @Autowired
    private MockMvc mockMvc;

    private String auto(String placa) {
        return """
                {"tipo":"AUTO","placa":"%s","marca":"Toyota","modelo":"Corolla",
                 "anio":2022,"tarifaDiariaBase":120.00,"numeroPuertas":4}
                """.formatted(placa);
    }

    private String moto(String placa) {
        return """
                {"tipo":"MOTO","placa":"%s","marca":"Honda","modelo":"CB 500",
                 "anio":2023,"tarifaDiariaBase":80.00,"cilindrada":500,"incluyeCasco":true}
                """.formatted(placa);
    }

    private String camion(String placa) {
        return """
                {"tipo":"CAMION","placa":"%s","marca":"Volvo","modelo":"FH 540",
                 "anio":2021,"tarifaDiariaBase":450.00,"capacidadToneladas":25.5}
                """.formatted(placa);
    }

    private long crear(String json) throws Exception {
        MvcResult resultado = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        String location = resultado.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    @Test
    @DisplayName("1. Crear un auto devuelve 201, Location y kilometraje 0.00")
    void crearAuto() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(auto("AAA-111")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(URL + "/")))
                .andExpect(jsonPath("$.tipo").value("AUTO"))
                .andExpect(jsonPath("$.numeroPuertas").value(4))
                .andExpect(content().string(containsString("\"kilometraje\":0.00")));
    }

    @Test
    @DisplayName("2. Crear una moto devuelve 201 con sus campos propios")
    void crearMoto() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(moto("MMM-222")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("MOTO"))
                .andExpect(jsonPath("$.cilindrada").value(500))
                .andExpect(jsonPath("$.incluyeCasco").value(true))
                .andExpect(jsonPath("$.numeroPuertas").doesNotExist());
    }

    @Test
    @DisplayName("3. Crear un camión devuelve 201 con su capacidad")
    void crearCamion() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(camion("CCC-333")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CAMION"))
                .andExpect(jsonPath("$.capacidadToneladas").value(25.5));
    }

    @Test
    @DisplayName("4. Datos inválidos devuelven 400 con el detalle por campo")
    void crearInvalido() throws Exception {
        String json = """
                {"tipo":"AUTO","placa":"abc123","marca":"","modelo":"Corolla",
                 "anio":1980,"tarifaDiariaBase":-10,"numeroPuertas":9}
                """;
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.placa").exists())
                .andExpect(jsonPath("$.errores.marca").exists())
                .andExpect(jsonPath("$.errores.anio").exists())
                .andExpect(jsonPath("$.errores.tarifaDiariaBase").exists())
                .andExpect(jsonPath("$.errores.numeroPuertas").exists());
    }

    @Test
    @DisplayName("5. Un tipo de vehículo inexistente devuelve 400")
    void crearTipoInexistente() throws Exception {
        String json = """
                {"tipo":"AVION","placa":"AVN-001","marca":"Embraer","modelo":"E195",
                 "anio":2020,"tarifaDiariaBase":9000}
                """;
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Una placa duplicada devuelve 409")
    void crearDuplicado() throws Exception {
        crear(auto("DUP-001"));
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(moto("DUP-001")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("DUP-001")));
    }

    @Test
    @DisplayName("7. Obtener por id devuelve 200; un id inexistente devuelve 404")
    void obtenerPorId() throws Exception {
        long id = crear(auto("GET-001"));
        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placa").value("GET-001"));
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("8. Listar con ?tipo=AUTO devuelve solo autos; un tipo inválido devuelve 400")
    void listarPorTipo() throws Exception {
        crear(auto("LIS-001"));
        crear(moto("LIS-002"));
        crear(camion("LIS-003"));
        mockMvc.perform(get(URL).param("tipo", "AUTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", everyItem(org.hamcrest.Matchers.is("AUTO"))))
                .andExpect(jsonPath("$[*].placa", hasItem("LIS-001")));
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].placa", hasItem("LIS-003")));
        mockMvc.perform(get(URL).param("tipo", "AVION"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("9. Cambiar la tarifa devuelve 200; una tarifa no positiva devuelve 400")
    void actualizarTarifa() throws Exception {
        long id = crear(auto("TAR-001"));
        mockMvc.perform(put(URL + "/" + id + "/tarifa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarifaDiariaBase\": 150}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"tarifaDiariaBase\":150.00")));
        mockMvc.perform(put(URL + "/" + id + "/tarifa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarifaDiariaBase\": -5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("10. Sumar kilómetros acumula el kilometraje; 0 km devuelve 400")
    void registrarKilometros() throws Exception {
        long id = crear(moto("KMS-001"));
        mockMvc.perform(post(URL + "/" + id + "/kilometros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometros\": 150.5}"))
                .andExpect(status().isOk());
        mockMvc.perform(post(URL + "/" + id + "/kilometros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometros\": 49.5}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"kilometraje\":200.00")));
        mockMvc.perform(post(URL + "/" + id + "/kilometros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kilometros\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("11. El costo de mantenimiento usa la fórmula de cada tipo")
    void mantenimiento() throws Exception {
        long idAuto = crear(auto("MAN-001"));
        mockMvc.perform(post(URL + "/" + idAuto + "/kilometros").contentType(MediaType.APPLICATION_JSON)
                .content("{\"kilometros\": 1000}"));
        // Auto: 100 + 1000 * 0.05 = 150.00
        mockMvc.perform(get(URL + "/" + idAuto + "/mantenimiento"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"costoMantenimiento\":150.00")));

        long idCamion = crear(camion("MAN-002"));
        // Camión sin km: 300 + 0 * 0.10 + 25.5 * 20 = 810.00
        mockMvc.perform(get(URL + "/" + idCamion + "/mantenimiento"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"costoMantenimiento\":810.00")));
    }

    @Test
    @DisplayName("12. El borrado lógico devuelve 204 y el vehículo deja de existir para la API")
    void borradoLogico() throws Exception {
        long id = crear(auto("DEL-001"));
        mockMvc.perform(delete(URL + "/" + id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(URL))
                .andExpect(jsonPath("$[*].placa", not(hasItem("DEL-001"))));
        mockMvc.perform(delete(URL + "/" + id))
                .andExpect(status().isNotFound());
    }
}
