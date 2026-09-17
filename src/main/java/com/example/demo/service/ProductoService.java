package com.example.demo.service;

import com.example.demo.client.dummyjson.DummyJsonProducto;
import com.example.demo.client.dummyjson.DummyJsonProductosResponse;
import com.example.demo.dto.PaginaDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ServicioExternoException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final RestClient restClient;

    public ProductoService(@Qualifier("dummyJsonRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PaginaDTO<ProductoDTO> obtenerProductos(int limit, int skip) {
        DummyJsonProductosResponse response;
        try {
            response = restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductosResponse.class);
        } catch (RestClientException ex) {
            throw new ServicioExternoException("Error de red al intentar obtener productos desde DummyJSON.", ex);
        }

        if (response == null || response.products() == null) {
            return new PaginaDTO<>(List.of(), 0, skip, limit);
        }

        List<ProductoDTO> productosMapeados = response.products().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PaginaDTO<>(
                productosMapeados,
                response.total(),
                response.skip(),
                response.limit()
        );
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        DummyJsonProducto externalResponse;
        try {
            externalResponse = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new RecursoNoEncontradoException("Producto no encontrado en DummyJSON con ID: " + id);
                    })
                    .body(DummyJsonProducto.class);
        } catch (RecursoNoEncontradoException ex) {
            throw ex; // Lo relanzamos tal cual para que el ExceptionHandler devuelva 404
        } catch (RestClientException ex) {
            throw new ServicioExternoException("Error de red al intentar obtener producto " + id + " desde DummyJSON.", ex);
        }

        if (externalResponse == null) {
            throw new RecursoNoEncontradoException("Producto no encontrado en DummyJSON con ID: " + id);
        }

        return mapToDTO(externalResponse);
    }

    private ProductoDTO mapToDTO(DummyJsonProducto external) {
        return new ProductoDTO(
                external.id(),
                external.title(),
                external.description(),
                external.category(),
                external.brand(),
                external.price(),
                external.thumbnail()
        );
    }
}
