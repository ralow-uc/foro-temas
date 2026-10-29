package com.foro.temas.exception;

/**
 * Se lanza cuando se intenta crear una categoria con un nombre o un slug que ya
 * existe en el foro.
 */
public class DatosDuplicadosException extends RuntimeException {

    public DatosDuplicadosException(String mensaje) {
        super(mensaje);
    }
}
