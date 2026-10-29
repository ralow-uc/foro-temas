package com.foro.temas.controller;

import com.foro.temas.dto.CategoriaRequest;
import com.foro.temas.dto.ComentarioRequest;
import com.foro.temas.dto.TemaRequest;
import com.foro.temas.repository.CategoriaRepository;
import com.foro.temas.repository.ComentarioRepository;
import com.foro.temas.repository.TemaRepository;
import com.foro.temas.security.GeneradorDeTokensDePrueba;
import com.foro.temas.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los endpoints HTTP del microservicio del foro, incluidas las
 * reglas de autorizacion. Los tokens se firman en la propia prueba con el mismo
 * secreto que usa foro-usuarios, de modo que no hace falta levantar el otro
 * servicio para comprobar que la autorizacion funciona.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeneradorDeTokensDePrueba tokens;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Long idCategoria;
    private String tokenFeliks;
    private String tokenYiheng;
    private String tokenAdmin;

    @BeforeEach
    void prepararBase() {
        comentarioRepository.deleteAll();
        temaRepository.deleteAll();
        categoriaRepository.deleteAll();
        idCategoria = categoriaService.crear(
                new CategoriaRequest("Cubo 3x3", "El clasico de 3x3x3", "cubo-3x3")).id();

        tokenFeliks = tokens.para(1L, "feliks", "USUARIO");
        tokenYiheng = tokens.para(2L, "yiheng", "USUARIO");
        tokenAdmin = tokens.para(9L, "admin", "ADMIN");
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    private Long publicarTema(String token, String titulo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/temas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TemaRequest(idCategoria, titulo, "Contenido del tema de prueba."))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    // ---------------------------------------------------------------- categorias

    @Test
    @DisplayName("GET /api/categorias es publico y trae la cantidad de temas")
    void listarCategoriasEsPublico() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cubo 3x3"))
                .andExpect(jsonPath("$[0].cantidadDeTemas").value(0));
    }

    @Test
    @DisplayName("POST /api/categorias exige token y rol ADMIN")
    void crearCategoriaSoloAdmin() throws Exception {
        String cuerpo = json(new CategoriaRequest("Big cubes", "4x4 y superiores", "big-cubes"));

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/categorias").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/categorias").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("big-cubes"));
    }

    @Test
    @DisplayName("POST /api/categorias responde 409 si el nombre ya existe")
    void crearCategoriaDuplicadaDevuelve409() throws Exception {
        mockMvc.perform(post("/api/categorias").header("Authorization", tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CategoriaRequest("Cubo 3x3", "Otra", "otro-slug"))))
                .andExpect(status().isConflict());
    }

    // --------------------------------------------------------------------- temas

    @Test
    @DisplayName("Un token con firma invalida no autentica")
    void tokenConFirmaInvalidaEsRechazado() throws Exception {
        mockMvc.perform(post("/api/temas")
                        .header("Authorization", tokens.conFirmaInvalida("impostor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TemaRequest(idCategoria, "Titulo falso", "Contenido falso."))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/temas exige token y firma el tema con el autor del token")
    void publicarTemaExigeToken() throws Exception {
        String cuerpo = json(new TemaRequest(idCategoria, "Mi primer sub-20", "Por fin baje de 20 segundos."));

        mockMvc.perform(post("/api/temas")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/temas").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorNombre").value("feliks"))
                .andExpect(jsonPath("$.autorId").value(1))
                .andExpect(jsonPath("$.categoria.nombre").value("Cubo 3x3"))
                .andExpect(jsonPath("$.cantidadDeComentarios").value(0));
    }

    @Test
    @DisplayName("POST /api/temas responde 400 si el titulo o el contenido no son validos")
    void publicarTemaInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/temas").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TemaRequest(idCategoria, "hey", "corto"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists())
                .andExpect(jsonPath("$.errores.contenido").exists());
    }

    @Test
    @DisplayName("POST /api/temas responde 404 si la categoria no existe")
    void publicarEnCategoriaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/temas").header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TemaRequest(9999L, "Un titulo valido", "Un contenido suficientemente largo."))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoria no encontrada"));
    }

    @Test
    @DisplayName("GET /api/temas es publico, viene paginado y admite filtros")
    void buscarTemasEsPublico() throws Exception {
        publicarTema(tokenFeliks, "Mi primer sub-20 con CFOP");
        publicarTema(tokenYiheng, "CFOP o Roux para bajar de 15");
        publicarTema(tokenFeliks, "Resena del GAN 14");

        mockMvc.perform(get("/api/temas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.contenido.length()").value(3));

        mockMvc.perform(get("/api/temas").param("texto", "cfop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));

        mockMvc.perform(get("/api/temas").param("autor", "feliks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));

        mockMvc.perform(get("/api/temas")
                        .param("categoria", String.valueOf(idCategoria))
                        .param("texto", "gan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].titulo").value("Resena del GAN 14"));

        mockMvc.perform(get("/api/temas").param("tamano", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2));
    }

    @Test
    @DisplayName("GET /api/temas/{id} trae el detalle con sus comentarios")
    void detalleDeTema() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Mi primer sub-20");
        mockMvc.perform(post("/api/temas/{id}/comentarios", idTema)
                        .header("Authorization", tokenYiheng)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ComentarioRequest("Felicitaciones, buen avance."))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/temas/{id}", idTema))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Mi primer sub-20"))
                .andExpect(jsonPath("$.comentarios.length()").value(1))
                .andExpect(jsonPath("$.comentarios[0].autorNombre").value("yiheng"));
    }

    @Test
    @DisplayName("GET /api/temas/{id} responde 404 si el tema no existe")
    void detalleDeTemaInexistente() throws Exception {
        mockMvc.perform(get("/api/temas/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Tema no encontrado"));
    }

    @Test
    @DisplayName("PUT /api/temas/{id}: el autor edita el suyo, otro usuario recibe 403")
    void editarTema() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Titulo original");
        String cuerpo = json(new TemaRequest(idCategoria, "Titulo corregido", "Contenido corregido y suficiente."));

        mockMvc.perform(put("/api/temas/{id}", idTema).header("Authorization", tokenYiheng)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Operacion no permitida"));

        mockMvc.perform(put("/api/temas/{id}", idTema).header("Authorization", tokenFeliks)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo corregido"));
    }

    @Test
    @DisplayName("DELETE /api/temas/{id}: el ADMIN borra cualquier tema")
    void borrarTema() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Tema a moderar");

        mockMvc.perform(delete("/api/temas/{id}", idTema).header("Authorization", tokenYiheng))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/temas/{id}", idTema).header("Authorization", tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/temas/{id}", idTema))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- comentarios

    @Test
    @DisplayName("POST /api/temas/{id}/comentarios exige token")
    void comentarExigeToken() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Mi primer sub-20");
        String cuerpo = json(new ComentarioRequest("Comentario de prueba."));

        mockMvc.perform(post("/api/temas/{id}/comentarios", idTema)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/temas/{id}/comentarios", idTema)
                        .header("Authorization", tokenYiheng)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorNombre").value("yiheng"))
                .andExpect(jsonPath("$.temaId").value(idTema));
    }

    @Test
    @DisplayName("GET /api/temas/{id}/comentarios es publico")
    void listarComentariosEsPublico() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Mi primer sub-20");
        mockMvc.perform(post("/api/temas/{id}/comentarios", idTema)
                        .header("Authorization", tokenYiheng)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ComentarioRequest("Un comentario publico."))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/temas/{id}/comentarios", idTema))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("DELETE /api/comentarios/{id}: el autor puede, un tercero no")
    void borrarComentario() throws Exception {
        Long idTema = publicarTema(tokenFeliks, "Mi primer sub-20");
        String respuesta = mockMvc.perform(post("/api/temas/{id}/comentarios", idTema)
                        .header("Authorization", tokenYiheng)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ComentarioRequest("Comentario de yiheng."))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long idComentario = objectMapper.readTree(respuesta).get("id").asLong();

        mockMvc.perform(delete("/api/comentarios/{id}", idComentario).header("Authorization", tokenFeliks))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/comentarios/{id}", idComentario).header("Authorization", tokenYiheng))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/estado responde 200 y es publico")
    void getEstadoEsPublico() throws Exception {
        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OK"))
                .andExpect(jsonPath("$.servicio").value("foro-temas"))
                .andExpect(jsonPath("$.baseDeDatos.conectada").value(true));
    }
}
