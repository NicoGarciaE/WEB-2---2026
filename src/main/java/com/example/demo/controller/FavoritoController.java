package com.example.demo.controller;

import com.example.demo.config.OpenApiConfig;
import com.example.demo.dto.favorito.FavoritoRequest;
import com.example.demo.dto.favorito.FavoritoResponse;
import com.example.demo.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * CRUD completo de favoritos persistido en PostgreSQL. Requiere autenticación
 * Bearer JWT y verifica que la lista asociada pertenezca al usuario autenticado
 * (salvo rol ADMIN).
 */
@RestController
@RequestMapping("/api/favoritos")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@Tag(
        name = "Favoritos",
        description = "Favoritos del usuario sobre el catálogo de productos. Requiere autenticación (Bearer JWT): solo permite ver y modificar favoritos de listas propias (o cualquiera si es ADMIN)."
)
public class FavoritoController {

    private final FavoritoService service;

    public FavoritoController(FavoritoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Listar favoritos",
            description = "Devuelve los favoritos pertenecientes a las listas del usuario autenticado (o todos si es ADMIN). Responde 401 sin token válido."
    )
    public List<FavoritoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener un favorito por id",
            description = "Responde 401 sin token, 403 si pertenece a una lista ajena y 404 si no existe."
    )
    public FavoritoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear un favorito",
            description = "Agrega un favorito en una lista propia (o cualquiera si es ADMIN). Responde 401 sin token, 403 si la lista es ajena y 404 si la lista no existe."
    )
    public ResponseEntity<FavoritoResponse> crear(@Valid @RequestBody FavoritoRequest request) {
        FavoritoResponse creado = service.crear(request);
        return ResponseEntity.created(URI.create("/api/favoritos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un favorito",
            description = "Actualiza un favorito verificando propiedad tanto sobre la lista actual como sobre la nueva lista destino. Responde 401/403/404 según corresponda."
    )
    public FavoritoResponse actualizar(@PathVariable Long id, @Valid @RequestBody FavoritoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un favorito",
            description = "Elimina un favorito de una lista propia (o cualquiera si es ADMIN). Responde 401 sin token, 403 si es ajeno y 404 si no existe."
    )
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
