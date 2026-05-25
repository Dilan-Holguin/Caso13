package com.eap08.domesticas.dto;

import java.util.List;

public class ReporteResponse {

    public record ReporteDistribucion(
        Long hogarId,
        List<MiembroDistribucion> miembros
    ) {}

    public record MiembroDistribucion(
        Long usuarioId,
        String nombre,
        Long total,
        Long pendientes,
        Long enProgreso,
        Long completadas
    ) {}

    public record ReporteCumplimiento(
        Long hogarId,
        List<UsuarioCumplimiento> usuarios
    ) {}

    public record UsuarioCumplimiento(
        Long usuarioId,
        String nombre,
        Long totalAsignadas,
        Long completadas,
        Long aTiempo,
        Long tarde,
        double tasaCumplimiento
    ) {}
}
