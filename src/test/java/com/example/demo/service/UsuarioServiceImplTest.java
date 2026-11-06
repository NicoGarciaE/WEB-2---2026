package com.example.demo.service;

import com.example.demo.domain.Rol;
import com.example.demo.domain.Usuario;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.dto.auth.UsuarioResponse;
import com.example.demo.exception.EmailYaRegistradoException;
import com.example.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder);
    }

    @Test
    void registrar_passwordEnTextoPlano_guardaHashYNuncaTextoPlano() {
        String passwordPlano = "secretoSuperSeguro123";
        when(usuarioRepository.existsByEmail("ana@test.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> {
                    Usuario u = inv.getArgument(0);
                    return new Usuario(1L, u.email(), u.passwordHash(), u.rol());
                });

        UsuarioResponse respuesta = usuarioService.registrar(new RegistroRequest("ana@test.com", passwordPlano));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();
        assertThat(guardado.passwordHash()).isNotEqualTo(passwordPlano);
        assertThat(guardado.passwordHash()).isNotBlank();
        assertThat(passwordEncoder.matches(passwordPlano, guardado.passwordHash())).isTrue();
        assertThat(guardado.rol()).isEqualTo(Rol.USER);
        assertThat(respuesta.rol()).isEqualTo(Rol.USER);
    }

    @Test
    void registrar_emailYaExistente_lanzaEmailYaRegistrado() {
        when(usuarioRepository.existsByEmail("repetido@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(new RegistroRequest("repetido@test.com", "clave123")))
                .isInstanceOf(EmailYaRegistradoException.class);

        verify(usuarioRepository, never()).save(any());
    }
}

