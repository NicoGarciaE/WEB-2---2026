package com.example.demo.dto;

import java.util.List;

/**
 * Envoltorio para devolver listas paginadas de cualquier tipo (T).
 */
public record PaginaDTO<T>(
        List<T> contenido,
        int total,
        int salto,
        int limite
) {
}
