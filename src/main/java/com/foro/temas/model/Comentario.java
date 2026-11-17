package com.foro.temas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.type.NumericBooleanConverter;

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

    /**
     * 1 si un moderador retiro el contenido por incumplir las normas del foro.
     *
     * El contenido baneado no se borra: deja de mostrarse en los listados y en
     * el detalle publico, pero queda en la base como evidencia y se puede
     * restituir si la sancion se levanta. Lo cambia el microservicio
     * foro-moderacion a traves de PUT /api/.../moderacion.
     */
    @Column(name = "BANEADO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean baneado = false;

    /** Motivo que dejo el moderador al banear el contenido. Nulo si no esta baneado. */
    @Column(name = "MOTIVO_BANEO", length = 500)
    private String motivoBaneo;

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

    public boolean isBaneado() {
        return baneado;
    }

    public void setBaneado(boolean baneado) {
        this.baneado = baneado;
    }

    public String getMotivoBaneo() {
        return motivoBaneo;
    }

    public void setMotivoBaneo(String motivoBaneo) {
        this.motivoBaneo = motivoBaneo;
    }

    /** Retira el contenido del foro dejando constancia del motivo. */
    public void banear(String motivo) {
        this.baneado = true;
        this.motivoBaneo = motivo;
    }

    /** Restituye el contenido cuando se levanta la sancion. */
    public void restituir() {
        this.baneado = false;
        this.motivoBaneo = null;
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
