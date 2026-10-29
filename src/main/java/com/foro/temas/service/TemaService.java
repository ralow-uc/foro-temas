package com.foro.temas.service;

import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.ComentarioResponse;
import com.foro.temas.dto.PaginaResponse;
import com.foro.temas.dto.TemaDetalleResponse;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.dto.TemaResponse;
import com.foro.temas.security.UsuarioAutenticado;

import java.util.List;

/**
 * Contrato de la capa de servicio de los temas y comentarios del foro.
 *
 * Las operaciones que modifican contenido reciben el usuario autenticado porque
 * la regla de quien puede hacer que vive aqui, no en el controlador: el autor
 * manda sobre lo suyo y un ADMIN sobre todo.
 */
public interface TemaService {

    /** Busca temas con filtros opcionales de categoria, texto y autor, paginando. */
    PaginaResponse<TemaResponse> buscar(Long categoriaId, String texto, String autor,
                                        int pagina, int tamano);

    /** Devuelve el detalle de un tema junto con sus comentarios. */
    TemaDetalleResponse obtenerPorId(Long id);

    /** Publica un tema firmado por el usuario autenticado. */
    TemaResponse crear(TemaRequest solicitud, UsuarioAutenticado autor);

    /** Edita un tema. Solo su autor o un ADMIN. */
    TemaResponse actualizar(Long id, TemaRequest solicitud, UsuarioAutenticado solicitante);

    /** Elimina un tema y sus comentarios. Solo su autor o un ADMIN. */
    void eliminar(Long id, UsuarioAutenticado solicitante);

    /** Publica un comentario dentro de un tema abierto. */
    ComentarioResponse comentar(Long temaId, ComentarioRequest solicitud, UsuarioAutenticado autor);

    /** Lista los comentarios de un tema en orden cronologico. */
    List<ComentarioResponse> listarComentarios(Long temaId);

    /** Elimina un comentario. Solo su autor o un ADMIN. */
    void eliminarComentario(Long comentarioId, UsuarioAutenticado solicitante);
}
