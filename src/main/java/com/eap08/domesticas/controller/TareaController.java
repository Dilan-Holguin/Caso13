package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.TareaRequest.CreateTareaRequest;
import com.eap08.domesticas.dto.TareaRequest.UpdateStatusRequest;
import com.eap08.domesticas.dto.TareaRequest.UpdateTareaRequest;
import com.eap08.domesticas.dto.TareaResponse.TareaData;
import com.eap08.domesticas.dto.TareaResponse.TareaListData;
import com.eap08.domesticas.service.TareaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;


@Tag(name = "Tareas", description = "Gestión de tareas domésticas por hogar")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @Operation(summary = "Crear tarea", description = "Crea una nueva tarea en el hogar. Solo miembros del hogar pueden crear tareas")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "No perteneces a este hogar")
    })

    @PostMapping("/households/{hogarId}/tasks")
    public ResponseEntity<TareaData> crearTarea(
        @PathVariable Long hogarId,
        @Valid @RequestBody CreateTareaRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TareaData response = tareaService.crearTarea(hogarId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar tareas", description = "Retorna las tareas del hogar. Filtra opcionalmente por estado, categoría o usuario asignado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de tareas"),
        @ApiResponse(responseCode = "403", description = "No perteneces a este hogar")
    })

    @GetMapping("/households/{hogarId}/tasks")
    public ResponseEntity<List<TareaListData>> listarTareas(
        @PathVariable Long hogarId,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) Long asignadoA,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tareaService.listarTareas(hogarId, estado, categoria, asignadoA, userDetails.getUsername())
        );
    }

    @Operation(summary = "Obtener tarea", description = "Retorna el detalle de una tarea. Solo accesible para miembros del hogar al que pertenece")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle de la tarea"),
        @ApiResponse(responseCode = "403", description = "No tienes acceso a esta tarea"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })

    @GetMapping("/tasks/{tareaId}")
    public ResponseEntity<TareaData> obtenerTarea(
        @PathVariable Long tareaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tareaService.obtenerTarea(tareaId, userDetails.getUsername())
        );
    }

    @Operation(summary = "Actualizar tarea", description = "Actualiza título, descripción, categoría, fecha límite o asignado de una tarea")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tarea actualizada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para editar esta tarea"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PutMapping("/tasks/{tareaId}")
    public ResponseEntity<TareaData> actualizarTarea(
        @PathVariable Long tareaId,
        @Valid @RequestBody UpdateTareaRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tareaService.actualizarTarea(tareaId, request, userDetails.getUsername())
        );
    }

    @Operation(summary = "Actualizar estado de tarea", description = "Actualiza el estado de una tarea: Pendiente, En_progreso o Completada")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado de la tarea actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para editar esta tarea"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PatchMapping("/tasks/{tareaId}/status")
    public ResponseEntity<TareaData> actualizarEstado(
        @PathVariable Long tareaId,
        @Valid @RequestBody UpdateStatusRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tareaService.actualizarEstado(tareaId, request, userDetails.getUsername())
        );
    }
    @Operation(summary = "Eliminar tarea", description = "Elimina una tarea del hogar. Solo el Administrador puede eliminar tareas")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tarea eliminada"),
        @ApiResponse(responseCode = "403", description = "Solo el Administrador puede eliminar tareas"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @DeleteMapping("/tasks/{tareaId}")
    public ResponseEntity<Void> eliminarTarea(
        @PathVariable Long tareaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        tareaService.eliminarTarea(tareaId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
