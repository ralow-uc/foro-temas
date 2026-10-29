package com.foro.temas.service;

import com.foro.temas.dto.CategoriaRequest;
import com.foro.temas.dto.CategoriaResponse;
import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.ComentarioResponse;
import com.foro.temas.dto.PaginaResponse;
import com.foro.temas.dto.TemaDetalleResponse;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.dto.TemaResponse;
import com.foro.temas.exception.CategoriaNoEncontradaException;
import com.foro.temas.exception.DatosDuplicadosException;
import com.foro.temas.exception.OperacionNoPermitidaException;
import com.foro.temas.exception.TemaNoEncontradoException;
import com.foro.temas.repository.CategoriaRepository;
import com.foro.temas.repository.ComentarioRepository;
import com.foro.temas.repository.TemaRepository;
import com.foro.temas.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del CRUD de categorias, temas y comentarios a traves de la capa de
 * servicio, incluidas las reglas de quien puede modificar o borrar que.
 */
@SpringBootTest
@ActiveProfiles("test")
class ForoServiceImplTest {

    private static final UsuarioAutenticado FELIKS =
            new UsuarioAutenticado(1L, "feliks", List.of("USUARIO"));
    private static final UsuarioAutenticado YIHENG =
            new UsuarioAutenticado(2L, "yiheng", List.of("USUARIO"));
    private static final UsuarioAutenticado ADMIN =
            new UsuarioAutenticado(9L, "admin", List.of("ADMIN"));

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private TemaService temaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Long idCategoria;

    @BeforeEach
    void prepararBase() {
        comentarioRepository.deleteAll();
        temaRepository.deleteAll();
        categoriaRepository.deleteAll();
        idCategoria = categoriaService.crear(
                new CategoriaRequest("Cubo 3x3", "El clasico de 3x3x3", "cubo-3x3")).id();
    }

    private Long publicarTema(UsuarioAutenticado autor, String titulo) {
        return temaService.crear(new TemaRequest(idCategoria, titulo, "Contenido del tema."), autor).id();
    }

    // ---------------------------------------------------------------- categorias

    @Test
    @DisplayName("POST: crea una categoria y reporta cuantos temas tiene")
    void crearCategoria() {
        CategoriaResponse creada = categoriaService.crear(
                new CategoriaRequest("Big cubes", "4x4 y superiores", "big-cubes"));

        assertThat(creada.id()).isNotNull();
        assertThat(creada.nombre()).isEqualTo("Big cubes");
        assertThat(creada.cantidadDeTemas()).isZero();
    }

    @Test
    @DisplayName("POST: rechaza una categoria con nombre o slug repetido")
    void crearCategoriaDuplicadaFalla() {
        assertThatThrownBy(() -> categoriaService.crear(
                new CategoriaRequest("Cubo 3x3", "Otra descripcion", "otro-slug")))
                .isInstanceOf(DatosDuplicadosException.class);

        assertThatThrownBy(() -> categoriaService.crear(
                new CategoriaRequest("Otro nombre", "Otra descripcion", "cubo-3x3")))
                .isInstanceOf(DatosDuplicadosException.class);
    }

    @Test
    @DisplayName("GET: lista las categorias con la cantidad real de temas de cada una")
    void listarCategoriasConSuConteo() {
        categoriaService.crear(new CategoriaRequest("Big cubes", "4x4 y superiores", "big-cubes"));
        publicarTema(FELIKS, "Mi primer sub-20");
        publicarTema(YIHENG, "Cross a ciegas");

        List<CategoriaResponse> categorias = categoriaService.listar();

        assertThat(categorias).hasSize(2);
        assertThat(categorias).filteredOn(c -> c.nombre().equals("Cubo 3x3"))
                .first().extracting(CategoriaResponse::cantidadDeTemas).isEqualTo(2L);
        assertThat(categorias).filteredOn(c -> c.nombre().equals("Big cubes"))
                .first().extracting(CategoriaResponse::cantidadDeTemas).isEqualTo(0L);
    }

    // --------------------------------------------------------------------- temas

