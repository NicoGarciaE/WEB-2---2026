package com.example.demo.controller;

import com.example.demo.dto.FavoritoRequestDTO;
import com.example.demo.dto.FavoritoResponseDTO;
import com.example.demo.service.FavoritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @Operation(summary = "Crear un nuevo favorito")
    @PostMapping
    public ResponseEntity<FavoritoResponseDTO> crear(@Valid @RequestBody FavoritoRequestDTO requestDTO) {
        FavoritoResponseDTO creado = favoritoService.crearFavorito(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado); // 201 Created
    }

    @Operation(summary = "Obtener la lista de todos los favoritos")
    @GetMapping
    public List<FavoritoResponseDTO> listarTodos() {
        return favoritoService.obtenerTodos(); // 200 OK por defecto
    }

    @Operation(summary = "Obtener un favorito por ID")
    @GetMapping("/{id}")
    public FavoritoResponseDTO obtenerUno(@PathVariable Long id) {
        return favoritoService.obtenerPorId(id); // 200 OK por defecto
    }

    @Operation(summary = "Actualizar un favorito existente")
    @PutMapping("/{id}")
    public FavoritoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody FavoritoRequestDTO requestDTO) {
        return favoritoService.actualizarFavorito(id, requestDTO); // 200 OK por defecto
    }

    @Operation(summary = "Eliminar un favorito por ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void eliminar(@PathVariable Long id) {
        favoritoService.eliminarFavorito(id);
    }
}
