package com.foro.temas.exception;

/**
 * Se lanza cuando un usuario intenta modificar o borrar contenido que no le
 * pertenece y no tiene el rol ADMIN.
 *
 * Es la excepcion que materializa la diferencia de privilegios entre los roles:
 * el controlador la traduce en un 403.
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
