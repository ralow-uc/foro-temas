package com.foro.temas.dto;

import com.foro.temas.model.Categoria;

/**
 * Version breve de la categoria para anidarla dentro de un tema, sin repetir
 * la descripcion ni el conteo en cada elemento del listado.
 */
public record CategoriaResumen(Long id, String nombre, String slug) {

    public static CategoriaResumen desde(Categoria categoria) {
        return new CategoriaResumen(categoria.getId(), categoria.getNombre(), categoria.getSlug());
    }
}
