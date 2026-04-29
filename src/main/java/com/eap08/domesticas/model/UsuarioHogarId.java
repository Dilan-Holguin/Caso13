package com.eap08.domesticas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioHogarId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "hogar_id")
    private Long hogarId;
}