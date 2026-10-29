package com.foro.temas.exception;

/**
 * Se lanza cuando se referencia una categoria que no existe.
 */
public class CategoriaNoEncontradaException extends RuntimeException {

    public CategoriaNoEncontradaException(Long id) {
        super("No existe una categoria con el ID " + id);
    }
}
