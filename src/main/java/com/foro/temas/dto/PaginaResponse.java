package com.foro.temas.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envoltorio de una pagina de resultados.
 *
 * Se devuelve este record en vez del Page de Spring Data para que el JSON que
 * ve el cliente tenga una forma estable y en espanol, sin depender de la
 * serializacion interna del framework.
 */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    public static <E, T> PaginaResponse<T> desde(Page<E> pagina, Function<E, T> convertidor) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(convertidor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
