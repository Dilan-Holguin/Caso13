package com.eap08.domesticas.security;

import com.eap08.domesticas.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBadCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var response = handler.handleBadCredentials(new BadCredentialsException("bad"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(body.getMessage()).isEqualTo("Correo o contraseña incorrectos");
    }

    @Test
    void shouldHandleResponseStatusException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(body.getMessage()).isEqualTo("Tarea no encontrada");
    }

    @Test
    void shouldHandleRuntimeException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var response = handler.handleRuntimeException(new RuntimeException("Categoria no valida"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(body.getMessage()).isEqualTo("Categoria no valida");
    }

    @Test
    void shouldHandleGenericException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/test");

        var response = handler.handleGenericException(new Exception("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getMessage()).isEqualTo("Ocurrió un error interno en el servidor");
    }
}