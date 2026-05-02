package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_hogar")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioHogar {

    @EmbeddedId
    private UsuarioHogarId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hogarId")
    @JoinColumn(name = "hogar_id")
    private Hogar hogar;

    @Column(nullable = false, length = 50)
    private String rol;

    @CreationTimestamp
    @Column(name = "fecha_union", updatable = false)
    private LocalDateTime fechaUnion;

    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_MIEMBRO       = "Miembro";
}
