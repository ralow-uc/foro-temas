package com.foro.temas.controller;

import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.ComentarioResponse;
import com.foro.temas.dto.PaginaResponse;
import com.foro.temas.dto.TemaDetalleResponse;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.dto.TemaResponse;
import com.foro.temas.security.UsuarioAutenticado;
import com.foro.temas.service.TemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST de los temas y comentarios del foro.
 *
 * Endpoints expuestos:
 *   GET    /api/temas                   Busca temas. Filtros ?categoria= ?texto= ?autor=,
 *                                       paginado con ?pagina= y ?tamano= (publico).
 *   GET    /api/temas/{id}              Detalle del tema con sus comentarios (publico).
 *   POST   /api/temas                   Publica un tema (requiere token).
 *   PUT    /api/temas/{id}              Edita un tema. Su autor o un ADMIN.
 *   DELETE /api/temas/{id}              Elimina un tema. Su autor o un ADMIN.
 *   GET    /api/temas/{id}/comentarios  Lista los comentarios del tema (publico).
 *   POST   /api/temas/{id}/comentarios  Comenta un tema (requiere token).
 *
 * El controlador no decide quien puede editar o borrar: pasa la identidad del
 * token al servicio, que es donde vive esa regla de negocio.
 */
@RestController
@RequestMapping("/api/temas")
public class TemaController {

    /** Tamano de pagina por omision cuando el cliente no lo indica. */
    private static final int TAMANO_POR_OMISION = 10;

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<TemaResponse>> buscar(
            @RequestParam(name = "categoria", required = false) Long categoriaId,
            @RequestParam(name = "texto", required = false) String texto,
            @RequestParam(name = "autor", required = false) String autor,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamano", defaultValue = "" + TAMANO_POR_OMISION) int tamano) {

        return ResponseEntity.ok(temaService.buscar(categoriaId, texto, autor, pagina, tamano));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemaDetalleResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(temaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TemaResponse> publicar(@Valid @RequestBody TemaRequest solicitud,
                                                 @AuthenticationPrincipal UsuarioAutenticado autor,
                                                 UriComponentsBuilder uriBuilder) {
        TemaResponse creado = temaService.crear(solicitud, autor);
        return ResponseEntity
                .created(uriBuilder.path("/api/temas/{id}").buildAndExpand(creado.id()).toUri())
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemaResponse> actualizar(@PathVariable Long id,
                                                   @Valid @RequestBody TemaRequest solicitud,
                                                   @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        return ResponseEntity.ok(temaService.actualizar(id, solicitud, solicitante));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        temaService.eliminar(id, solicitante);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listarComentarios(@PathVariable Long id) {
        return ResponseEntity.ok(temaService.listarComentarios(id));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(@PathVariable Long id,
                                                       @Valid @RequestBody ComentarioRequest solicitud,
                                                       @AuthenticationPrincipal UsuarioAutenticado autor,
                                                       UriComponentsBuilder uriBuilder) {
        ComentarioResponse creado = temaService.comentar(id, solicitud, autor);
        return ResponseEntity
                .created(uriBuilder.path("/api/temas/{tema}/comentarios").buildAndExpand(id).toUri())
                .body(creado);
    }
}
