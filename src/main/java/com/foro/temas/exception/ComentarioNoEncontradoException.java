package com.foro.temas.exception;

/**
 * Se lanza cuando se solicita un comentario cuyo ID no existe.
 */
public class ComentarioNoEncontradoException extends RuntimeException {

    public ComentarioNoEncontradoException(Long id) {
        super("No existe un comentario con el ID " + id);
    }
}
