package com.rentaya.rentaya_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación en /swagger-ui.html. El botón "Authorize" recibe el token de /api/v1/auth/login. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI rentayaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RentaYa API")
                        .version("v1")
                        .description("Gestão de frota, clientes, aluguéis, pagamentos e avaliações de uma "
                                + "concessionária de veículos."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
