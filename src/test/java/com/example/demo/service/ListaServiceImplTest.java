package com.example.demo.service;

import com.example.demo.domain.Lista;
import com.example.demo.domain.Rol;
import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.exception.AccesoDenegadoException;
import com.example.demo.repository.FavoritoRepository;
import com.example.demo.repository.ListaRepository;
import com.example.demo.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @InjectMocks
    private ListaServiceImpl listaService;

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crear_usuarioAutenticado_asociaListaAlUsuarioAutenticado() {
        autenticarComo(42L, "nico@test.com", Rol.USER);
        when(listaRepository.save(any(Lista.class)))
                .thenAnswer(inv -> {
                    Lista arg = inv.getArgument(0);
                    return new Lista(10L, arg.nombre(), arg.usuarioId());
                });

        ListaResponse creada = listaService.crear(new ListaRequest("Mis favoritos"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());

        assertThat(captor.getValue().usuarioId()).isEqualTo(42L);
        assertThat(captor.getValue().nombre()).isEqualTo("Mis favoritos");
        assertThat(creada.usuarioId()).isEqualTo(42L);
    }

    @Test
    void obtener_listaPropia_devuelveLista() {
        autenticarComo(42L, "nico@test.com", Rol.USER);
        when(listaRepository.findById(5L)).thenReturn(Optional.of(new Lista(5L, "Compras", 42L)));

        ListaResponse resultado = listaService.obtener(5L);

        assertThat(resultado.id()).isEqualTo(5L);
        assertThat(resultado.usuarioId()).isEqualTo(42L);
    }

    @Test
    void obtener_listaAjena_lanzaAccesoDenegado() {
        autenticarComo(42L, "nico@test.com", Rol.USER);
        when(listaRepository.findById(5L)).thenReturn(Optional.of(new Lista(5L, "Lista de otro", 99L)));

        assertThatThrownBy(() -> listaService.obtener(5L))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void obtener_listaAjenaComoAdmin_devuelveLista() {
        autenticarComo(1L, "admin@test.com", Rol.ADMIN);
        when(listaRepository.findById(5L)).thenReturn(Optional.of(new Lista(5L, "Lista de otro", 99L)));

        ListaResponse resultado = listaService.obtener(5L);

        assertThat(resultado.id()).isEqualTo(5L);
        assertThat(resultado.usuarioId()).isEqualTo(99L);
    }

    @Test
    void listar_rolUser_devuelveSoloListasPropias() {
        autenticarComo(42L, "nico@test.com", Rol.USER);
        when(listaRepository.findByUsuarioId(42L))
                .thenReturn(List.of(new Lista(1L, "Mía", 42L)));

        List<ListaResponse> resultado = listaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().usuarioId()).isEqualTo(42L);
        verify(listaRepository).findByUsuarioId(42L);
    }

    @Test
    void listar_rolAdmin_devuelveTodasLasListas() {
        autenticarComo(1L, "admin@test.com", Rol.ADMIN);
        when(listaRepository.findAll()).thenReturn(List.of(
                new Lista(1L, "Lista A", 42L),
                new Lista(2L, "Lista B", 99L)
        ));

        List<ListaResponse> resultado = listaService.listar();

        assertThat(resultado).hasSize(2);
        verify(listaRepository).findAll();
    }

    private void autenticarComo(Long id, String email, Rol rol) {
        UsuarioAutenticado principal = new UsuarioAutenticado(id, email, rol);
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

