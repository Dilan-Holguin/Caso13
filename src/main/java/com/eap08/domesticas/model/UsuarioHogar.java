package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla intermedia "usuario_hogar".
 *
 * Esta tabla resuelve la relación N:M entre usuarios y hogares,
 * y además guarda el ROL que tiene cada usuario dentro de ese hogar.
 *
 * La PK es compuesta: (usuario_id, hogar_id) — un usuario solo puede
 * aparecer una vez en el mismo hogar, pero puede estar en varios hogares.
 *
 * Los valores válidos de rol están en el CHECK de la BD:
 *   'Administrador' o 'Miembro' — exactamente con esa capitalización.
 */
@Entity
@Table(name = "usuario_hogar")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioHogar {

    // @EmbeddedId le dice a JPA que el ID de esta entidad
    // vive en la clase UsuarioHogarId (que tiene los dos campos)
    @EmbeddedId
    private UsuarioHogarId id;

    // @MapsId("usuarioId") conecta el campo usuarioId del EmbeddedId
    // con esta relación — así JPA sabe que es la misma columna
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("hogarId")
    @JoinColumn(name = "hogar_id")
    private Hogar hogar;

    // Valores exactos del CHECK constraint en la BD
    @Column(nullable = false, length = 50)
    private String rol; // "Administrador" o "Miembro"

    @CreationTimestamp
    @Column(name = "fecha_union", updatable = false)
    private LocalDateTime fechaUnion;

    // Constantes para no escribir strings sueltos por el código
    public static final String ROL_ADMINISTRADOR = "Administrador";
    public static final String ROL_MIEMBRO       = "Miembro";
}