package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa la información que envía el cliente para crear o actualizar un Favorito.
 * Solo pedimos lo estrictamente necesario.
 */
public record FavoritoRequestDTO(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,
        
        @NotBlank(message = "La nota personal no puede estar vacía")
        @Size(max = 255, message = "La nota personal no puede superar los 255 caracteres")
        String notaPersonal
) {
}
