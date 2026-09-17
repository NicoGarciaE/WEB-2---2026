package com.example.demo.dto;

/**
 * DTO que expone nuestra propia API para los productos.
 * Mapeamos solo lo que nos interesa y con nombres en español.
 */
public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        String marca,
        Double precio,
        String imagen
) {
}
