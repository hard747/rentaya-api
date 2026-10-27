package com.rentaya.rentaya_api.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.rentaya.rentaya_api.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API sin estado: cada petición trae un JWT firmado con HMAC-SHA256 en el header Authorization.
 * El claim "rol" se convierte en la autoridad ROLE_ADMIN, ROLE_EMPLEADO o ROLE_CLIENTE.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String EMPLEADO = "EMPLEADO";
    private static final String CLIENTE = "CLIENTE";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público: login, registro, salud, documentación y catálogo de vehículos
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        // Spring reenvía los errores (por ejemplo, una ruta inexistente) a /error: si no fuera
                        // pública, un 404 llegaría al cliente como un 401 engañoso.
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos/estadisticas/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos/*/mantenimiento").hasAnyRole(ADMIN, EMPLEADO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos", "/api/v1/vehiculos/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/vehiculos/*/kilometros").hasAnyRole(ADMIN, EMPLEADO)
                        .requestMatchers("/api/v1/vehiculos/**").hasRole(ADMIN)
                        // Alquileres: el cliente solo opera sobre los suyos (se valida en el controlador)
                        .requestMatchers("/api/v1/alquileres/**").hasAnyRole(ADMIN, EMPLEADO, CLIENTE)
                        // Los clientes califican a los conductores; la empresa califica al taller
                        .requestMatchers(HttpMethod.POST, "/api/v1/empleados/*/calificaciones").hasAnyRole(ADMIN, CLIENTE)
                        .requestMatchers(HttpMethod.POST, "/api/v1/empleados/*/viajes",
                                "/api/v1/empleados/*/horas-extra").hasAnyRole(ADMIN, EMPLEADO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/empleados/*/revisiones/*").hasAnyRole(ADMIN, EMPLEADO)
                        .requestMatchers("/api/v1/clientes/**").hasAnyRole(ADMIN, EMPLEADO)
                        .requestMatchers("/api/v1/empleados/**", "/api/v1/talleres/**", "/api/v1/pagos/**",
                                "/api/v1/usuarios/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(objectMapper, res, HttpStatus.UNAUTHORIZED,
                                        "Falta el token o no es válido", req.getRequestURI())))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(objectMapper, res, HttpStatus.UNAUTHORIZED,
                                        "Falta el token o no es válido", req.getRequestURI()))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(objectMapper, res, HttpStatus.FORBIDDEN,
                                        "No tienes permiso para esta operación", req.getRequestURI())))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());
        return http.build();
    }

    private static void escribirError(ObjectMapper objectMapper, jakarta.servlet.http.HttpServletResponse res,
                                      HttpStatus status, String mensaje, String path) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(res.getOutputStream(),
                new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, path, null));
    }

    /**
     * CORS: el frontend vive en otro dominio, así que el navegador solo le deja llamar a la API
     * si esta responde con su origen en Access-Control-Allow-Origin. Los orígenes vienen de CORS_ORIGENES.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${rentaya.cors.origenes:http://localhost:5173}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Location"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
