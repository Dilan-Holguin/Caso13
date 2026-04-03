package com.eap08.domesticas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @Email(message = "El formato del correo no es válido")
    @NotBlank(message = "El correo no puede estar vacío")
    private String email;
}