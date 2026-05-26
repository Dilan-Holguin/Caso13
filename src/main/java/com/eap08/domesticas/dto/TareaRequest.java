package com.eap08.domesticas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class TareaRequest {

    public record CreateTareaRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(min = 3, max = 150)
        String titulo,
        String descripcion,
        @NotBlank(message = "La categoria es obligatoria")
        String categoria,
        @Future(message = "La fecha limite debe ser futura")
        LocalDateTime fechaLimite,
        String prioridad,
        Long asignadoAId
    ) {}

    public record UpdateTareaRequest(
        @Size(min = 3, max = 150)
        String titulo,
        String descripcion,
        String categoria,
        @Future(message = "La fecha limite debe ser futura")
        LocalDateTime fechaLimite,
        String prioridad,
        Long asignadoAId
    ) {}

    public record UpdateStatusRequest(
        @NotBlank(message = "El estado es obligatorio")
        String estado
    ) {}
}
