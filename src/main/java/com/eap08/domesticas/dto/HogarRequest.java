package com.eap08.domesticas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HogarRequest {

    public record CreateHogarRequest(
        @NotBlank(message = "El nombre del hogar es obligatorio")
        @Size(min = 3, max = 150)
        String nombre,
        String descripcion
    ) {}

    public record InvitarMiembroRequest(
        @NotBlank @Email(message = "Formato de email no válido")
        String emailInvitado
    ) {}

    public record ResponderInvitacionRequest(
        @NotBlank(message = "Debes indicar ACEPTAR o RECHAZAR")
        String accion
    ) {}
}