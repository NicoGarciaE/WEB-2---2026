package com.example.demo.repository;

import com.example.demo.domain.Favorito;
import com.example.demo.entity.FavoritoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository jpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Favorito> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Favorito save(Favorito favorito) {
        FavoritoEntity entity = toEntity(favorito);
        FavoritoEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return jpaRepository.findByListaId(listaId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Favorito toDomain(FavoritoEntity entity) {
        Long listaId = entity.getLista() != null ? entity.getLista().getId() : null;
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getNota(),
                entity.getFechaAlta(),
                listaId
        );
    }

    private FavoritoEntity toEntity(Favorito domain) {
        FavoritoEntity entity = new FavoritoEntity(
                domain.id(),
                domain.productoId(),
                domain.nota(),
                domain.fechaAgregado(),
                null
        );
        if (domain.listaId() != null) {
            com.example.demo.entity.ListaEntity lista = new com.example.demo.entity.ListaEntity();
            lista.setId(domain.listaId());
            entity.setLista(lista);
        }
        return entity;
    }
}
