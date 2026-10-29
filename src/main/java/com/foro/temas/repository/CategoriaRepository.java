package com.foro.temas.repository;

import com.foro.temas.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de las categorias del foro.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Busca una categoria por su nombre, sin distinguir mayusculas. */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /** Busca una categoria por su identificador legible para URL. */
    Optional<Categoria> findBySlug(String slug);

    /** Comprueba si el nombre de categoria ya existe. */
    boolean existsByNombreIgnoreCase(String nombre);

    /** Comprueba si el slug ya existe. */
    boolean existsBySlug(String slug);

    /** Devuelve las categorias ordenadas por nombre, como se muestran en el foro. */
    List<Categoria> findAllByOrderByNombreAsc();
}
