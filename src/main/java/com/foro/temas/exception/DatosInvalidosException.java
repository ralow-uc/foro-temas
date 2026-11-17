package com.foro.temas.exception;

/**
 * Regla de negocio sobre los datos de entrada que no se puede expresar con
 * Bean Validation, por ejemplo "el motivo es obligatorio cuando se banea".
 * Se responde 400.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
