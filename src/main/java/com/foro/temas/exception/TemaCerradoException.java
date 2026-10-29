package com.foro.temas.exception;

/**
 * Se lanza cuando se intenta comentar un tema que fue cerrado.
 */
public class TemaCerradoException extends RuntimeException {

    public TemaCerradoException(Long id) {
        super("El tema " + id + " esta cerrado y no admite comentarios nuevos");
    }
}
