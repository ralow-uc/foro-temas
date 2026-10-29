package com.foro.temas.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de la aplicacion en respuestas HTTP con un cuerpo
 * claro, para que Postman muestre el motivo exacto del error.
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeErrores.class);

    @ExceptionHandler(TemaNoEncontradoException.class)
    public ProblemDetail manejarTemaNoEncontrado(TemaNoEncontradoException ex) {
        return detalle(HttpStatus.NOT_FOUND, "Tema no encontrado", ex.getMessage());
    }

    @ExceptionHandler(CategoriaNoEncontradaException.class)
    public ProblemDetail manejarCategoriaNoEncontrada(CategoriaNoEncontradaException ex) {
        return detalle(HttpStatus.NOT_FOUND, "Categoria no encontrada", ex.getMessage());
    }

    @ExceptionHandler(ComentarioNoEncontradoException.class)
    public ProblemDetail manejarComentarioNoEncontrado(ComentarioNoEncontradoException ex) {
        return detalle(HttpStatus.NOT_FOUND, "Comentario no encontrado", ex.getMessage());
    }

    /**
     * Un usuario autenticado que intenta tocar contenido ajeno recibe 403.
     * Es la diferencia de privilegios entre roles vista desde el cliente.
     */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ProblemDetail manejarOperacionNoPermitida(OperacionNoPermitidaException ex) {
        log.debug("Operacion rechazada por falta de privilegios: {}", ex.getMessage());
        return detalle(HttpStatus.FORBIDDEN, "Operacion no permitida", ex.getMessage());
    }

    @ExceptionHandler(DatosDuplicadosException.class)
    public ProblemDetail manejarDuplicados(DatosDuplicadosException ex) {
        return detalle(HttpStatus.CONFLICT, "Datos duplicados", ex.getMessage());
    }

    @ExceptionHandler(TemaCerradoException.class)
    public ProblemDetail manejarTemaCerrado(TemaCerradoException ex) {
        return detalle(HttpStatus.CONFLICT, "Tema cerrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problema = detalle(HttpStatus.BAD_REQUEST, "Datos invalidos",
                "Revisa los campos indicados en 'errores'");
        problema.setProperty("errores", errores);
        return problema;
    }

    private ProblemDetail detalle(HttpStatus estado, String titulo, String mensaje) {
        ProblemDetail problema = ProblemDetail.forStatus(estado);
        problema.setTitle(titulo);
        problema.setDetail(mensaje);
        return problema;
    }
}
