package com.foro.temas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que un ADMIN envia para crear una categoria del foro.
 */
public record CategoriaRequest(

        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 400, message = "La descripcion no puede superar los 400 caracteres")
        String descripcion,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 100, message = "El slug no puede superar los 100 caracteres")
        @Pattern(regexp = "^[a-z0-9-]+$",
                message = "El slug solo admite minusculas, numeros y guiones")
        String slug) {
}
