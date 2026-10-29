package com.foro.temas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente envia al publicar (POST) o editar (PUT) un tema.
 *
 * El autor no se envia: se toma del JWT, de modo que nadie pueda publicar
 * firmando con el nombre de otro.
 */
public record TemaRequest(

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId,

        @NotBlank(message = "El titulo es obligatorio")
        @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres")
        String titulo,

        @NotBlank(message = "El contenido es obligatorio")
        @Size(min = 10, max = 4000, message = "El contenido debe tener entre 10 y 4000 caracteres")
        String contenido) {
}
