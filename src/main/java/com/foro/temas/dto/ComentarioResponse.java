package com.foro.temas.dto;

import com.foro.temas.model.Comentario;

import java.time.LocalDateTime;

/**
 * Comentario devuelto por el microservicio.
 */
public record ComentarioResponse(
        Long id,
        Long temaId,
        Long autorId,
        String autorNombre,
        String contenido,
        LocalDateTime fechaCreacion) {

    public static ComentarioResponse desde(Comentario comentario) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getTema().getId(),
                comentario.getAutorId(),
                comentario.getAutorNombre(),
                comentario.getContenido(),
                comentario.getFechaCreacion());
    }
}
