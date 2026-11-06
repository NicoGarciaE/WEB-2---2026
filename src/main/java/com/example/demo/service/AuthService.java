package com.example.demo.service;

import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;

public interface AuthService {
    UsuarioResponse registrar(RegistroRequest request);
    AuthResponse login(LoginRequest request);
}