    @Test
    @DisplayName("POST: publica un tema firmado con el autor que viene del token")
    void crearTema() {
        TemaResponse creado = temaService.crear(
                new TemaRequest(idCategoria, "Resena del GAN 14", "El MagLev se siente distinto."), FELIKS);

        assertThat(creado.id()).isNotNull();
        assertThat(creado.autorId()).isEqualTo(FELIKS.id());
        assertThat(creado.autorNombre()).isEqualTo("feliks");
        assertThat(creado.categoria().nombre()).isEqualTo("Cubo 3x3");
        assertThat(creado.cerrado()).isFalse();
        assertThat(creado.fechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("POST: no se puede publicar en una categoria que no existe")
    void crearTemaEnCategoriaInexistenteFalla() {
        assertThatThrownBy(() -> temaService.crear(
                new TemaRequest(9999L, "Titulo", "Contenido"), FELIKS))
                .isInstanceOf(CategoriaNoEncontradaException.class);
    }

    @Test
    @DisplayName("GET: la busqueda pagina y filtra por categoria, texto y autor")
    void buscarTemas() {
        publicarTema(FELIKS, "Mi primer sub-20 con CFOP");
        publicarTema(YIHENG, "CFOP o Roux para bajar de 15");
        publicarTema(FELIKS, "Resena del GAN 14");

        PaginaResponse<TemaResponse> todos = temaService.buscar(null, null, null, 0, 10);
        PaginaResponse<TemaResponse> porTexto = temaService.buscar(null, "cfop", null, 0, 10);
        PaginaResponse<TemaResponse> porAutor = temaService.buscar(null, null, "FELIKS", 0, 10);
        PaginaResponse<TemaResponse> primeraPagina = temaService.buscar(null, null, null, 0, 2);

        assertThat(todos.totalElementos()).isEqualTo(3);
        assertThat(porTexto.totalElementos()).isEqualTo(2);
        assertThat(porAutor.totalElementos()).isEqualTo(2);
        assertThat(primeraPagina.contenido()).hasSize(2);
        assertThat(primeraPagina.totalPaginas()).isEqualTo(2);
        assertThat(primeraPagina.pagina()).isZero();
    }

    @Test
    @DisplayName("GET: el detalle de un tema trae sus comentarios en orden")
    void detalleDeTemaConComentarios() {
        Long idTema = publicarTema(FELIKS, "Mi primer sub-20");
        temaService.comentar(idTema, new ComentarioRequest("Felicitaciones."), YIHENG);
        temaService.comentar(idTema, new ComentarioRequest("Cual cubo usas?"), ADMIN);

        TemaDetalleResponse detalle = temaService.obtenerPorId(idTema);

        assertThat(detalle.titulo()).isEqualTo("Mi primer sub-20");
        assertThat(detalle.comentarios()).hasSize(2);
        assertThat(detalle.comentarios()).extracting(ComentarioResponse::autorNombre)
                .containsExactly("yiheng", "admin");
    }

    @Test
    @DisplayName("GET: un tema inexistente lanza TemaNoEncontradoException")
    void detalleDeTemaInexistenteFalla() {
        assertThatThrownBy(() -> temaService.obtenerPorId(9999L))
                .isInstanceOf(TemaNoEncontradoException.class)
                .hasMessageContaining("9999");
    }

    @Test
    @DisplayName("PUT: el autor puede editar su propio tema")
    void elAutorEditaSuTema() {
        Long idTema = publicarTema(FELIKS, "Titulo original");

        TemaResponse actualizado = temaService.actualizar(
                idTema, new TemaRequest(idCategoria, "Titulo corregido", "Contenido corregido."), FELIKS);

        assertThat(actualizado.titulo()).isEqualTo("Titulo corregido");
    }

    @Test
    @DisplayName("PUT: otro usuario no puede editar un tema ajeno")
    void otroUsuarioNoEditaTemaAjeno() {
        Long idTema = publicarTema(FELIKS, "Titulo original");

        assertThatThrownBy(() -> temaService.actualizar(
                idTema, new TemaRequest(idCategoria, "Secuestrado", "Contenido."), YIHENG))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    @DisplayName("DELETE: el autor borra su tema y el ADMIN puede borrar cualquiera")
    void borradoPorAutorYPorAdmin() {
        Long idPropio = publicarTema(FELIKS, "Tema propio");
        Long idAjeno = publicarTema(YIHENG, "Tema de otro");

        temaService.eliminar(idPropio, FELIKS);
        assertThat(temaRepository.existsById(idPropio)).isFalse();

        temaService.eliminar(idAjeno, ADMIN);
        assertThat(temaRepository.existsById(idAjeno)).isFalse();
    }

    @Test
    @DisplayName("DELETE: un usuario sin privilegios no borra temas ajenos")
    void usuarioNoBorraTemaAjeno() {
        Long idTema = publicarTema(FELIKS, "Tema propio");

        assertThatThrownBy(() -> temaService.eliminar(idTema, YIHENG))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    @DisplayName("DELETE: al borrar un tema desaparecen sus comentarios")
    void borrarTemaBorraComentarios() {
        Long idTema = publicarTema(FELIKS, "Tema con respuestas");
        temaService.comentar(idTema, new ComentarioRequest("Primero."), YIHENG);
        temaService.comentar(idTema, new ComentarioRequest("Segundo."), ADMIN);

        temaService.eliminar(idTema, ADMIN);

        assertThat(comentarioRepository.countByTemaId(idTema)).isZero();
    }

    // ---------------------------------------------------------------- comentarios

    @Test
    @DisplayName("POST: cualquier usuario autenticado puede comentar un tema abierto")
    void comentarTema() {
        Long idTema = publicarTema(FELIKS, "Mi primer sub-20");

        ComentarioResponse comentario = temaService.comentar(
                idTema, new ComentarioRequest("Buen avance, sigue asi."), YIHENG);

        assertThat(comentario.id()).isNotNull();
        assertThat(comentario.temaId()).isEqualTo(idTema);
        assertThat(comentario.autorNombre()).isEqualTo("yiheng");
        assertThat(temaService.listarComentarios(idTema)).hasSize(1);
    }

    @Test
    @DisplayName("DELETE: el autor del comentario y el ADMIN pueden borrarlo, otro usuario no")
    void borradoDeComentarios() {
        Long idTema = publicarTema(FELIKS, "Mi primer sub-20");
        Long idDeYiheng = temaService.comentar(idTema, new ComentarioRequest("De yiheng."), YIHENG).id();
        Long idDeAdmin = temaService.comentar(idTema, new ComentarioRequest("Del admin."), ADMIN).id();

        assertThatThrownBy(() -> temaService.eliminarComentario(idDeYiheng, FELIKS))
                .isInstanceOf(OperacionNoPermitidaException.class);

        temaService.eliminarComentario(idDeYiheng, YIHENG);
        temaService.eliminarComentario(idDeAdmin, ADMIN);

        assertThat(temaService.listarComentarios(idTema)).isEmpty();
    }
}
