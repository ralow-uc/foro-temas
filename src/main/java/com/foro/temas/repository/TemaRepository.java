package com.foro.temas.repository;

import com.foro.temas.model.Tema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de los temas del foro.
 *
 * Al extender JpaRepository se obtienen las operaciones CRUD contra Oracle sin
 * escribir SQL. Las dos consultas propias estan en JPQL porque combinan filtros
 * opcionales y una agrupacion, que no se pueden expresar con nombres de metodo.
 */
@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    /**
     * Busqueda de temas con tres filtros opcionales y paginacion.
     *
     * Cada condicion se desactiva sola cuando su parametro llega nulo, de modo
     * que una unica consulta cubre las ocho combinaciones posibles de filtros
     * sin multiplicar los metodos del repositorio.
     *
     * Dos detalles de esta consulta no son adorno:
     *
     * 1) El LIKE declara ESCAPE '\' de forma explicita. Si no se declara,
     *    Hibernate genera "escape ''" y, como en Oracle la cadena vacia ES
     *    NULL, la comparacion entera se vuelve nula y la busqueda por texto
     *    no devuelve nada. Con un caracter de escape real el LIKE funciona.
     *
     * 2) Los parametros de texto van envueltos en CAST(... AS String) dentro
     *    de la comprobacion de nulidad. Un parametro que solo aparece en un
     *    "IS NULL" no tiene tipo que inferir y el driver lo enviaria como un
     *    null sin tipo, que Oracle rechaza. El CAST le da tipo. El filtro de
     *    categoria no lo necesita: al compararse contra una columna numerica
     *    ya viaja como BIGINT.
     */
    @Query("""
            SELECT t FROM Tema t
            WHERE (:categoriaId IS NULL OR t.categoria.id = :categoriaId)
              AND (CAST(:texto AS String) IS NULL
                   OR UPPER(t.titulo) LIKE UPPER(CONCAT('%', :texto, '%')) ESCAPE '\\')
              AND (CAST(:autor AS String) IS NULL
                   OR UPPER(t.autorNombre) = UPPER(:autor))
            """)
    Page<Tema> buscar(@Param("categoriaId") Long categoriaId,
                      @Param("texto") String texto,
                      @Param("autor") String autor,
                      Pageable paginacion);

    /** Cuenta los temas de cada categoria en una sola consulta agrupada. */
    @Query("""
            SELECT t.categoria.id AS categoriaId, COUNT(t) AS cantidad
            FROM Tema t
            GROUP BY t.categoria.id
            """)
    List<ConteoPorCategoria> contarTemasPorCategoria();

    /** Cuantos temas hay en una categoria. Se usa para no borrar categorias con contenido. */
    long countByCategoriaId(Long categoriaId);
}
