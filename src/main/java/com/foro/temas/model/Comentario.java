package com.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad JPA que representa un comentario dentro de un tema del foro.
 *
 * Se mapea a la tabla COMENTARIO (ver scripts/01_crear_tablas.sql).
 */
@Entity
@Table(name = "COMENTARIO")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comentario_seq_gen")
    @SequenceGenerator(name = "comentario_seq_gen", sequenceName = "COMENTARIO_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TEMA_ID", nullable = false)
    private Tema tema;

    @Column(name = "AUTOR_ID", nullable = false)
    private Long autorId;

    @Column(name = "AUTOR_NOMBRE", nullable = false, length = 50)
    private String autorNombre;

    @Column(name = "CONTENIDO", nullable = false, length = 2000)
    private String contenido;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    protected Comentario() {
        // Constructor requerido por JPA.
    }

    public Comentario(Long id, Tema tema, Long autorId, String autorNombre, String contenido) {
        this.id = id;
        this.tema = tema;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.contenido = contenido;
    }

    @PrePersist
    void alInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public void setAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comentario otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Comentario{id=" + id + ", autorNombre='" + autorNombre + '\'' + '}';
    }
}
