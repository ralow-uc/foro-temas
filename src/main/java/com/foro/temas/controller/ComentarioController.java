package com.foro.temas.controller;

import com.foro.temas.security.UsuarioAutenticado;
import com.foro.temas.service.TemaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operar sobre un comentario concreto.
 *
 * Endpoint expuesto:
 *   DELETE /api/comentarios/{id}   Elimina un comentario. Su autor o un ADMIN.
 *
 * Vive aparte de TemaController porque un comentario se borra por su propio ID,
 * sin necesidad de saber a que tema pertenece.
 */
@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final TemaService temaService;

    public ComentarioController(TemaService temaService) {
        this.temaService = temaService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        temaService.eliminarComentario(id, solicitante);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
