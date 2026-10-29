package com.foro.temas.service.impl;

import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.ComentarioResponse;
import com.foro.temas.dto.PaginaResponse;
import com.foro.temas.dto.TemaDetalleResponse;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.dto.TemaResponse;
import com.foro.temas.exception.CategoriaNoEncontradaException;
import com.foro.temas.exception.ComentarioNoEncontradoException;
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

import java.util.List;

/**
 * Implementacion de la logica de negocio de los temas y comentarios del foro.
 *
 * Aqui viven dos reglas que no son del controlador:
 *   - el autor de un contenido manda sobre el suyo y un ADMIN sobre todo;
 *   - un tema cerrado no admite comentarios nuevos.
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
                tema -> TemaResponse.desde(tema, comentarioRepository.countByTemaId(tema.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public TemaDetalleResponse obtenerPorId(Long id) {
        Tema tema = buscarTemaOFallar(id);
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
        return TemaResponse.desde(guardado, comentarioRepository.countByTemaId(id));
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
        Tema tema = buscarTemaOFallar(temaId);
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
        if (!temaRepository.existsById(temaId)) {
            throw new TemaNoEncontradoException(temaId);
        }
        return comentarioRepository.findByTemaIdOrderByFechaCreacionAsc(temaId).stream()
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
