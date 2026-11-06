package com.example.demo.repository;

import com.example.demo.domain.Lista;
import com.example.demo.entity.ListaEntity;
import com.example.demo.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository listaJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository listaJpaRepository) {
        this.listaJpaRepository = listaJpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity entity = toEntity(lista);
        ListaEntity savedEntity = listaJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public List<Lista> findAll() {
        return listaJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return listaJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return listaJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        listaJpaRepository.deleteById(id);
    }

    private ListaEntity toEntity(Lista lista) {
        UsuarioEntity usuario = null;
        if (lista.usuarioId() != null) {
            usuario = new UsuarioEntity();
            usuario.setId(lista.usuarioId());
        }
        return new ListaEntity(lista.id(), lista.nombre(), usuario);
    }

    private Lista toDomain(ListaEntity entity) {
        Long usuarioId = entity.getUsuario() != null ? entity.getUsuario().getId() : null;
        return new Lista(entity.getId(), entity.getNombre(), usuarioId);
    }
}
