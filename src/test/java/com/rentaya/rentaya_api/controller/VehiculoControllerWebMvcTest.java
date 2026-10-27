package com.rentaya.rentaya_api.controller;

import com.rentaya.rentaya_api.dto.VehiculoResponse;
import com.rentaya.rentaya_api.exception.RecursoNoEncontradoException;
import com.rentaya.rentaya_api.security.SecurityConfig;
import com.rentaya.rentaya_api.service.VehiculoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Solo la capa web: el servicio está simulado, no se levanta la base de datos. */
@WebMvcTest(VehiculoController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class VehiculoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehiculoService vehiculoService;

    private VehiculoResponse auto() {
        return new VehiculoResponse(7L, "AUTO", "ABC-123", "Toyota", "Yaris", 2022, new BigDecimal("120.00"),
                new BigDecimal("0.00"), "Toyota Yaris (2022) - ABC-123", 4, null, null, null, true);
    }

    @Test
    @DisplayName("POST válido delega en el servicio y devuelve 201 con Location")
    void crear() throws Exception {
        when(vehiculoService.crear(any())).thenReturn(auto());

        mockMvc.perform(post("/api/v1/vehiculos").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tipo":"AUTO","placa":"ABC-123","marca":"Toyota","modelo":"Yaris",
                         "anio":2022,"tarifaDiariaBase":120,"numeroPuertas":4}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/vehiculos/7"))
                .andExpect(jsonPath("$.descripcion").value("Toyota Yaris (2022) - ABC-123"))
                .andExpect(jsonPath("$.cilindrada").doesNotExist());
    }

    @Test
    @DisplayName("POST inválido devuelve 400 sin llamar al servicio")
    void crearInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/vehiculos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"AUTO\",\"placa\":\"mala\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.placa").exists());
        verify(vehiculoService, never()).crear(any());
    }

    @Test
    @DisplayName("La excepción de recurso no encontrado se traduce a 404")
    void noEncontrado() throws Exception {
        when(vehiculoService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe"));

        mockMvc.perform(get("/api/v1/vehiculos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/v1/vehiculos/99"));
    }

    @Test
    @DisplayName("Un id no numérico devuelve 400")
    void idInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/vehiculos/abc")).andExpect(status().isBadRequest());
    }
}
