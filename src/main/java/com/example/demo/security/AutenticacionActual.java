package com.example.demo.security;

import com.example.demo.exception.CredencialesInvalidasException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AutenticacionActual {

    private AutenticacionActual() {
    }

    public static UsuarioAutenticado obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new CredencialesInvalidasException("No hay un usuario autenticado");
        }
        return usuario;
    }
}

