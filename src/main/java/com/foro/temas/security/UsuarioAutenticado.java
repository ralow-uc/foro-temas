package com.foro.temas.security;

import java.util.List;

/**
 * Identidad del usuario que viene dentro del JWT emitido por foro-usuarios.
 *
 * foro-temas no consulta la tabla USUARIO ni llama al otro microservicio: todo lo
 * que necesita saber del autor viaja firmado en el token.
 */
public record UsuarioAutenticado(Long id, String nombreUsuario, List<String> roles) {

    public static final String ROL_ADMIN = "ADMIN";

    /** Un ADMIN puede intervenir sobre contenido ajeno. */
    public boolean esAdmin() {
        return roles != null && roles.contains(ROL_ADMIN);
    }
}
