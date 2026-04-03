package com.eap08.domesticas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    // El token que llegó por email — identifica de quién es la solicitud
    @NotBlank(message = "El token no puede estar vacío")
    private String token;

    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String nuevaPassword;
}