package com.rentaya.rentaya_api.security;

import com.rentaya.rentaya_api.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Emite los JWT: sub = usuario, rol = su rol y, para clientes, clienteId. */
@Service
public class TokenService {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_CLIENTE_ID = "clienteId";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("rentaya-api")
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(properties.expiracionMinutos(), ChronoUnit.MINUTES))
                .claim(CLAIM_ROL, List.of(usuario.getRol().name()));
        if (usuario.getCliente() != null) {
            claims.claim(CLAIM_CLIENTE_ID, usuario.getCliente().getId());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long expiracionSegundos() {
        return properties.expiracionMinutos() * 60;
    }
}
