package com.foro.temas.dto;

import com.foro.temas.model.Tema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle de un tema: la misma informacion del listado mas sus comentarios.
 */
public record TemaDetalleResponse(
        Long id,
        CategoriaResumen categoria,
        Long autorId,
        String autorNombre,
        String titulo,
        String contenido,
        LocalDateTime fechaCreacion,
        boolean cerrado,
        List<ComentarioResponse> comentarios) {

    public static TemaDetalleResponse desde(Tema tema, List<ComentarioResponse> comentarios) {
        return new TemaDetalleResponse(
                tema.getId(),
                CategoriaResumen.desde(tema.getCategoria()),
                tema.getAutorId(),
                tema.getAutorNombre(),
                tema.getTitulo(),
                tema.getContenido(),
                tema.getFechaCreacion(),
                tema.isCerrado(),
                comentarios);
    }
}
