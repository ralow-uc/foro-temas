package com.foro.temas.dto;

import com.foro.temas.model.Categoria;

/**
 * Categoria devuelta por el microservicio, con la cantidad de temas que agrupa.
 */
public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion,
        String slug,
        long cantidadDeTemas) {

    public static CategoriaResponse desde(Categoria categoria, long cantidadDeTemas) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getSlug(),
                cantidadDeTemas);
    }
}
