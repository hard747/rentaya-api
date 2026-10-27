package com.rentaya.rentaya_api.security;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Con Tomcat real: MockMvc no reproduce el reenvío de errores a /error que hace el contenedor,
 * así que este comportamiento solo se puede verificar levantando el servidor.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServidorRealTest {

    @LocalServerPort
    private int puerto;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> enviar(HttpRequest.Builder peticion) throws IOException, InterruptedException {
        return http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder a(String ruta) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta));
    }

    @Test
    @DisplayName("Con un token válido, una URL mal formada responde 400 y no un 401 engañoso")
    void urlMalFormada() throws Exception {
        String login = enviar(a("/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"username\":\"admin\",\"password\":\"admin-pruebas-123\"}"))).body();
        String token = JsonPath.read(login, "$.token");

        // El firewall de Spring Security rechaza "//" y el contenedor reenvía el error a /error.
        HttpResponse<String> respuesta = enviar(a("/api/v1/empleados//viajes")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()));

        assertThat(respuesta.statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("Sin token, una ruta protegida sigue respondiendo 401")
    void sinToken() throws Exception {
        assertThat(enviar(a("/api/v1/clientes")).statusCode()).isEqualTo(401);
    }
}
