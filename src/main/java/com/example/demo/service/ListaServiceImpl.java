package com.example.demo.service;

import com.example.demo.domain.Favorito;
import com.example.demo.domain.Lista;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.exception.ListaNoVaciaException;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.AutenticacionActual;
import com.example.demo.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaServiceImpl(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    public List<ListaResponse> listar() {
        UsuarioAutenticado actual = AutenticacionActual.obtener();
        List<Lista> listas = actual.esAdmin()
                ? listaRepository.findAll()
                : listaRepository.findByUsuarioId(actual.id());

        return listas.stream()
                .map(this::aResponse)
                .toList();
    }

    @Override
    public ListaResponse obtener(Long id) {
        Lista lista = buscarOFallar(id);
        verificarAcceso(lista);
        return aResponse(lista);
    }

    @Override
    public ListaResponse crear(ListaRequest request) {
        UsuarioAutenticado actual = AutenticacionActual.obtener();
        Lista nueva = new Lista(null, request.nombre(), actual.id());
        return aResponse(listaRepository.save(nueva));
    }

    @Override
    public List<FavoritoResponse> obtenerFavoritos(Long id) {
        Lista lista = buscarOFallar(id);
        verificarAcceso(lista);
        return favoritoRepository.findByListaId(id).stream()
                .map(this::aFavoritoResponse)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        Lista lista = buscarOFallar(id);
        verificarAcceso(lista);

        List<Favorito> favoritos = favoritoRepository.findByListaId(id);
        if (!favoritos.isEmpty()) {
            throw new ListaNoVaciaException("La lista no está vacía, no se puede eliminar");
        }
        listaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        Lista origen = buscarOFallar(origenId);
        verificarAcceso(origen);

        Lista destino = buscarOFallar(destinoId);
        verificarAcceso(destino);

        List<Favorito> favoritos = favoritoRepository.findByListaId(origenId);
        for (Favorito f : favoritos) {
            Favorito actualizado = new Favorito(
                    f.id(),
                    f.productoId(),
                    f.nota(),
                    f.fechaAgregado(),
                    destino.id()
            );
            favoritoRepository.save(actualizado);
        }

        listaRepository.deleteById(origen.id());
    }

    private Lista buscarOFallar(Long id) {
        return listaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lista con id " + id));
    }

    private void verificarAcceso(Lista lista) {
        UsuarioAutenticado actual = AutenticacionActual.obtener();
        if (!actual.esAdmin() && !Objects.equals(lista.usuarioId(), actual.id())) {
            throw new AccesoDenegadoException("No tenés permisos para operar sobre la lista con id " + lista.id());
        }
    }

    private ListaResponse aResponse(Lista lista) {
        return new ListaResponse(lista.id(), lista.nombre(), lista.usuarioId());
    }

    private FavoritoResponse aFavoritoResponse(Favorito favorito) {
        return new FavoritoResponse(
                favorito.id(),
                favorito.productoId(),
                favorito.nota(),
                favorito.fechaAgregado(),
                favorito.listaId());
    }
}
