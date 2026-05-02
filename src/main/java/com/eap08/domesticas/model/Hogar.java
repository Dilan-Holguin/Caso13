package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hogar")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hogar_id")
    private Long hogarId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @CreationTimestamp
    @Column(name = "h_created_at", updatable = false)
    private LocalDateTime hCreatedAt;
}
