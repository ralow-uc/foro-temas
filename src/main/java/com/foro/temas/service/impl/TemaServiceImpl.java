package com.foro.temas.service.impl;

import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.ComentarioResponse;
import com.foro.temas.dto.ModeracionRequest;
import com.foro.temas.dto.ModeracionResponse;
import com.foro.temas.dto.PaginaResponse;
import com.foro.temas.dto.TemaDetalleResponse;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.dto.TemaResponse;
import com.foro.temas.exception.CategoriaNoEncontradaException;
import com.foro.temas.exception.ComentarioNoEncontradoException;
import com.foro.temas.exception.ContenidoModeradoException;
import com.foro.temas.exception.DatosInvalidosException;
import com.foro.temas.exception.OperacionNoPermitidaException;
import com.foro.temas.exception.TemaCerradoException;
import com.foro.temas.exception.TemaNoEncontradoException;
import com.foro.temas.model.Categoria;
import com.foro.temas.model.Comentario;
import com.foro.temas.model.Tema;
import com.foro.temas.repository.CategoriaRepository;
import com.foro.temas.repository.ComentarioRepository;
import com.foro.temas.repository.TemaRepository;
import com.foro.temas.security.UsuarioAutenticado;
import com.foro.temas.service.TemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion de la logica de negocio de los temas y comentarios del foro.
 *
 * Aqui viven las reglas que no son del controlador:
 *   - el autor de un contenido manda sobre el suyo y un ADMIN sobre todo;
 *   - un tema cerrado no admite comentarios nuevos;
 *   - el contenido baneado por moderacion deja de ser visible para el publico
 *     (no se borra: se puede restituir si se levanta la sancion).
 */
@Service
public class TemaServiceImpl implements TemaService {

    private static final Logger log = LoggerFactory.getLogger(TemaServiceImpl.class);

    /** Tope de elementos por pagina, para que nadie pida la base completa de una vez. */
    private static final int TAMANO_MAXIMO_DE_PAGINA = 50;

    private final TemaRepository temaRepository;
    private final ComentarioRepository comentarioRepository;
    private final CategoriaRepository categoriaRepository;

