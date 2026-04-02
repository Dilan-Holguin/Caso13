package com.eap08.domesticas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;


@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok genera automáticamente el constructor con los tres campos

public class AuthResponse extends RepresentationModel<AuthResponse> {
    private String token;
    private String email;
    private String nombre;
}