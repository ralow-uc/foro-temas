package com.foro.temas.dto;

import com.foro.temas.model.Tema;

import java.time.LocalDateTime;

/**
 * Tema devuelto en los listados y en las busquedas.
 */
public record TemaResponse(
        Long id,
        CategoriaResumen categoria,
        Long autorId,
        String autorNombre,
        String titulo,
        String contenido,
        LocalDateTime fechaCreacion,
        boolean cerrado,
        long cantidadDeComentarios) {

    public static TemaResponse desde(Tema tema, long cantidadDeComentarios) {
        return new TemaResponse(
                tema.getId(),
                CategoriaResumen.desde(tema.getCategoria()),
                tema.getAutorId(),
                tema.getAutorNombre(),
                tema.getTitulo(),
                tema.getContenido(),
                tema.getFechaCreacion(),
                tema.isCerrado(),
                cantidadDeComentarios);
    }
}
