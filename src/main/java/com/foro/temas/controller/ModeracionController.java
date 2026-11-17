package com.foro.temas.controller;

import com.foro.temas.dto.ModeracionRequest;
import com.foro.temas.dto.ModeracionResponse;
import com.foro.temas.security.UsuarioAutenticado;
import com.foro.temas.service.TemaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Punto de entrada que usa el microservicio foro-moderacion para aplicar sus
 * sanciones sobre el contenido de este servicio.
 *
 * Endpoints expuestos (todos exigen rol MODERADOR o ADMIN):
 *   PUT /api/moderacion/temas/{id}         Banea o restituye un tema.
 *   PUT /api/moderacion/comentarios/{id}   Banea o restituye un comentario.
 *   GET /api/moderacion/baneados           Contenido actualmente baneado.
 *
 * foro-temas sigue siendo el unico dueno de las tablas TEMA y COMENTARIO: el
 * otro servicio no las toca directamente, le pide el cambio por HTTP
 * reenviando el JWT del moderador. Asi cada microservicio conserva sus datos.
 */
@RestController
@RequestMapping("/api/moderacion")
@PreAuthorize("hasAnyRole('MODERADOR', 'ADMIN')")
public class ModeracionController {

    private final TemaService temaService;

    public ModeracionController(TemaService temaService) {
        this.temaService = temaService;
    }

    @PutMapping("/temas/{id}")
    public ResponseEntity<ModeracionResponse> moderarTema(@PathVariable Long id,
                                                          @Valid @RequestBody ModeracionRequest orden,
                                                          @AuthenticationPrincipal UsuarioAutenticado moderador) {
        return ResponseEntity.ok(temaService.moderarTema(id, orden, moderador));
    }

    @PutMapping("/comentarios/{id}")
    public ResponseEntity<ModeracionResponse> moderarComentario(@PathVariable Long id,
                                                                @Valid @RequestBody ModeracionRequest orden,
                                                                @AuthenticationPrincipal UsuarioAutenticado moderador) {
        return ResponseEntity.ok(temaService.moderarComentario(id, orden, moderador));
    }

    @GetMapping("/baneados")
    public ResponseEntity<List<ModeracionResponse>> listarBaneados() {
        return ResponseEntity.ok(temaService.listarContenidoBaneado());
    }
}
