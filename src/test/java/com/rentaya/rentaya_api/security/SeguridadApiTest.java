package com.rentaya.rentaya_api.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Login, registro, tokens JWT reales y permisos por rol (ADMIN, EMPLEADO, CLIENTE). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadApiTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions post(String url, String json, String token) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return mockMvc.perform(req);
    }

    private ResultActions get(String url, String token) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url);
        if (token != null) {
            req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return mockMvc.perform(req);
    }

    private String token(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.token");
    }

    private String loginAdmin() throws Exception {
        return token(post("/api/v1/auth/login", "{\"username\":\"admin\",\"password\":\"admin-pruebas-123\"}", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN")));
    }

    private long idDe(ResultActions resultado) throws Exception {
        return ((Number) JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    @DisplayName("Sin token: el catálogo es público, pero crear o ver estadísticas devuelve 401")
    void sinToken() throws Exception {
        get("/api/v1/vehiculos", null).andExpect(status().isOk());
        get("/actuator/health", null).andExpect(status().isOk());
        post("/api/v1/vehiculos", "{}", null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        get("/api/v1/vehiculos/estadisticas/por-marca", null).andExpect(status().isUnauthorized());
        get("/api/v1/clientes", "token-falso").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login: credenciales correctas devuelven un JWT; incorrectas devuelven 401")
    void login() throws Exception {
        String admin = loginAdmin();
        post("/api/v1/vehiculos", """
                {"tipo":"AUTO","placa":"SEG-001","marca":"Kia","modelo":"Rio","anio":2021,
                 "tarifaDiariaBase":90,"numeroPuertas":4}""", admin).andExpect(status().isCreated());
        post("/api/v1/auth/login", "{\"username\":\"admin\",\"password\":\"incorrecta\"}", null)
                .andExpect(status().isUnauthorized());
        post("/api/v1/auth/login", "{\"username\":\"nadie\",\"password\":\"incorrecta\"}", null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un CLIENTE alquila a su nombre, ve solo lo suyo y no puede administrar")
    void permisosDeCliente() throws Exception {
        String admin = loginAdmin();
        long auto = idDe(post("/api/v1/vehiculos", """
                {"tipo":"AUTO","placa":"SEG-002","marca":"Toyota","modelo":"Yaris","anio":2022,
                 "tarifaDiariaBase":120,"numeroPuertas":4}""", admin));
        long otroCliente = idDe(post("/api/v1/clientes", """
                {"tipo":"CLIENTE","nombre":"Otro Cliente","dni":"11112222","email":"otro@mail.com"}""", admin));
        long conductor = idDe(post("/api/v1/empleados", """
                {"tipo":"CONDUCTOR","nombre":"Carlos Ruiz","dni":"40123456","sueldoBase":2000,
                 "aniosAntiguedad":5,"licencia":"C"}""", admin));

        String ana = token(post("/api/v1/auth/registro", """
                {"username":"anatorres","password":"clave-segura-1","nombre":"Ana Torres",
                 "dni":"45678912","email":"ana@mail.com"}""", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE")));
        String clientes = get("/api/v1/clientes", admin).andReturn().getResponse().getContentAsString();
        long idAna = ((Number) JsonPath.<java.util.List<Object>>read(clientes,
                "$.content[?(@.dni == '45678912')].id").getFirst()).longValue();

        // Puede alquilar a su nombre, pero no a nombre de otro cliente
        post("/api/v1/alquileres", "{\"clienteId\":" + idAna + ",\"vehiculoId\":" + auto + ",\"dias\":3}", ana)
                .andExpect(status().isCreated());
        post("/api/v1/alquileres", "{\"clienteId\":" + otroCliente + ",\"vehiculoId\":" + auto + ",\"dias\":3}", ana)
                .andExpect(status().isForbidden());

        // Solo ve sus alquileres, aunque pida los de otro
        get("/api/v1/alquileres?clienteId=" + otroCliente, ana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].clienteId", everyItem(is((int) idAna))));

        // Puede calificar a un conductor, pero no administrar
        post("/api/v1/empleados/" + conductor + "/calificaciones", "{\"estrellas\":5}", ana)
                .andExpect(status().isOk());
        post("/api/v1/vehiculos", "{}", ana).andExpect(status().isForbidden());
        get("/api/v1/vehiculos/estadisticas/por-marca", ana).andExpect(status().isForbidden());
        get("/api/v1/clientes", ana).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un EMPLEADO registra kilómetros, pero no crea usuarios ni vehículos")
    void permisosDeEmpleado() throws Exception {
        String admin = loginAdmin();
        post("/api/v1/usuarios", "{\"username\":\"pedro\",\"password\":\"clave-segura-2\",\"rol\":\"EMPLEADO\"}", admin)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("EMPLEADO"));
        long camion = idDe(post("/api/v1/vehiculos", """
                {"tipo":"CAMION","placa":"SEG-003","marca":"Volvo","modelo":"FH","anio":2020,
                 "tarifaDiariaBase":400,"capacidadToneladas":12}""", admin));

        String pedro = token(post("/api/v1/auth/login", "{\"username\":\"pedro\",\"password\":\"clave-segura-2\"}", null)
                .andExpect(jsonPath("$.rol").value("EMPLEADO")));
        post("/api/v1/vehiculos/" + camion + "/kilometros", "{\"kilometros\":100}", pedro).andExpect(status().isOk());
        get("/api/v1/vehiculos/" + camion + "/mantenimiento", pedro).andExpect(status().isOk());
        post("/api/v1/usuarios", "{\"username\":\"otro\",\"password\":\"clave-segura-3\",\"rol\":\"ADMIN\"}", pedro)
                .andExpect(status().isForbidden());
        post("/api/v1/vehiculos", "{}", pedro).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("La documentación OpenAPI y Swagger UI son públicas")
    void documentacion() throws Exception {
        get("/v3/api-docs", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("RentaYa API"))
                .andExpect(jsonPath("$.paths['/api/v1/alquileres']").exists());
        get("/swagger-ui/index.html", null).andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS: el navegador acepta al frontend autorizado y rechaza otros orígenes")
    void cors() throws Exception {
        mockMvc.perform(options("/api/v1/alquileres")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        mockMvc.perform(options("/api/v1/alquileres")
                        .header(HttpHeaders.ORIGIN, "https://sitio-ajeno.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("La raíz redirige a Swagger UI")
    void raiz() throws Exception {
        get("/", null)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui.html"));
    }

    @Test
    @DisplayName("Registro con datos repetidos o inválidos")
    void registroInvalido() throws Exception {
        String registro = """
                {"username":"anatorres","password":"clave-segura-1","nombre":"Ana Torres",
                 "dni":"45678912","email":"ana@mail.com"}""";
        post("/api/v1/auth/registro", registro, null).andExpect(status().isCreated());
        post("/api/v1/auth/registro", registro, null).andExpect(status().isConflict());
        post("/api/v1/auth/registro", "{\"username\":\"x\",\"password\":\"corta\"}", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.password").exists());
    }
}
