package com.example.demo.repository;

import com.example.demo.domain.Lista;
import java.util.List;
import java.util.Optional;

public interface ListaRepository {
    Lista save(Lista lista);
    List<Lista> findAll();
    List<Lista> findByUsuarioId(Long usuarioId);
    Optional<Lista> findById(Long id);
    void deleteById(Long id);
}
