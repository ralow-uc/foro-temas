package com.foro.temas.exception;

/**
 * Se lanza cuando se solicita un tema cuyo ID no existe.
 */
public class TemaNoEncontradoException extends RuntimeException {

    public TemaNoEncontradoException(Long id) {
        super("No existe un tema con el ID " + id);
    }
}
