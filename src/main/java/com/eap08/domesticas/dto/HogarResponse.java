package com.eap08.domesticas.dto;

import java.time.LocalDateTime;

public class HogarResponse {

    public record HogarData(
        Long hogarId,
        String nombre,
        String descripcion,
        LocalDateTime creadoEn
    ) {}

    public record InvitacionResponse(
        Long invitacionId,
        String emailInvitado,
        String nombreHogar,
        String token,
        LocalDateTime fechaExpiracion,
        String estado
    ) {}

    public record MiembroResponse(
        Long usuarioId,
        String nombre,
        String email,
        String rol,
        LocalDateTime fechaUnion
    ) {}
}