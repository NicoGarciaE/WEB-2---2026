package com.example.demo.repository;

import com.example.demo.domain.Favorito;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para favoritos. El service depende de esta
 * interfaz, no de la implementación concreta — hoy es en memoria, pero
 * podría cambiarse por una basada en JPA (TP2) sin tocar el service.
 */
public interface FavoritoRepository {
    List<Favorito> findAll();
    Optional<Favorito> findById(Long id);
    Favorito save(Favorito favorito);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<Favorito> findByListaId(Long listaId);
}
