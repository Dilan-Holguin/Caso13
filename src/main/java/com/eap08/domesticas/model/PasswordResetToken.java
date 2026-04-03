package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el usuario al que pertenece este token
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expiracion", nullable = false)
    private LocalDateTime expiracion;

    // Cuando se usa el token lo marcamos como verdadero para que no pueda
    // usarse de nuevo, incluso si técnicamente aún no ha expirado
    @Column(name = "usado", nullable = false)
    private boolean usado = false;
}