package com.foro.temas.repository;

/**
 * Proyeccion del resultado de contar los temas agrupados por categoria.
 *
 * Spring Data rellena esta interfaz con los alias de la consulta JPQL, de modo
 * que el conteo de las seis categorias se obtiene en una sola consulta en vez
 * de una por categoria.
 */
public interface ConteoPorCategoria {

    Long getCategoriaId();

    Long getCantidad();
}
