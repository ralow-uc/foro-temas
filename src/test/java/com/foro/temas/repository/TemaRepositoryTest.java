package com.foro.temas.repository;

import com.foro.temas.model.Categoria;
import com.foro.temas.model.Comentario;
import com.foro.temas.model.Tema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprueba el mapeo JPA de Categoria, Tema y Comentario, y las consultas del
 * foro contra una base de datos real (H2 en modo Oracle).
 */
@DataJpaTest
// Sin esto, @DataJpaTest sustituye el DataSource por uno embebido y descarta
// la URL del perfil de pruebas, con lo que H2 dejaria de emular a Oracle y
// las pruebas validarian una semantica distinta a la de la base real.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TemaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Categoria tresPorTres;
    private Categoria metodos;

    @BeforeEach
    void prepararDatos() {
        tresPorTres = categoriaRepository.save(
                new Categoria(null, "Cubo 3x3", "El clasico de 3x3x3", "cubo-3x3"));
        metodos = categoriaRepository.save(
                new Categoria(null, "Metodos y algoritmos", "CFOP, Roux, ZZ y mas", "metodos-y-algoritmos"));

        temaRepository.save(new Tema(null, tresPorTres, 1L, "feliks",
                "Mi primer sub-20 con CFOP", "Por fin baje de 20 segundos de promedio."));
        temaRepository.save(new Tema(null, metodos, 2L, "yiheng",
                "CFOP o Roux para bajar de 15", "Llevo meses estancado y no se si cambiar de metodo."));
        temaRepository.save(new Tema(null, metodos, 1L, "feliks",
                "Set completo de OLL en un mes", "Comparto mi plan de memorizacion."));
    }

    @Test
    @DisplayName("JPA genera el ID con la secuencia y persiste el tema con su categoria")
    void guardaYRecuperaElTema() {
        Tema guardado = temaRepository.save(new Tema(null, tresPorTres, 3L, "tymon",
                "Resena del GAN 14", "El MagLev se siente distinto al principio."));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFechaCreacion()).isNotNull();
        assertThat(guardado.isCerrado()).isFalse();

        Tema recuperado = temaRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getTitulo()).isEqualTo("Resena del GAN 14");
        assertThat(recuperado.getAutorNombre()).isEqualTo("tymon");
        assertThat(recuperado.getCategoria().getNombre()).isEqualTo("Cubo 3x3");
    }

    @Test
    @DisplayName("La busqueda sin filtros devuelve todos los temas paginados")
    void busquedaSinFiltros() {
        Page<Tema> pagina = temaRepository.buscar(null, null, null,
                PageRequest.of(0, 2, Sort.by("id")));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getContent()).hasSize(2);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("La busqueda filtra por categoria")
    void busquedaPorCategoria() {
        Page<Tema> pagina = temaRepository.buscar(metodos.getId(), null, null,
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(pagina.getContent()).extracting(t -> t.getCategoria().getNombre())
                .containsOnly("Metodos y algoritmos");
        assertThat(pagina.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("La busqueda por texto del titulo no distingue mayusculas")
    void busquedaPorTexto() {
        Page<Tema> enMayusculas = temaRepository.buscar(null, "CFOP", null,
                PageRequest.of(0, 10, Sort.by("id")));
        Page<Tema> enMinusculas = temaRepository.buscar(null, "cfop", null,
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(enMayusculas.getContent()).extracting(Tema::getTitulo)
                .containsExactly("Mi primer sub-20 con CFOP", "CFOP o Roux para bajar de 15");
        assertThat(enMinusculas.getContent()).extracting(Tema::getTitulo)
                .containsExactly("Mi primer sub-20 con CFOP", "CFOP o Roux para bajar de 15");
    }

    @Test
    @DisplayName("La busqueda filtra por autor")
    void busquedaPorAutor() {
        Page<Tema> pagina = temaRepository.buscar(null, null, "FELIKS",
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(Tema::getAutorNombre).containsOnly("feliks");
    }

    @Test
    @DisplayName("Los tres filtros se combinan en una sola consulta")
    void busquedaConLosTresFiltros() {
        Page<Tema> pagina = temaRepository.buscar(metodos.getId(), "oll", "feliks",
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(pagina.getContent()).extracting(Tema::getTitulo)
                .containsExactly("Set completo de OLL en un mes");
    }

    @Test
    @DisplayName("Buscar sin filtros y despues por texto no contamina la segunda consulta")
    void busquedaSinFiltrosSeguidaDeBusquedaPorTexto() {
        temaRepository.buscar(null, null, null, PageRequest.of(0, 10, Sort.by("id")));

        Page<Tema> porTexto = temaRepository.buscar(null, "cfop", null,
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(porTexto.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cuenta los temas agrupados por categoria en una sola consulta")
    void cuentaTemasPorCategoria() {
        Map<Long, Long> conteo = temaRepository.contarTemasPorCategoria().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ConteoPorCategoria::getCategoriaId,
                        ConteoPorCategoria::getCantidad,
                        (a, b) -> a));

        assertThat(conteo.get(tresPorTres.getId())).isEqualTo(1);
        assertThat(conteo.get(metodos.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Los comentarios de un tema se recuperan en orden cronologico")
    void comentariosEnOrden() {
        Tema tema = temaRepository.buscar(null, "sub-20", null, PageRequest.of(0, 1))
                .getContent().getFirst();

        tema.agregarComentario(new Comentario(null, tema, 2L, "yiheng", "Felicitaciones, buen avance."));
        tema.agregarComentario(new Comentario(null, tema, 3L, "tymon", "Cual cubo estas usando?"));
        temaRepository.saveAndFlush(tema);

        List<Comentario> comentarios = comentarioRepository.findByTemaIdOrderByFechaCreacionAsc(tema.getId());

        assertThat(comentarios).hasSize(2);
        assertThat(comentarios).extracting(Comentario::getAutorNombre).containsExactly("yiheng", "tymon");
        assertThat(comentarioRepository.countByTemaId(tema.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Las categorias se consultan por slug y por nombre")
    void consultaDeCategorias() {
        assertThat(categoriaRepository.findBySlug("cubo-3x3")).isPresent();
        assertThat(categoriaRepository.findByNombreIgnoreCase("CUBO 3X3")).isPresent();
        assertThat(categoriaRepository.existsBySlug("inexistente")).isFalse();
    }

    @Test
    @DisplayName("Al borrar un tema se borran sus comentarios")
    void borrarTemaBorraSusComentarios() {
        Tema tema = temaRepository.buscar(null, "sub-20", null, PageRequest.of(0, 1))
                .getContent().getFirst();
        tema.agregarComentario(new Comentario(null, tema, 2L, "yiheng", "Comentario que debe desaparecer."));
        temaRepository.saveAndFlush(tema);

        Function<Long, Long> cuantos = comentarioRepository::countByTemaId;
        assertThat(cuantos.apply(tema.getId())).isEqualTo(1);

        temaRepository.delete(tema);
        temaRepository.flush();

        assertThat(comentarioRepository.countByTemaId(tema.getId())).isZero();
    }
}
