package com.foro.temas.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de salud del microservicio.
 *
 * GET /api/estado responde 200 con el estado del servicio y confirma que la
 * conexion a la base de datos esta operativa, informando el motor real. Sirve
 * para distinguir de un vistazo si se esta corriendo contra H2 o contra Oracle.
 */
@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    private final JdbcTemplate jdbcTemplate;
    private final String nombreAplicacion;

    public EstadoController(JdbcTemplate jdbcTemplate,
                            @Value("${spring.application.name:foro-temas}") String nombreAplicacion) {
        this.jdbcTemplate = jdbcTemplate;
        this.nombreAplicacion = nombreAplicacion;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> estado() {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", "OK");
        cuerpo.put("servicio", nombreAplicacion);
        cuerpo.put("baseDeDatos", describirBaseDeDatos());
        return ResponseEntity.ok(cuerpo);
    }

    private Map<String, Object> describirBaseDeDatos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        try {
            datos.put("conectada", true);
            datos.put("motor", jdbcTemplate.execute((ConnectionCallback<String>) conexion ->
                    conexion.getMetaData().getDatabaseProductName()
                            + " " + conexion.getMetaData().getDatabaseProductVersion()));
        } catch (RuntimeException e) {
            datos.put("conectada", false);
            datos.put("detalle", e.getMessage());
        }
        return datos;
    }
}
