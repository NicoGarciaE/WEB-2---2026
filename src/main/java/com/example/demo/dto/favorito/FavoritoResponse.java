package com.example.demo.dto.favorito;

import java.time.LocalDateTime;

/**
 * DTO de salida: lo que la API devuelve. Sin anotaciones de validación —
 * no tiene sentido validar lo que la propia API produce.
 */
public record FavoritoResponse(
        Long id,
        Long productoId,
        String nota,
        LocalDateTime fechaAgregado,
        Long listaId
) {
}
