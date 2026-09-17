package com.example.demo.service;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.mapper.FavoritoMapper;
import com.example.demo.model.Favorito;
import com.example.demo.repository.FavoritoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final FavoritoMapper favoritoMapper;
    private final ProductoService productoService;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           FavoritoMapper favoritoMapper,
                           ProductoService productoService) {
        this.favoritoRepository = favoritoRepository;
        this.favoritoMapper = favoritoMapper;
        this.productoService = productoService;
    }

    public FavoritoResponseDTO crearFavorito(FavoritoRequestDTO requestDTO) {
        // Validamos que el producto exista en DummyJSON
        // Si no existe, ProductoService lanzará RecursoNoEncontradoException y se corta el flujo aquí
        productoService.obtenerProductoPorId(requestDTO.productoId());

        Favorito entidad = favoritoMapper.toEntity(requestDTO);
        Favorito guardado = favoritoRepository.save(entidad);
        return favoritoMapper.toDTO(guardado);
    }

    public List<FavoritoResponseDTO> obtenerTodos() {
        return favoritoRepository.findAll().stream()
                .map(favoritoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public FavoritoResponseDTO obtenerPorId(Long id) {
        Favorito favorito = buscarEntidadPorId(id);
        return favoritoMapper.toDTO(favorito);
    }

    public FavoritoResponseDTO actualizarFavorito(Long id, FavoritoRequestDTO requestDTO) {
        Favorito existente = buscarEntidadPorId(id);

        // Si cambiaron el productoId, validamos que el nuevo exista
        if (!existente.getProductoId().equals(requestDTO.productoId())) {
            productoService.obtenerProductoPorId(requestDTO.productoId());
            existente.setProductoId(requestDTO.productoId());
        }

        existente.setNotaPersonal(requestDTO.notaPersonal());
        
        Favorito actualizado = favoritoRepository.save(existente);
        return favoritoMapper.toDTO(actualizado);
    }

    public void eliminarFavorito(Long id) {
        // Buscamos primero para asegurarnos de que existe y lanzar 404 si no
        buscarEntidadPorId(id);
        favoritoRepository.deleteById(id);
    }

    private Favorito buscarEntidadPorId(Long id) {
        return favoritoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito no encontrado con ID: " + id));
    }
}
