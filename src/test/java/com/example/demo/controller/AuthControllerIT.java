package com.example.demo.controller;

import com.example.demo.dto.auth.AuthResponse;
import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.RegistroRequest;
import com.example.demo.support.BaseIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends BaseIntegrationSupport {

    @Test
    void login_credencialesValidas_devuelve200ConToken() {
        String email = "login-ok-" + UUID.randomUUID() + "@test.com";
        restClient().post()
                .uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegistroRequest(email, "claveValida123"))
                .retrieve()
                .toBodilessEntity();

        ResponseEntity<AuthResponse> respuesta = restClient().post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "claveValida123"))
                .retrieve()
                .toEntity(AuthResponse.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().token()).isNotBlank();
    }

    @Test
    void login_credencialesInvalidas_devuelve401() {
        String email = "login-fail-" + UUID.randomUUID() + "@test.com";
        restClient().post()
                .uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegistroRequest(email, "claveCorrecta"))
                .retrieve()
                .toBodilessEntity();

        ResponseEntity<String> respuesta = restClient().post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(email, "claveIncorrecta"))
                .retrieve()
                .toEntity(String.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registrar_emailDuplicado_devuelve409() {
        String email = "duplicado-" + UUID.randomUUID() + "@test.com";
        restClient().post()
                .uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegistroRequest(email, "clave123"))
                .retrieve()
                .toBodilessEntity();

        ResponseEntity<String> segundoIntento = restClient().post()
                .uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RegistroRequest(email, "otraClave"))
                .retrieve()
                .toEntity(String.class);

        assertThat(segundoIntento.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}

