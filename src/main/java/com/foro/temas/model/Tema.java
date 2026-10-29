package com.foro.temas.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad JPA que representa un tema publicado en el foro.
 *
 * AUTOR_ID y AUTOR_NOMBRE se copian del JWT que emite foro-usuarios y no llevan
 * relacion JPA con la entidad Usuario: cada microservicio mantiene su propio
 * modelo y no se acopla al del otro (ver docs/arquitectura.md).
 *
 * Se mapea a la tabla TEMA (ver scripts/01_crear_tablas.sql).
 */
@Entity
@Table(name = "TEMA")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tema_seq_gen")
    @SequenceGenerator(name = "tema_seq_gen", sequenceName = "TEMA_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private Categoria categoria;

    @Column(name = "AUTOR_ID", nullable = false)
    private Long autorId;

    @Column(name = "AUTOR_NOMBRE", nullable = false, length = 50)
    private String autorNombre;

    @Column(name = "TITULO", nullable = false, length = 200)
    private String titulo;

    @Column(name = "CONTENIDO", nullable = false, length = 4000)
    private String contenido;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * 1 si el tema esta cerrado a nuevos comentarios, 0 si sigue abierto.
     *
     * Igual que USUARIO.ACTIVO, la columna es NUMBER(1) y necesita el
     * convertidor numerico para que el mapeo valide contra Oracle 23, que tiene
     * un tipo BOOLEAN nativo distinto.
     */
    @Column(name = "CERRADO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean cerrado = false;

    /**
     * Comentarios del tema. Se cargan en diferido porque el listado de temas no
     * los necesita; solo el detalle de un tema los recorre.
     */
    @OneToMany(mappedBy = "tema", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fechaCreacion ASC")
    private List<Comentario> comentarios = new ArrayList<>();

    protected Tema() {
        // Constructor requerido por JPA.
    }

    public Tema(Long id, Categoria categoria, Long autorId, String autorNombre,
                String titulo, String contenido) {
        this.id = id;
        this.categoria = categoria;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.titulo = titulo;
        this.contenido = contenido;
    }

    /** La fecha de publicacion la fija la aplicacion, no el cliente. */
    @PrePersist
    void alInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Agrega un comentario manteniendo las dos puntas de la relacion.
     *
     * Es imprescindible pasar siempre por aqui: si solo se fija el tema dentro
     * del comentario, la coleccion de este lado queda desactualizada y el
     * borrado en cascada del tema no encuentra sus comentarios.
     */
    public void agregarComentario(Comentario comentario) {
        comentario.setTema(this);
        this.comentarios.add(comentario);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public boolean isCerrado() {
        return cerrado;
    }

    public void setCerrado(boolean cerrado) {
        this.cerrado = cerrado;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios == null ? new ArrayList<>() : comentarios;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tema otro)) {
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
        return "Tema{id=" + id + ", titulo='" + titulo + '\'' + ", autorNombre='" + autorNombre + '\'' + '}';
    }
}
