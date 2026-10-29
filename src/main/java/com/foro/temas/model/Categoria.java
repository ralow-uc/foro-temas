package com.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entidad JPA que representa una categoria de temas del foro.
 *
 * El foro de cubos y twisty puzzles tiene seis. Se mapea a la tabla CATEGORIA
 * (ver scripts/01_crear_tablas.sql).
 */
@Entity
@Table(name = "CATEGORIA")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categoria_seq_gen")
    @SequenceGenerator(name = "categoria_seq_gen", sequenceName = "CATEGORIA_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 100, unique = true)
    private String nombre;

    @Column(name = "DESCRIPCION", nullable = false, length = 400)
    private String descripcion;

    /** Identificador legible para URL, sin espacios ni acentos. */
    @Column(name = "SLUG", nullable = false, length = 100, unique = true)
    private String slug;

    protected Categoria() {
        // Constructor requerido por JPA.
    }

    public Categoria(Long id, String nombre, String descripcion, String slug) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.slug = slug;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categoria otra)) {
            return false;
        }
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Categoria{id=" + id + ", nombre='" + nombre + '\'' + '}';
    }
}
