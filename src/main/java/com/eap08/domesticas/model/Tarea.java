package com.eap08.domesticas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarea")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tarea_id")
    private Long tareaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hogar_id", nullable = false)
    private Hogar hogar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_a")
    private Usuario asignadoA;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "Pendiente";

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(length = 10)
    private String prioridad;

    @Column(name = "completada_at")
    private LocalDateTime completadaAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static final String ESTADO_PENDIENTE   = "Pendiente";
    public static final String ESTADO_EN_PROGRESO = "En_progreso";
    public static final String ESTADO_COMPLETADA  = "Completada";

    public static final String CAT_LIMPIEZA      = "Limpieza";
    public static final String CAT_COCINA        = "Cocina";
    public static final String CAT_COMPRAS       = "Compras";
    public static final String CAT_MANTENIMIENTO = "Mantenimiento";
    public static final String CAT_OTRO          = "Otro";

    public static final String PRIORIDAD_ALTA  = "Alta";
    public static final String PRIORIDAD_MEDIA = "Media";
    public static final String PRIORIDAD_BAJA  = "Baja";
}
