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

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @PostMapping("/households/{hogarId}/tasks")
    public ResponseEntity<TareaData> crearTarea(
        @PathVariable Long hogarId,
        @Valid @RequestBody CreateTareaRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        TareaData response = tareaService.crearTarea(hogarId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @GetMapping("/tasks/{tareaId}")
    public ResponseEntity<TareaData> obtenerTarea(
        @PathVariable Long tareaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            tareaService.obtenerTarea(tareaId, userDetails.getUsername())
        );
    }

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

    @DeleteMapping("/tasks/{tareaId}")
    public ResponseEntity<Void> eliminarTarea(
        @PathVariable Long tareaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        tareaService.eliminarTarea(tareaId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