    public TemaServiceImpl(TemaRepository temaRepository,
                           ComentarioRepository comentarioRepository,
                           CategoriaRepository categoriaRepository) {
        this.temaRepository = temaRepository;
        this.comentarioRepository = comentarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<TemaResponse> buscar(Long categoriaId, String texto, String autor,
                                               int pagina, int tamano) {
        Pageable paginacion = PageRequest.of(
                Math.max(pagina, 0),
                Math.clamp(tamano, 1, TAMANO_MAXIMO_DE_PAGINA),
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by("id")));

        Page<Tema> resultado = temaRepository.buscar(
                categoriaId, normalizar(texto), normalizar(autor), paginacion);

        return PaginaResponse.desde(resultado,
                tema -> TemaResponse.desde(tema, comentarioRepository.countByTemaIdAndBaneadoFalse(tema.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public TemaDetalleResponse obtenerPorId(Long id) {
        Tema tema = buscarTemaVisibleOFallar(id);
        return TemaDetalleResponse.desde(tema, listarComentarios(id));
    }

    @Override
    @Transactional
    public TemaResponse crear(TemaRequest solicitud, UsuarioAutenticado autor) {
        Categoria categoria = categoriaRepository.findById(solicitud.categoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(solicitud.categoriaId()));

        Tema tema = new Tema(
                null,
                categoria,
                autor.id(),
                autor.nombreUsuario(),
                solicitud.titulo().trim(),
                solicitud.contenido().trim());

        Tema guardado = temaRepository.save(tema);
        log.debug("Tema {} publicado por {}", guardado.getId(), autor.nombreUsuario());
        return TemaResponse.desde(guardado, 0L);
    }

    @Override
    @Transactional
    public TemaResponse actualizar(Long id, TemaRequest solicitud, UsuarioAutenticado solicitante) {
        Tema tema = buscarTemaOFallar(id);
        exigirPermisoSobre(tema.getAutorId(), solicitante, "editar este tema");

        Categoria categoria = categoriaRepository.findById(solicitud.categoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(solicitud.categoriaId()));

        tema.setCategoria(categoria);
        tema.setTitulo(solicitud.titulo().trim());
        tema.setContenido(solicitud.contenido().trim());

        Tema guardado = temaRepository.save(tema);
        return TemaResponse.desde(guardado, comentarioRepository.countByTemaIdAndBaneadoFalse(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id, UsuarioAutenticado solicitante) {
        Tema tema = buscarTemaOFallar(id);
        exigirPermisoSobre(tema.getAutorId(), solicitante, "eliminar este tema");

        temaRepository.delete(tema);
        log.debug("Tema {} eliminado por {}", id, solicitante.nombreUsuario());
    }

    @Override
    @Transactional
    public ComentarioResponse comentar(Long temaId, ComentarioRequest solicitud, UsuarioAutenticado autor) {
        Tema tema = buscarTemaVisibleOFallar(temaId);
        if (tema.isCerrado()) {
            throw new TemaCerradoException(temaId);
        }

        Comentario comentario = new Comentario(
                null, tema, autor.id(), autor.nombreUsuario(), solicitud.contenido().trim());
        // Se agrega por el agregado para que las dos puntas de la relacion queden
        // coherentes y el borrado en cascada del tema funcione.
        tema.agregarComentario(comentario);

        Comentario guardado = comentarioRepository.save(comentario);
        log.debug("Comentario {} publicado por {} en el tema {}",
                guardado.getId(), autor.nombreUsuario(), temaId);
        return ComentarioResponse.desde(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarComentarios(Long temaId) {
        buscarTemaVisibleOFallar(temaId);
        return comentarioRepository.findByTemaIdAndBaneadoFalseOrderByFechaCreacionAsc(temaId).stream()
                .map(ComentarioResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarComentario(Long comentarioId, UsuarioAutenticado solicitante) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ComentarioNoEncontradoException(comentarioId));
        exigirPermisoSobre(comentario.getAutorId(), solicitante, "eliminar este comentario");

        comentarioRepository.delete(comentario);
        log.debug("Comentario {} eliminado por {}", comentarioId, solicitante.nombreUsuario());
    }

    @Override
    @Transactional
    public ModeracionResponse moderarTema(Long temaId, ModeracionRequest orden, UsuarioAutenticado moderador) {
        Tema tema = buscarTemaOFallar(temaId);
        if (Boolean.TRUE.equals(orden.baneado())) {
            tema.banear(exigirMotivo(orden));
        } else {
            tema.restituir();
        }
        temaRepository.save(tema);
        log.info("Tema {} {} por {}", temaId, tema.isBaneado() ? "baneado" : "restituido",
                moderador.nombreUsuario());
        return aModeracion(tema);
    }

    @Override
    @Transactional
    public ModeracionResponse moderarComentario(Long comentarioId, ModeracionRequest orden,
                                                UsuarioAutenticado moderador) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ComentarioNoEncontradoException(comentarioId));
        if (Boolean.TRUE.equals(orden.baneado())) {
            comentario.banear(exigirMotivo(orden));
        } else {
            comentario.restituir();
        }
        comentarioRepository.save(comentario);
        log.info("Comentario {} {} por {}", comentarioId,
                comentario.isBaneado() ? "baneado" : "restituido", moderador.nombreUsuario());
        return aModeracion(comentario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeracionResponse> listarContenidoBaneado() {
        List<ModeracionResponse> baneados = new ArrayList<>();
        temaRepository.findByBaneadoTrueOrderByFechaCreacionDesc()
                .forEach(tema -> baneados.add(aModeracion(tema)));
        comentarioRepository.findByBaneadoTrueOrderByFechaCreacionDesc()
                .forEach(comentario -> baneados.add(aModeracion(comentario)));
        return baneados;
    }

    private String exigirMotivo(ModeracionRequest orden) {
        if (orden.motivo() == null || orden.motivo().isBlank()) {
            throw new DatosInvalidosException("El motivo es obligatorio para banear un contenido");
        }
        return orden.motivo().trim();
    }

    private ModeracionResponse aModeracion(Tema tema) {
        return new ModeracionResponse("TEMA", tema.getId(), tema.getTitulo(), tema.getAutorId(),
                tema.getAutorNombre(), tema.isBaneado(), tema.getMotivoBaneo());
    }

    private ModeracionResponse aModeracion(Comentario comentario) {
        String extracto = comentario.getContenido().length() > 80
                ? comentario.getContenido().substring(0, 77) + "..."
                : comentario.getContenido();
        return new ModeracionResponse("COMENTARIO", comentario.getId(), extracto,
                comentario.getAutorId(), comentario.getAutorNombre(),
                comentario.isBaneado(), comentario.getMotivoBaneo());
    }

    /** Como buscarTemaOFallar, pero un tema baneado se trata como no disponible (410). */
    private Tema buscarTemaVisibleOFallar(Long id) {
        Tema tema = buscarTemaOFallar(id);
        if (tema.isBaneado()) {
            throw new ContenidoModeradoException("El tema " + id
                    + " fue retirado por moderacion: " + tema.getMotivoBaneo());
        }
        return tema;
    }

    private Tema buscarTemaOFallar(Long id) {
        return temaRepository.findById(id).orElseThrow(() -> new TemaNoEncontradoException(id));
    }

    /**
     * Deja pasar al autor del contenido y a cualquier ADMIN; al resto lo detiene.
     * Es la regla que separa los privilegios de los dos roles.
     */
    private void exigirPermisoSobre(Long autorIdDelContenido, UsuarioAutenticado solicitante,
                                    String accion) {
        if (solicitante.esAdmin() || autorIdDelContenido.equals(solicitante.id())) {
            return;
        }
        log.debug("{} intento {} sin ser el autor", solicitante.nombreUsuario(), accion);
        throw new OperacionNoPermitidaException(
                "Solo el autor o un administrador puede " + accion);
    }

    /** Un filtro ausente o en blanco viaja como null, que el repositorio ignora. */
    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
