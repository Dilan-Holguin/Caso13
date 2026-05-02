package com.eap08.domesticas.dto;

import java.time.LocalDateTime;

public class TareaResponse {

    public record TareaData(
        Long tareaId,
        Long hogarId,
        String titulo,
        String descripcion,
        String categoria,
        String estado,
        LocalDateTime fechaLimite,
        AsignadoInfo asignadoA,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record AsignadoInfo(
        Long usuarioId,
        String nombre,
        String email
    ) {}

    public record TareaListData(
        Long tareaId,
        String titulo,
        String categoria,
        String estado,
        LocalDateTime fechaLimite,
        String asignadoANombre
    ) {}
}
