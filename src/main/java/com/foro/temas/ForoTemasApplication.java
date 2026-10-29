package com.foro.temas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de contenido del foro.
 *
 * Responsabilidades: categorias, publicacion y busqueda de temas, y comentarios.
 * No administra usuarios: confia en el JWT que emite foro-usuarios.
 */
@SpringBootApplication
public class ForoTemasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForoTemasApplication.class, args);
    }
}
