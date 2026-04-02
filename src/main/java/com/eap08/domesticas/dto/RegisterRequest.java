package com.eap08.domesticas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    // @Email valida automáticamente que el texto tenga formato de correo (x@y.z)
    @Email(message = "El formato del correo no es válido")
    @NotBlank(message = "El correo no puede estar vacío")
    private String email;

    // @Size garantiza una contraseña mínimamente segura
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}