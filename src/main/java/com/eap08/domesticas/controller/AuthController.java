package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.ForgotPasswordRequest;
import com.eap08.domesticas.dto.LoginRequest;
import com.eap08.domesticas.dto.MessageResponse;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.dto.ResetPasswordRequest;
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
    public ResponseEntity<MessageResponse> logout() {
        // Aunque en JWT stateless el servidor no invalida ningún estado,
        // devolvemos un mensaje explícito para que el cliente sepa que debe
        // descartar el token localmente. Es una confirmación de contrato.
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada correctamente"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(authService.resetPassword(request));
    }


}