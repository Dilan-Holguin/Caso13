package com.eap08.domesticas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // Lombok genera automáticamente el constructor con los tres campos
public class AuthResponse {
    private String token;
    private String email;
    private String nombre;
}