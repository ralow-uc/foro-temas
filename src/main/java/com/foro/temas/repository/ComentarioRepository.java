package com.foro.temas.repository;

import com.foro.temas.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de los comentarios del foro.
 */
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /** Devuelve los comentarios de un tema en orden cronologico. */
    List<Comentario> findByTemaIdOrderByFechaCreacionAsc(Long temaId);

    /** Cuantos comentarios tiene un tema. */
    long countByTemaId(Long temaId);

    /** Comentarios visibles (no baneados) de un tema, en orden cronologico. */
    List<Comentario> findByTemaIdAndBaneadoFalseOrderByFechaCreacionAsc(Long temaId);

    /** Cuantos comentarios visibles (no baneados) tiene un tema. */
    long countByTemaIdAndBaneadoFalse(Long temaId);

    /** Comentarios retirados por moderacion. */
    List<Comentario> findByBaneadoTrueOrderByFechaCreacionDesc();
}
