package com.foro.temas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Comentario que un usuario autenticado publica dentro de un tema.
 */
public record ComentarioRequest(

        @NotBlank(message = "El comentario no puede estar vacio")
        @Size(min = 2, max = 2000, message = "El comentario debe tener entre 2 y 2000 caracteres")
        String contenido) {
}
