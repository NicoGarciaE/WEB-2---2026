package com.example.demo.repository;

import com.example.demo.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación en memoria del repositorio de Favoritos usando un ConcurrentHashMap.
 * Esto asegura que las operaciones concurrentes (típicas de las peticiones web) sean seguras.
 */
@Repository
public class FavoritoRepositoryInMemoryImpl implements FavoritoRepository {

    private final ConcurrentHashMap<Long, Favorito> dataStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            // Es una creación, generamos un nuevo ID simulando el autoincremental
            long nuevoId = idGenerator.getAndIncrement();
            favorito.setId(nuevoId);
        }
        // Tanto para crear como para actualizar, put() en el HashMap reemplaza si ya existe
        dataStore.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return Optional.ofNullable(dataStore.get(id));
    }

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(dataStore.values());
    }

    @Override
    public void deleteById(Long id) {
        dataStore.remove(id);
    }
}
