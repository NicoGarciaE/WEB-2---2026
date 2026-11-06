package com.example.demo.service;

import com.example.demo.domain.Favorito;
import com.example.demo.domain.Lista;
import com.example.demo.dto.favorito.FavoritoRequest;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.AutenticacionActual;
import com.example.demo.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository repository;
    private final ListaRepository listaRepository;

    public FavoritoServiceImpl(FavoritoRepository repository, ListaRepository listaRepository) {
        this.repository = repository;
        this.listaRepository = listaRepository;
    }

    @Override
    public List<FavoritoResponse> listar() {
        UsuarioAutenticado actual = AutenticacionActual.obtener();
        if (actual.esAdmin()) {
            return repository.findAll().stream()
                    .map(this::aResponse)
                    .toList();
        }

        Set<Long> misListaIds = listaRepository.findByUsuarioId(actual.id()).stream()
                .map(Lista::id)
                .collect(Collectors.toSet());

        return repository.findAll().stream()
                .filter(f -> misListaIds.contains(f.listaId()))
                .map(this::aResponse)
                .toList();
    }

    @Override
    public FavoritoResponse obtener(Long id) {
        Favorito favorito = buscarOFallar(id);
        verificarAccesoALista(favorito.listaId());
        return aResponse(favorito);
    }

    @Override
    public FavoritoResponse crear(FavoritoRequest request) {
        verificarAccesoALista(request.listaId());
        Favorito nuevo = new Favorito(
                null,
                request.productoId(),
                request.nota(),
                LocalDateTime.now(),
                request.listaId());
        return aResponse(repository.save(nuevo));
    }

    @Override
    public FavoritoResponse actualizar(Long id, FavoritoRequest request) {
        Favorito existente = buscarOFallar(id);
        verificarAccesoALista(existente.listaId());
        verificarAccesoALista(request.listaId());

        Favorito actualizado = new Favorito(
                existente.id(),
                request.productoId(),
                request.nota(),
                existente.fechaAgregado(),
                request.listaId()
        );
        return aResponse(repository.save(actualizado));
    }

    @Override
    public void eliminar(Long id) {
        Favorito existente = buscarOFallar(id);
        verificarAccesoALista(existente.listaId());
        repository.deleteById(id);
    }

    private Favorito buscarOFallar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el favorito con id " + id));
    }

    private void verificarAccesoALista(Long listaId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista con id " + listaId));
        UsuarioAutenticado actual = AutenticacionActual.obtener();
        if (!actual.esAdmin() && !Objects.equals(lista.usuarioId(), actual.id())) {
            throw new AccesoDenegadoException("No tenés permisos para operar sobre la lista con id " + listaId);
        }
    }

    private FavoritoResponse aResponse(Favorito favorito) {
        return new FavoritoResponse(
                favorito.id(),
                favorito.productoId(),
                favorito.nota(),
                favorito.fechaAgregado(),
                favorito.listaId());
    }
}
