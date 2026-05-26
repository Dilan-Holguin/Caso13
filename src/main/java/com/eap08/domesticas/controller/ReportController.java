package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.ReporteResponse.ReporteCumplimiento;
import com.eap08.domesticas.dto.ReporteResponse.ReporteDistribucion;
import com.eap08.domesticas.service.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reportes", description = "Reportes de distribucion y cumplimiento por hogar")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final TareaService tareaService;

    @Operation(summary = "Reporte de distribucion de responsabilidades",
               description = "Muestra cuantas tareas tiene cada miembro del hogar, desglosadas por estado.")
    @GetMapping("/households/{hogarId}/reports/distribution")
    public ResponseEntity<ReporteDistribucion> reporteDistribucion(
            @PathVariable Long hogarId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            tareaService.generarReporteDistribucion(hogarId, userDetails.getUsername())
        );
    }

    @Operation(summary = "Reporte de historial de cumplimiento por usuario",
               description = "Muestra tasa de cumplimiento, tareas a tiempo y tarde por cada usuario del hogar.")
    @GetMapping("/households/{hogarId}/reports/cumplimiento")
    public ResponseEntity<ReporteCumplimiento> reporteCumplimiento(
            @PathVariable Long hogarId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            tareaService.generarReporteCumplimiento(hogarId, userDetails.getUsername())
        );
    }
}
