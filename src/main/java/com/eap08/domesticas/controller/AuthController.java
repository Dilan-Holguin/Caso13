package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.AuthResponse;
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

        // Agregamos los enlaces HATEOAS a la respuesta
        // self — apunta al endpoint que acaba de ser llamado
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).register(null))
                .withSelfRel());

        // login — le dice al cliente que el siguiente paso lógico es iniciar sesión
        // Por ahora apunta al mismo controlador porque login aún no existe,
        // lo actualizaremos cuando implementemos ese endpoint
        response.add(WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(AuthController.class).register(null))
                .withRel("login"));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}