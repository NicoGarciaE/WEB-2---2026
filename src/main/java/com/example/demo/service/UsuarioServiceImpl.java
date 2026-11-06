package com.example.demo.service;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;
import com.example.demo.exception.EmailYaRegistradoException;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException("Ya existe un usuario registrado con el email " + email);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Usuario nuevo = new Usuario(null, email, passwordHash, Rol.USER);
        try {
            Usuario guardado = usuarioRepository.save(nuevo);
            return new UsuarioResponse(guardado.id(), guardado.email(), guardado.rol());
        } catch (DataIntegrityViolationException ex) {
            throw new EmailYaRegistradoException("Ya existe un usuario registrado con el email " + email);
        }
    }
}

