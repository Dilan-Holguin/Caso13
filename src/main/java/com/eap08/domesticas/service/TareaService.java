package com.eap08.domesticas.service;

import com.eap08.domesticas.dto.TareaRequest.*;
import com.eap08.domesticas.dto.TareaResponse.*;
import com.eap08.domesticas.model.*;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareaService {

    private final TareaRepository tareaRepo;
    private final HogarRepository hogarRepo;
    private final UsuarioHogarRepository usuarioHogarRepo;
    private final UsuarioRepository usuarioRepo;

    private static final Set<String> CATEGORIAS_VALIDAS = Set.of(
        Tarea.CAT_LIMPIEZA, Tarea.CAT_COCINA, Tarea.CAT_COMPRAS,
        Tarea.CAT_MANTENIMIENTO, Tarea.CAT_OTRO
    );

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
        Tarea.ESTADO_PENDIENTE, Tarea.ESTADO_EN_PROGRESO, Tarea.ESTADO_COMPLETADA
    );

    @Transactional
    public TareaData crearTarea(Long hogarId, CreateTareaRequest request, String emailCreador) {
        validarEsMiembro(hogarId, emailCreador);
        validarCategoria(request.categoria());

        Hogar hogar = obtenerHogarRef(hogarId);

        Tarea.TareaBuilder builder = Tarea.builder()
            .hogar(hogar)
            .titulo(request.titulo())
            .descripcion(request.descripcion())
            .categoria(request.categoria())
            .estado(Tarea.ESTADO_PENDIENTE)
            .fechaLimite(request.fechaLimite());

        if (request.asignadoAId() != null) {
            validarPerteneceAHogar(hogarId, request.asignadoAId());
            builder.asignadoA(usuarioRepo.findById(request.asignadoAId()).orElse(null));
        }

        Tarea tarea = tareaRepo.save(builder.build());
        log.info("Tarea '{}' creada en hogar {} por {}", tarea.getTitulo(), hogarId, emailCreador);

        return toTareaData(tarea);
    }

    @Transactional(readOnly = true)
    public List<TareaListData> listarTareas(Long hogarId, String estado,
                                            String categoria, Long asignadoA,
                                            String emailSolicitante) {
        validarEsMiembro(hogarId, emailSolicitante);

        List<Tarea> tareas = tareaRepo.findWithFilters(hogarId, estado, categoria, asignadoA);

        return tareas.stream()
            .map(this::toTareaListData)
            .toList();
    }

    @Transactional(readOnly = true)
    public TareaData obtenerTarea(Long tareaId, String emailSolicitante) {
        Tarea tarea = obtenerTareaPorId(tareaId);
        validarEsMiembro(tarea.getHogar().getHogarId(), emailSolicitante);
        return toTareaData(tarea);
    }

    @Transactional
    public TareaData actualizarTarea(Long tareaId, UpdateTareaRequest request, String emailEditor) {
        Tarea tarea = obtenerTareaPorId(tareaId);
        validarEsMiembro(tarea.getHogar().getHogarId(), emailEditor);

        if (request.titulo() != null) tarea.setTitulo(request.titulo());
        if (request.descripcion() != null) tarea.setDescripcion(request.descripcion());
        if (request.fechaLimite() != null) tarea.setFechaLimite(request.fechaLimite());

        if (request.categoria() != null) {
            validarCategoria(request.categoria());
            tarea.setCategoria(request.categoria());
        }

        if (request.asignadoAId() != null) {
            validarPerteneceAHogar(tarea.getHogar().getHogarId(), request.asignadoAId());
            tarea.setAsignadoA(usuarioRepo.findById(request.asignadoAId()).orElse(null));
        }

        Tarea actualizada = tareaRepo.save(tarea);
        log.info("Tarea {} actualizada por {}", tareaId, emailEditor);
        return toTareaData(actualizada);
    }

    @Transactional
    public TareaData actualizarEstado(Long tareaId, UpdateStatusRequest request, String emailEditor) {
        Tarea tarea = obtenerTareaPorId(tareaId);
        validarEsMiembro(tarea.getHogar().getHogarId(), emailEditor);

        if (!ESTADOS_VALIDOS.contains(request.estado())) {
            throw new RuntimeException("Estado no valido. Usa: Pendiente, En_progreso, Completada");
        }

        tarea.setEstado(request.estado());
        Tarea actualizada = tareaRepo.save(tarea);
        log.info("Estado de tarea {} cambiado a {} por {}", tareaId, request.estado(), emailEditor);
        return toTareaData(actualizada);
    }

    @Transactional
    public void eliminarTarea(Long tareaId, String emailSolicitante) {
        Tarea tarea = obtenerTareaPorId(tareaId);
        validarEsAdministrador(tarea.getHogar().getHogarId(), emailSolicitante);
        tareaRepo.delete(tarea);
        log.info("Tarea {} eliminada por {}", tareaId, emailSolicitante);
    }

    // --- Metodos privados de apoyo ---

    private void validarCategoria(String categoria) {
        if (!CATEGORIAS_VALIDAS.contains(categoria)) {
            throw new RuntimeException("Categoria no valida: " + categoria);
        }
    }

    private void validarEsMiembro(Long hogarId, String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(usuario.getUsuarioId(), hogarId)) {
            throw new RuntimeException("No perteneces a este hogar");
        }
    }

    private void validarEsAdministrador(Long hogarId, String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        UsuarioHogar miembro = usuarioHogarRepo
            .findByIdUsuarioIdAndIdHogarId(usuario.getUsuarioId(), hogarId)
            .orElseThrow(() -> new RuntimeException("No perteneces a este hogar"));
        if (!UsuarioHogar.ROL_ADMINISTRADOR.equals(miembro.getRol())) {
            throw new RuntimeException("Solo el Administrador puede eliminar tareas");
        }
    }

    private void validarPerteneceAHogar(Long hogarId, Long usuarioId) {
        if (!usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(usuarioId, hogarId)) {
            throw new RuntimeException("El usuario asignado no pertenece a este hogar");
        }
    }

    private Tarea obtenerTareaPorId(Long tareaId) {
        return tareaRepo.findById(tareaId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
    }

    private Hogar obtenerHogarRef(Long hogarId) {
        return hogarRepo.getReferenceById(hogarId);
    }

    // --- Mapeo de entidades a DTOs ---

    private TareaData toTareaData(Tarea t) {
        AsignadoInfo asignado = null;
        if (t.getAsignadoA() != null) {
            asignado = new AsignadoInfo(
                t.getAsignadoA().getUsuarioId(),
                t.getAsignadoA().getNombre(),
                t.getAsignadoA().getEmail()
            );
        }
        return new TareaData(
            t.getTareaId(),
            t.getHogar().getHogarId(),
            t.getTitulo(),
            t.getDescripcion(),
            t.getCategoria(),
            t.getEstado(),
            t.getFechaLimite(),
            asignado,
            t.getCreatedAt(),
            t.getUpdatedAt()
        );
    }

    private TareaListData toTareaListData(Tarea t) {
        String nombreAsignado = t.getAsignadoA() != null ? t.getAsignadoA().getNombre() : null;
        return new TareaListData(
            t.getTareaId(),
            t.getTitulo(),
            t.getCategoria(),
            t.getEstado(),
            t.getFechaLimite(),
            nombreAsignado
        );
    }
}
