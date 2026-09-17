package com.example.demo.controller;

import com.example.demo.dto.PaginaDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.service.ProductoService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Obtener lista de productos paginada")
    @GetMapping
    public PaginaDTO<ProductoDTO> getProductos(
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return productoService.obtenerProductos(limit, skip);
    }

    @Operation(summary = "Obtener un producto por ID")
    @GetMapping("/{id}")
    public ProductoDTO getProducto(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id);
    }
}
