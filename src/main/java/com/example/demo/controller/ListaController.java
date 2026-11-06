package com.example.demo.controller;

import com.example.demo.config.OpenApiConfig;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(
        name = "Listas",
        description = "Operaciones sobre listas de favoritos. Requiere autenticación (Bearer JWT): cada usuario opera únicamente sobre sus propias listas; el rol ADMIN puede acceder a cualquiera."
)
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @PostMapping
    @Operation(
            summary = "Crear lista",
            description = "Crea una nueva lista asociada automáticamente al usuario autenticado. Responde 401 sin token válido."
    )
    public ResponseEntity<ListaResponse> crear(@RequestBody @Valid ListaRequest request) {
        ListaResponse response = listaService.crear(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Listar listas",
            description = "Devuelve las listas del usuario autenticado (o todas las listas del sistema si el usuario tiene rol ADMIN). Responde 401 sin token válido."
    )
    public ResponseEntity<List<ListaResponse>> listar() {
        return ResponseEntity.ok(listaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una lista por id",
            description = "Devuelve una lista puntual si pertenece al usuario autenticado o es ADMIN. Responde 401 sin token, 403 si es ajena y 404 si no existe."
    )
    public ResponseEntity<ListaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtener(id));
    }

    @GetMapping("/{id}/favoritos")
    @Operation(
            summary = "Favoritos de una lista",
            description = "Devuelve los favoritos de una lista propia (o de cualquiera si es ADMIN). Responde 401 sin token, 403 si es ajena y 404 si no existe."
    )
    public ResponseEntity<List<FavoritoResponse>> obtenerFavoritos(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.obtenerFavoritos(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una lista vacía",
            description = "Elimina una lista propia vacía (o cualquiera si es ADMIN). Responde 401 sin token, 403 si es ajena, 404 si no existe y 409 si aún tiene favoritos."
    )
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(
            summary = "Mover favoritos de una lista a otra",
            description = "Mueve los favoritos de la lista origen a la lista destino y elimina la lista origen en una única transacción. Requiere ser dueño de ambas listas (o ADMIN)."
    )
    public ResponseEntity<Void> moverFavoritos(@PathVariable Long origenId, @RequestBody Long destinoId) {
        listaService.moverFavoritos(origenId, destinoId);
        return ResponseEntity.noContent().build();
    }
}
