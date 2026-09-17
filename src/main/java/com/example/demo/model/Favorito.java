package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para representar un Favorito.
 * No usamos anotaciones de JPA porque esta entidad se guardará en memoria (TP1).
 */
public class Favorito {
    
    private Long id;
    private Long productoId;
    private String notaPersonal;
    private LocalDateTime fechaCreacion;

    public Favorito() {
    }

    public Favorito(Long id, Long productoId, String notaPersonal, LocalDateTime fechaCreacion) {
        this.id = id;
        this.productoId = productoId;
        this.notaPersonal = notaPersonal;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNotaPersonal() {
        return notaPersonal;
    }

    public void setNotaPersonal(String notaPersonal) {
        this.notaPersonal = notaPersonal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
