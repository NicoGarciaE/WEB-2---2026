package com.example.demo.controller;

import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Operaciones sobre las listas de favoritos")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @PostMapping
    @Operation(summary = "Crear lista", description = "Crea una nueva lista de favoritos")
    public ResponseEntity<ListaResponse> crear(@RequestBody @Valid ListaRequest request) {
        ListaResponse response = listaService.crear(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar listas", description = "Devuelve todas las listas de favoritos")
    public ResponseEntity<List<ListaResponse>> listar() {
        return ResponseEntity.ok(listaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una lista", description = "Devuelve una lista puntual por id")
    public ResponseEntity<ListaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtener(id));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Favoritos de una lista", description = "Devuelve los favoritos de una lista puntual")
    public ResponseEntity<List<FavoritoResponse>> obtenerFavoritos(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerFavoritos(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una lista vacía", description = "Elimina una lista puntual si no tiene favoritos")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(summary = "Mover favoritos de una lista a otra", description = "Mueve los favoritos de la lista origen a la lista destino, y elimina la lista origen")
    public ResponseEntity<Void> moverFavoritos(@PathVariable Long origenId, @RequestBody Long destinoId) {
        listaService.moverFavoritos(origenId, destinoId);
        return ResponseEntity.noContent().build();
    }
}
