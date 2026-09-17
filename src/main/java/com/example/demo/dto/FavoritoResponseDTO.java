package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa la información de un Favorito tal como la devuelve la API al cliente.
 * Contiene identificadores internos y fechas que el cliente no puede modificar directamente.
 */
public record FavoritoResponseDTO(
        Long id,
        Long productoId,
        String notaPersonal,
        LocalDateTime fechaCreacion
) {
}
