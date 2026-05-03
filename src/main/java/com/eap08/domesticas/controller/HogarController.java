package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.HogarRequest.CreateHogarRequest;
import com.eap08.domesticas.dto.HogarRequest.InvitarMiembroRequest;
import com.eap08.domesticas.dto.HogarRequest.ResponderInvitacionRequest;
import com.eap08.domesticas.dto.HogarResponse.HogarData;
import com.eap08.domesticas.dto.HogarResponse.InvitacionResponse;
import com.eap08.domesticas.dto.HogarResponse.MiembroResponse;
import com.eap08.domesticas.service.HogarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Hogar", description = "Endpoints para gestión de hogares, miembros e invitaciones")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor

public class HogarController {

    private final HogarService hogarService;

    @Operation(summary = "Crear hogar", description = "Crea un nuevo hogar y asigna al usuario autenticado como Administrador")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Hogar creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })

    @PostMapping
    public ResponseEntity<HogarData> crearHogar(
        @Valid @RequestBody CreateHogarRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        HogarData response = hogarService.crearHogar(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(summary = "Invitar miembro", description = "Genera un token de invitación para un email. Solo el Administrador del hogar puede invitar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Invitación generada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Email inválido o invitación duplicada"),
        @ApiResponse(responseCode = "403", description = "No eres Administrador de este hogar"),
        @ApiResponse(responseCode = "404", description = "Hogar no encontrado")
    })

    @PostMapping("/{hogarId}/invite")
    public ResponseEntity<InvitacionResponse> invitarMiembro(
        @PathVariable Long hogarId,
        @Valid @RequestBody InvitarMiembroRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        InvitacionResponse response = hogarService.invitarMiembro(
            hogarId, request, userDetails.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Responder invitación", description = "Acepta o rechaza una invitación usando el token recibido. El email del usuario autenticado debe coincidir con el email invitado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invitación procesada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Acción inválida o invitación ya procesada"),
        @ApiResponse(responseCode = "403", description = "El email no corresponde a esta invitación"),
        @ApiResponse(responseCode = "404", description = "Token no válido")
    })

    @PostMapping("/invitations/{token}/respond")
    public ResponseEntity<InvitacionResponse> responderInvitacion(
        @PathVariable String token,
        @Valid @RequestBody ResponderInvitacionRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        InvitacionResponse response = hogarService.responderInvitacion(
            token, request, userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar miembros", description = "Retorna todos los miembros activos del hogar. Solo accesible para miembros del hogar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de miembros"),
        @ApiResponse(responseCode = "403", description = "No perteneces a este hogar"),
        @ApiResponse(responseCode = "404", description = "Hogar no encontrado")
    })


    @GetMapping("/{hogarId}/members")
    public ResponseEntity<List<MiembroResponse>> listarMiembros(
        @PathVariable Long hogarId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
            hogarService.listarMiembros(hogarId, userDetails.getUsername())
        );
    }
}
