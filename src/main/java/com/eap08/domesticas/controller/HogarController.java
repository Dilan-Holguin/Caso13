package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.HogarRequest;
import com.eap08.domesticas.dto.HogarRequest.CreateHogarRequest;
import com.eap08.domesticas.dto.HogarRequest.InvitarMiembroRequest;
import com.eap08.domesticas.dto.HogarRequest.ResponderInvitacionRequest;
import com.eap08.domesticas.dto.HogarResponse;
import com.eap08.domesticas.dto.HogarResponse.HogarData;
import com.eap08.domesticas.dto.HogarResponse.InvitacionResponse;
import com.eap08.domesticas.dto.HogarResponse.MiembroResponse;

import com.eap08.domesticas.service.HogarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
public class HogarController {

    private final HogarService hogarService;

    // Crear hogar
    @PostMapping
    public ResponseEntity<HogarData> crearHogar(
        @Valid @RequestBody CreateHogarRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        HogarData response = hogarService.crearHogar(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Invitar miembro 
    
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

    //  Responder invitación
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

    // Listar miembros
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