package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para gestionar invitaciones a un hogar.
 *
 *  El flujo es:
 *
 *   1. Administrador llama a POST /api/households/{id}/invite
 *   2. Sistema genera un token UUID y lo guarda aquí con expiración 48h
 *   3. El invitado recibe el token (por ahora en la respuesta, luego por email)
 *   4. El invitado llama a POST /api/households/invitations/{token}/respond
 *   5. Si acepta, se crea un registro en usuario_hogar con rol 'Miembro'
 */
@Entity
@Table(name = "invitacion_hogar")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionHogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitacion_id")
    private Long invitacionId;

    // El token viaja en el link de invitación — debe ser impredecible (UUID)
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "email_invitado", nullable = false, length = 150)
    private String emailInvitado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hogar_id", nullable = false)
    private Hogar hogar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitado_por", nullable = false)
    private Usuario invitadoPor;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "Pendiente"; // Pendiente | Aceptada | Rechazada | Expirada

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Preguntarle al objeto mismo si ya venció — más limpio que comparar en el service
    public boolean estaExpirada() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }
}