package com.example.demo.security;

import com.example.demo.domain.Rol;

public record UsuarioAutenticado(
        Long id,
        String email,
        Rol rol
) {
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}

