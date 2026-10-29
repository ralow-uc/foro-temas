package com.foro.temas.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Utilidad de pruebas que firma tokens igual que lo hace foro-usuarios.
 *
 * Permite probar la autorizacion de foro-temas de punta a punta sin levantar el
 * otro microservicio: lo unico que comparten es el secreto, que es justo la
 * premisa del diseno.
 */
@Component
public class GeneradorDeTokensDePrueba {

    private final SecretKey clave;

    public GeneradorDeTokensDePrueba(@Value("${foro.jwt.secreto}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /** Token valido con la cabecera lista para usar en Authorization. */
    public String para(Long id, String nombreUsuario, String... roles) {
        Instant ahora = Instant.now();
        return "Bearer " + Jwts.builder()
                .subject(nombreUsuario)
                .claim("uid", id)
                .claim("roles", List.of(roles))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(3600)))
                .signWith(clave)
                .compact();
    }

    /** Token firmado con otro secreto, para comprobar que se rechaza. */
    public String conFirmaInvalida(String nombreUsuario) {
        SecretKey otraClave = Keys.hmacShaKeyFor(
                "un-secreto-totalmente-distinto-para-la-prueba-de-firma".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(nombreUsuario)
                .claim("uid", 1L)
                .claim("roles", List.of("ADMIN"))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(otraClave)
                .compact();
    }
}
