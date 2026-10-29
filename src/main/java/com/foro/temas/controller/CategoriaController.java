package com.foro.temas.controller;

import com.foro.temas.dto.CategoriaRequest;
import com.foro.temas.dto.CategoriaResponse;
import com.foro.temas.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST de las categorias del foro.
 *
 * Endpoints expuestos:
 *   GET  /api/categorias       Lista las categorias con su cantidad de temas (publico).
 *   GET  /api/categorias/{id}  Obtiene una categoria (publico).
 *   POST /api/categorias       Crea una categoria. Solo ADMIN.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest solicitud,
                                                   UriComponentsBuilder uriBuilder) {
        CategoriaResponse creada = categoriaService.crear(solicitud);
        return ResponseEntity
                .created(uriBuilder.path("/api/categorias/{id}").buildAndExpand(creada.id()).toUri())
                .body(creada);
    }
}
