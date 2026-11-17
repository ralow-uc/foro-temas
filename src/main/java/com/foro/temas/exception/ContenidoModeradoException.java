package com.foro.temas.exception;

/**
 * Se lanza cuando se pide un tema que fue retirado por moderacion.
 *
 * Se responde 410 Gone y no 404: el tema existio y sigue en la base, pero ya no
 * esta disponible para el publico. Asi el cliente puede mostrar "contenido
 * moderado" en lugar de "no existe".
 */
public class ContenidoModeradoException extends RuntimeException {

    public ContenidoModeradoException(String mensaje) {
        super(mensaje);
    }
}
