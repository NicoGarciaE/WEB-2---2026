package com.example.demo.controller;

import com.example.demo.dto.lista.ListaRequest;
import com.example.demo.dto.lista.ListaResponse;
import com.example.demo.support.BaseIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListaControllerIT extends BaseIntegrationSupport {

    @Test
    void obtener_listaPropia_devuelve200() {
        String tokenDueno = registrarYLoguearUsuario("dueno-" + UUID.randomUUID() + "@test.com", "clave123");
        ListaResponse creada = crearLista(tokenDueno, "Mi lista propia");

        ResponseEntity<ListaResponse> respuesta = restClient().get()
                .uri("/api/listas/{id}", creada.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDueno)
                .retrieve()
                .toEntity(ListaResponse.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().id()).isEqualTo(creada.id());
        assertThat(respuesta.getBody().nombre()).isEqualTo("Mi lista propia");
    }

    @Test
    void obtener_listaAjena_devuelve403() {
        String tokenDueno = registrarYLoguearUsuario("dueno-a-" + UUID.randomUUID() + "@test.com", "clave123");
        String tokenIntruso = registrarYLoguearUsuario("intruso-" + UUID.randomUUID() + "@test.com", "clave123");
        ListaResponse listaDeOtro = crearLista(tokenDueno, "Lista privada");

        ResponseEntity<String> respuesta = restClient().get()
                .uri("/api/listas/{id}", listaDeOtro.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenIntruso)
                .retrieve()
                .toEntity(String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listar_sinToken_devuelve401() {
        ResponseEntity<String> respuesta = restClient().get()
                .uri("/api/listas")
                .retrieve()
                .toEntity(String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void obtener_listaAjenaComoAdmin_devuelve200() {
        String tokenDueno = registrarYLoguearUsuario("user-comun-" + UUID.randomUUID() + "@test.com", "clave123");
        String tokenAdmin = crearYLoguearAdmin("admin-" + UUID.randomUUID() + "@test.com", "adminClave123");
        ListaResponse listaAjena = crearLista(tokenDueno, "Lista visible para admin");

        ResponseEntity<ListaResponse> respuesta = restClient().get()
                .uri("/api/listas/{id}", listaAjena.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                .retrieve()
                .toEntity(ListaResponse.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().id()).isEqualTo(listaAjena.id());
    }

    private ListaResponse crearLista(String token, String nombre) {
        return restClient().post()
                .uri("/api/listas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListaRequest(nombre))
                .retrieve()
                .body(ListaResponse.class);
    }
}

