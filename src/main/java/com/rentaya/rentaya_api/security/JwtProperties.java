package com.rentaya.rentaya_api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * rentaya.jwt.secret: clave HMAC de al menos 32 caracteres (variable de entorno JWT_SECRET).
 * rentaya.jwt.expiracion-minutos: validez del token.
 */
@ConfigurationProperties(prefix = "rentaya.jwt")
public record JwtProperties(String secret, long expiracionMinutos) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("rentaya.jwt.secret (JWT_SECRET) debe tener al menos 32 caracteres");
        }
        if (expiracionMinutos <= 0) {
            expiracionMinutos = 60;
        }
    }
}
