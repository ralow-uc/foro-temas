package com.foro.temas.service;

import com.foro.temas.dto.CategoriaRequest;
import com.foro.temas.dto.CategoriaResponse;

import java.util.List;

/**
 * Contrato de la capa de servicio de las categorias del foro.
 */
public interface CategoriaService {

    /** Lista las categorias ordenadas por nombre, con la cantidad de temas de cada una. */
    List<CategoriaResponse> listar();

    /** Obtiene una categoria por su ID. */
    CategoriaResponse obtenerPorId(Long id);

    /** Crea una categoria. Operacion reservada a un ADMIN. */
    CategoriaResponse crear(CategoriaRequest solicitud);
}
