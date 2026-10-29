package com.foro.temas.service.impl;

import com.foro.temas.dto.CategoriaRequest;
import com.foro.temas.dto.CategoriaResponse;
import com.foro.temas.exception.CategoriaNoEncontradaException;
import com.foro.temas.exception.DatosDuplicadosException;
import com.foro.temas.model.Categoria;
import com.foro.temas.repository.CategoriaRepository;
import com.foro.temas.repository.ConteoPorCategoria;
import com.foro.temas.repository.TemaRepository;
import com.foro.temas.service.CategoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementacion de la logica de negocio de las categorias del foro.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;
    private final TemaRepository temaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, TemaRepository temaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.temaRepository = temaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        // Una sola consulta agrupada para los conteos, en vez de una por categoria.
        Map<Long, Long> conteos = temaRepository.contarTemasPorCategoria().stream()
                .collect(Collectors.toMap(
                        ConteoPorCategoria::getCategoriaId,
                        ConteoPorCategoria::getCantidad,
                        (primero, segundo) -> primero));

        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(categoria -> CategoriaResponse.desde(
                        categoria, conteos.getOrDefault(categoria.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
        return CategoriaResponse.desde(categoria, temaRepository.countByCategoriaId(id));
    }

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        String slug = solicitud.slug().trim().toLowerCase();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new DatosDuplicadosException("Ya existe una categoria llamada " + nombre);
        }
        if (categoriaRepository.existsBySlug(slug)) {
            throw new DatosDuplicadosException("Ya existe una categoria con el slug " + slug);
        }

        Categoria guardada = categoriaRepository.save(
                new Categoria(null, nombre, solicitud.descripcion().trim(), slug));

        log.debug("Categoria creada: {}", guardada.getNombre());
        return CategoriaResponse.desde(guardada, 0L);
    }
}
