package com.foro.temas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Valida los JWT que emite foro-usuarios.
 *
 * Este microservicio no emite tokens ni consulta la tabla USUARIO: comparte con
 * foro-usuarios el secreto de firma y con eso comprueba por si mismo que el token
 * es autentico y esta vigente. Asi no hay una llamada de red por peticion ni
 * acoplamiento entre los dos servicios.
 */
@Component
public class LectorDeJwt {

    private static final String CLAIM_ID = "uid";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey clave;

    public LectorDeJwt(@Value("${foro.jwt.secreto}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Comprueba la firma y la vigencia del token y devuelve la identidad que
     * lleva dentro. Lanza JwtException si el token es invalido o esta caduco.
     */
    public UsuarioAutenticado leer(String token) throws JwtException {
        Claims contenido = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new UsuarioAutenticado(
                contenido.get(CLAIM_ID, Number.class).longValue(),
                contenido.getSubject(),
                extraerRoles(contenido));
    }

    @SuppressWarnings("unchecked")
    private List<String> extraerRoles(Claims contenido) {
        List<String> roles = (List<String>) contenido.get(CLAIM_ROLES, List.class);
        return roles == null ? List.of() : roles;
    }
}
