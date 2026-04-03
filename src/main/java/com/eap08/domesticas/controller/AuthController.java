package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.LoginRequest;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).register(null))
                .withSelfRel());
        // Ahora sí apunta al método login real
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).login(null))
                .withRel("login"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).login(null))
                .withSelfRel());
        // Le dice al cliente que desde aquí puede ir a ver los hogares
        // Este enlace lo actualizaremos cuando creemos el HogarController
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).login(null))
                .withRel("hogares"));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
    // En una arquitectura JWT stateless, el logout se maneja del lado del cliente
    // descartando el token. El servidor no necesita hacer nada adicional.
    // Devolvemos 204 No Content porque la operación fue exitosa pero no hay
    // nada que retornar en el cuerpo de la respuesta.
        return ResponseEntity.noContent().build();
    }
}