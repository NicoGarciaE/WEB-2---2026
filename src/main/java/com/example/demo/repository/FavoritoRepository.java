package com.example.demo.repository;

import com.example.demo.model.Favorito;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define las operaciones de persistencia para los Favoritos.
 * Permite cambiar la implementación (ej. de memoria a base de datos real) sin afectar la capa de servicio.
 */
public interface FavoritoRepository {
    
    Favorito save(Favorito favorito);
    
    Optional<Favorito> findById(Long id);
    
    List<Favorito> findAll();
    
    void deleteById(Long id);
}
