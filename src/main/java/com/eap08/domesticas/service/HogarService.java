package com.eap08.domesticas.service;

import com.eap08.domesticas.dto.HogarRequest.*;
import com.eap08.domesticas.dto.HogarResponse.*;
import com.eap08.domesticas.model.*;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.InvitacionHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class HogarService {

    private final HogarRepository hogarRepo;
    private final UsuarioHogarRepository usuarioHogarRepo;
    private final InvitacionHogarRepository invitacionRepo;
    private final UsuarioRepository usuarioRepo;

    // TASK 2: Crear un hogar familiar
    @Transactional
    public HogarData crearHogar(CreateHogarRequest request, String emailCreador) {

        Usuario creador = obtenerUsuarioPorEmail(emailCreador);

        Hogar hogar = Hogar.builder()
            .nombre(request.nombre())
            .descripcion(request.descripcion())
            .build();

        hogar = hogarRepo.save(hogar);
        log.info("Hogar '{}' creado con id={} por {}", hogar.getNombre(), hogar.getHogarId(), emailCreador);

        UsuarioHogarId compositeKey = new UsuarioHogarId(creador.getUsuarioId(), hogar.getHogarId());

        UsuarioHogar adminInicial = UsuarioHogar.builder()
            .id(compositeKey)
            .usuario(creador)
            .hogar(hogar)
            .rol(UsuarioHogar.ROL_ADMINISTRADOR)
            .build();

        usuarioHogarRepo.save(adminInicial);

        return toHogarData(hogar);
    }

    // TASK 3: Invitar un miembro al hogar
    @Transactional
    public InvitacionResponse invitarMiembro(Long hogarId,
                                          InvitarMiembroRequest request,
                                          String emailAdmin) {

        validarEsAdministrador(hogarId, emailAdmin);

        Hogar hogar = hogarRepo.findById(hogarId)
            .orElseThrow(() -> new RuntimeException("Hogar no encontrado"));

        usuarioRepo.findByEmail(request.emailInvitado()).ifPresent(u -> {
            if (usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(u.getUsuarioId(), hogarId)) {
                throw new RuntimeException("El usuario ya pertenece a este hogar");
            }
        });

        if (invitacionRepo.existsByEmailInvitadoAndHogarHogarIdAndEstado(
                request.emailInvitado(), hogarId, "Pendiente")) {
            throw new RuntimeException("Ya existe una invitacion pendiente para ese email");
        }

        Usuario admin = obtenerUsuarioPorEmail(emailAdmin);

        InvitacionHogar invitacion = InvitacionHogar.builder()
            .token(UUID.randomUUID().toString())
            .emailInvitado(request.emailInvitado())
            .hogar(hogar)
            .invitadoPor(admin)
            .estado("Pendiente")
            .fechaExpiracion(LocalDateTime.now().plusHours(48))
            .build();

        invitacion = invitacionRepo.save(invitacion);
        log.info("Invitacion generada para {} en hogar {}", request.emailInvitado(), hogarId);

        return toInvitacionResponse(invitacion);
    }

    // TASK 4: Aceptar o rechazar una invitacion
    @Transactional
    public InvitacionResponse responderInvitacion(String token,
                                               ResponderInvitacionRequest request,
                                               String emailRespondedor) {

        InvitacionHogar invitacion = invitacionRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token de invitacion no valido"));

        if (!"Pendiente".equals(invitacion.getEstado())) {
            throw new RuntimeException("Esta invitacion ya fue procesada");
        }

        if (invitacion.estaExpirada()) {
            invitacion.setEstado("Expirada");
            invitacionRepo.save(invitacion);
            throw new RuntimeException("La invitacion ha expirado");
        }

        if (!invitacion.getEmailInvitado().equalsIgnoreCase(emailRespondedor)) {
            throw new RuntimeException("No tienes permiso para responder esta invitacion");
        }

        String accion = request.accion().toUpperCase();

        if ("ACEPTAR".equals(accion)) {
            Usuario nuevoMiembro = obtenerUsuarioPorEmail(emailRespondedor);
            Long hogarId = invitacion.getHogar().getHogarId();

            UsuarioHogarId compositeKey = new UsuarioHogarId(nuevoMiembro.getUsuarioId(), hogarId);

            UsuarioHogar membresia = UsuarioHogar.builder()
                .id(compositeKey)
                .usuario(nuevoMiembro)
                .hogar(invitacion.getHogar())
                .rol(UsuarioHogar.ROL_MIEMBRO)
                .build();

            usuarioHogarRepo.save(membresia);
            invitacion.setEstado("Aceptada");
            log.info("{} acepto unirse al hogar {}", emailRespondedor, hogarId);

        } else if ("RECHAZAR".equals(accion)) {
            invitacion.setEstado("Rechazada");
            log.info("{} rechazo la invitacion al hogar {}", emailRespondedor,
                     invitacion.getHogar().getHogarId());

        } else {
            throw new RuntimeException("Accion no valida. Usa 'ACEPTAR' o 'RECHAZAR'");
        }

        return toInvitacionResponse(invitacionRepo.save(invitacion));
    }

    // Listar miembros del hogar
    public List<MiembroResponse> listarMiembros(Long hogarId, String emailSolicitante) {
        validarEsMiembro(hogarId, emailSolicitante);

        return usuarioHogarRepo.findByIdHogarId(hogarId).stream()
            .map(uh -> new MiembroResponse(
                uh.getUsuario().getUsuarioId(),
                uh.getUsuario().getNombre(),
                uh.getUsuario().getEmail(),
                uh.getRol(),
                uh.getFechaUnion()
            ))
            .toList();
    }

    // Metodos privados de apoyo
    private Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    private void validarEsAdministrador(Long hogarId, String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        UsuarioHogar miembro = usuarioHogarRepo
            .findByIdUsuarioIdAndIdHogarId(usuario.getUsuarioId(), hogarId)
            .orElseThrow(() -> new RuntimeException("No perteneces a este hogar"));

        if (!UsuarioHogar.ROL_ADMINISTRADOR.equals(miembro.getRol())) {
            throw new RuntimeException("Solo el Administrador puede realizar esta accion");
        }
    }

    private void validarEsMiembro(Long hogarId, String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        if (!usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(usuario.getUsuarioId(), hogarId)) {
            throw new RuntimeException("No tienes acceso a este hogar");
        }
    }

    private HogarData toHogarData(Hogar h) {
        return new HogarData(h.getHogarId(), h.getNombre(), h.getDescripcion(), h.getHCreatedAt());
    }

    private InvitacionResponse toInvitacionResponse(InvitacionHogar i) {
        return new InvitacionResponse(
            i.getInvitacionId(),
            i.getEmailInvitado(),
            i.getHogar().getNombre(),
            i.getToken(),
            i.getFechaExpiracion(),
            i.getEstado()
        );
    }
}