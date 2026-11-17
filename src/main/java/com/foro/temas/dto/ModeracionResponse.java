package com.foro.temas.dto;

/**
 * Estado de moderacion de un contenido despues de aplicar una orden.
 *
 * @param tipo        TEMA o COMENTARIO
 * @param id          identificador del contenido
 * @param titulo      titulo del tema, o un extracto del comentario
 * @param autorId     autor del contenido, para que moderacion pueda registrarlo
 * @param autorNombre nombre del autor
 * @param baneado     si quedo retirado del foro
 * @param motivo      motivo del baneo, nulo si fue restituido
 */
public record ModeracionResponse(
        String tipo,
        Long id,
        String titulo,
        Long autorId,
        String autorNombre,
        boolean baneado,
        String motivo) {
}
