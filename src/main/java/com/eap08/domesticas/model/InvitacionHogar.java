package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    private String estado = "Pendiente";

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean estaExpirada() {
        return LocalDateTime.now().isAfter(this.fechaExpiracion);
    }
}
