package com.foro.temas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Orden de moderacion sobre un tema o un comentario.
 *
 * La envia el microservicio foro-moderacion (o un moderador desde Postman):
 *   { "baneado": true,  "motivo": "Spam: publicidad de una tienda" }  retira el contenido
 *   { "baneado": false }                                              lo restituye
 *
 * Que el motivo sea obligatorio al banear se comprueba en el servicio, porque
 * depende del valor de "baneado" y no se puede expresar con una sola anotacion.
 */
public record ModeracionRequest(
        @NotNull(message = "Indica si el contenido queda baneado (true) o restituido (false)")
        Boolean baneado,

        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
        String motivo) {
}
