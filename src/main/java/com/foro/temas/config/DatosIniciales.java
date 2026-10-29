package com.foro.temas.config;

import com.foro.temas.model.Categoria;
import com.foro.temas.model.Comentario;
import com.foro.temas.model.Tema;
import com.foro.temas.repository.CategoriaRepository;
import com.foro.temas.repository.TemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga las seis categorias del foro y un conjunto de temas y comentarios de
 * ejemplo al iniciar con el perfil "local" (H2 en memoria), para poder probar
 * de inmediato las busquedas en Postman.
 *
 * No se ejecuta con el perfil "oracle": alli los datos los carga
 * scripts/02_datos_iniciales.sql y los administra la base real.
 *
 * Los autores corresponden a los usuarios que siembra foro-usuarios en su propio
 * perfil local, de modo que los dos servicios cuenten la misma historia.
 */
@Component
@Profile("local")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final CategoriaRepository categoriaRepository;
    private final TemaRepository temaRepository;

    public DatosIniciales(CategoriaRepository categoriaRepository, TemaRepository temaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.temaRepository = temaRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria tresPorTres = guardar("Cubo 3x3",
                "El clásico de 3x3x3: métodos, tiempos y primeros pasos.", "cubo-3x3");
        Categoria bigCubes = guardar("Big cubes",
                "4x4, 5x5 y superiores: reducción, paridades y hardware.", "big-cubes");
        Categoria metodos = guardar("Métodos y algoritmos",
                "CFOP, Roux, ZZ, Petrus y la discusión eterna sobre cuál es mejor.", "metodos-y-algoritmos");
        Categoria noCubicos = guardar("Puzzles no cúbicos",
                "Megaminx, Pyraminx, Skewb, Square-1 y todo lo que no es un cubo.", "puzzles-no-cubicos");
        Categoria competencias = guardar("Competencias WCA",
                "Torneos oficiales, reglamento, récords y experiencias compitiendo.", "competencias-wca");
        Categoria hardware = guardar("Hardware y mods",
                "Cubos magnéticos, lubricantes, tensiones y modificaciones.", "hardware-y-mods");

        Tema subVeinte = tema(tresPorTres, 3L, "feliks", "Por fin bajé de 20 segundos",
                "Llevaba tres meses estancado en 24 segundos y esta semana promedié 19.8. "
                        + "Lo que me desbloqueó fue dejar de mirar el cubo entre pasos.");
        subVeinte.agregarComentario(comentario(subVeinte, 4L, "yiheng",
                "Felicitaciones. El look-ahead es justo donde se gana el tiempo."));
        subVeinte.agregarComentario(comentario(subVeinte, 2L, "moderadora",
                "Buen hito. ¿Con qué método estás armando la cruz?"));

        Tema cfopVsRoux = tema(metodos, 4L, "yiheng", "¿CFOP o Roux para bajar de 15?",
                "Promedio 17 segundos con CFOP y siento que el F2L me limita. "
                        + "¿Vale la pena aprender Roux desde cero a esta altura?");
        cfopVsRoux.agregarComentario(comentario(cfopVsRoux, 3L, "feliks",
                "Cambiar de método cuesta unos dos meses de retroceso. Yo apuraría el F2L primero."));
        cfopVsRoux.agregarComentario(comentario(cfopVsRoux, 1L, "admin",
                "Hay sub-10 con los dos métodos. El método no es el cuello de botella a los 17 segundos."));
        cfopVsRoux.agregarComentario(comentario(cfopVsRoux, 2L, "moderadora",
                "Dejo fijado el tema: la pregunta se repite cada semana."));

        Tema oll = tema(metodos, 3L, "feliks", "Memorizar las 57 OLL en un mes",
                "Comparto el plan que usé: seis algoritmos nuevos por día, "
                        + "repaso de los anteriores y nada de aprender dos simétricos el mismo día.");
        oll.agregarComentario(comentario(oll, 4L, "yiheng",
                "Lo de no mezclar simétricos el mismo día es el mejor consejo del post."));

        Tema paridad = tema(bigCubes, 4L, "yiheng", "La paridad del 4x4 me sigue costando",
                "Entiendo por qué ocurre pero sigo ejecutando el algoritmo de OLL parity muy lento. "
                        + "¿Alguna variante más corta que recomienden?");
        paridad.agregarComentario(comentario(paridad, 1L, "admin",
                "Practica la versión de Lucas Parity. Es más larga de leer pero más cómoda de ejecutar."));

        Tema megaminx = tema(noCubicos, 2L, "moderadora", "Megaminx: cómo memorizar la última capa",
                "La última capa del Megaminx asusta por la cantidad de casos, "
                        + "pero con los algoritmos de 2-look se resuelve con muy poca memorización.");
        megaminx.agregarComentario(comentario(megaminx, 3L, "feliks",
                "El 2-look es lo correcto para empezar. Nadie aprende el full de entrada."));

        Tema nacional = tema(competencias, 1L, "admin", "Nacional WCA Chile 2026: organización",
                "Abrimos la convocatoria de jueces y runners para el nacional. "
                        + "No hace falta experiencia previa, se capacita en el lugar.");
        nacional.agregarComentario(comentario(nacional, 4L, "yiheng",
                "Me anoto como juez. ¿Hay que llevar algo?"));
        nacional.agregarComentario(comentario(nacional, 3L, "feliks",
                "Primera vez que compito. ¿Se puede usar cubo propio?"));

        Tema reglamento = tema(competencias, 2L, "moderadora", "Las tres reglas que más descalifican",
                "Repaso rápido del reglamento: inspección de 15 segundos, "
                        + "detener el timer con las manos planas y no soltar el cubo antes de parar.");

        Tema ganCatorce = tema(hardware, 3L, "feliks", "Reseña del GAN 14 con MagLev",
                "Tres semanas con el GAN 14. El MagLev se siente raro los primeros días "
                        + "y después no quieres volver a los resortes tradicionales.");
        ganCatorce.agregarComentario(comentario(ganCatorce, 4L, "yiheng",
                "¿Cómo se comporta en giros rápidos? Mi miedo es que se pase de vuelta."));
        ganCatorce.agregarComentario(comentario(ganCatorce, 1L, "admin",
                "Buena reseña. Agrego que el precio sigue siendo difícil de justificar."));

        Tema lubricantes = tema(hardware, 4L, "yiheng", "Lubricantes: Angstrom contra DNM-37",
                "Probé los dos en el mismo cubo durante dos semanas cada uno. "
                        + "Mi conclusión es que la diferencia se nota más en el core que en las piezas.");
        lubricantes.agregarComentario(comentario(lubricantes, 3L, "feliks",
                "Coincido. En las piezas casi no se nota después del primer día."));

        temaRepository.saveAll(List.of(
                subVeinte, cfopVsRoux, oll, paridad, megaminx,
                nacional, reglamento, ganCatorce, lubricantes));

        log.info("Perfil local: {} categorias y {} temas de ejemplo cargados",
                categoriaRepository.count(), temaRepository.count());
    }

    private Categoria guardar(String nombre, String descripcion, String slug) {
        return categoriaRepository.save(new Categoria(null, nombre, descripcion, slug));
    }

    private Tema tema(Categoria categoria, Long autorId, String autorNombre,
                      String titulo, String contenido) {
        return new Tema(null, categoria, autorId, autorNombre, titulo, contenido);
    }

    private Comentario comentario(Tema tema, Long autorId, String autorNombre, String contenido) {
        return new Comentario(null, tema, autorId, autorNombre, contenido);
    }
}
