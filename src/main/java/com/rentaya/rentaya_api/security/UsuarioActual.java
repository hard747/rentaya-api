package com.rentaya.rentaya_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Lee del token autenticado si el usuario es un cliente y, en ese caso, su id de cliente. */
public final class UsuarioActual {

    private UsuarioActual() { }

    /** Devuelve el id de cliente si quien llama tiene rol CLIENTE, o null si es ADMIN o EMPLEADO. */
    public static Long clienteId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token
                && token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            Jwt jwt = token.getToken();
            Object id = jwt.getClaims().get(TokenService.CLAIM_CLIENTE_ID);
            return id instanceof Number n ? n.longValue() : null;
        }
        return null;
    }
}
