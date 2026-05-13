package com.eap08.domesticas.service;

import com.eap08.domesticas.dto.TareaRequest.UpdateTareaRequest;
import com.eap08.domesticas.dto.TareaRequest.CreateTareaRequest;
import com.eap08.domesticas.dto.TareaResponse.TareaData;
import com.eap08.domesticas.model.Hogar;
import com.eap08.domesticas.model.Tarea;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.model.UsuarioHogar;
import com.eap08.domesticas.model.UsuarioHogarId;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepo;

    @Mock
    private HogarRepository hogarRepo;

    @Mock
    private UsuarioHogarRepository usuarioHogarRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private TareaService tareaService;

    @Test
    void shouldAssignTaskToValidHouseholdMember() {
        // Arrange
        Long tareaId = 10L;
        Long hogarId = 20L;
        Long editorId = 1L;
        Long miembroId = 2L;

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar QA").build();

        Usuario editor = new Usuario();
        editor.setUsuarioId(editorId);
        editor.setEmail("editor@example.com");

        Usuario miembro = new Usuario();
        miembro.setUsuarioId(miembroId);
        miembro.setNombre("Miembro QA");
        miembro.setEmail("member@example.com");

        Tarea tarea = Tarea.builder()
                .tareaId(tareaId)
                .hogar(hogar)
                .titulo("Barrer sala")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();

        UpdateTareaRequest request = new UpdateTareaRequest(
                null,
                null,
                null,
                null,
                miembroId);

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail("editor@example.com")).thenReturn(Optional.of(editor));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(editorId, hogarId)).thenReturn(true);
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(miembroId, hogarId)).thenReturn(true);
        when(usuarioRepo.findById(miembroId)).thenReturn(Optional.of(miembro));
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TareaData response = tareaService.actualizarTarea(tareaId, request, "editor@example.com");

        // Assert
        assertThat(tarea.getAsignadoA()).isNotNull();
        assertThat(tarea.getAsignadoA().getUsuarioId()).isEqualTo(miembroId);
        assertThat(response.asignadoA()).isNotNull();
        assertThat(response.asignadoA().usuarioId()).isEqualTo(miembroId);
        assertThat(response.asignadoA().nombre()).isEqualTo("Miembro QA");
        assertThat(response.asignadoA().email()).isEqualTo("member@example.com");

        verify(tareaRepo).save(tarea);
    }

    @Test
    void shouldCreateTaskWithoutAssignee() {
        // Arrange
        Long hogarId = 20L;
        Long adminId = 1L;
        String adminEmail = "admin@example.com";

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar QA").build();

        Usuario admin = new Usuario();
        admin.setUsuarioId(adminId);
        admin.setNombre("Admin QA");
        admin.setEmail(adminEmail);

        UsuarioHogar usuarioHogar = UsuarioHogar.builder()
                .id(new UsuarioHogarId(adminId, hogarId))
                .usuario(admin)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build();

        CreateTareaRequest request = new CreateTareaRequest(
                "Barrer sala",
                "Tarea sin responsable",
                Tarea.CAT_LIMPIEZA,
                null,
                null // No assignee specified
        );

        Tarea createdTarea = Tarea.builder()
                .tareaId(1L)
                .hogar(hogar)
                .titulo("Barrer sala")
                .descripcion("Tarea sin responsable")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .asignadoA(null) // Explicitly null
                .build();

        when(usuarioRepo.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(adminId, hogarId)).thenReturn(true);
        when(hogarRepo.getReferenceById(hogarId)).thenReturn(hogar);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(createdTarea);

        // Act
        TareaData response = tareaService.crearTarea(hogarId, request, adminEmail);

        // Assert
        assertThat(response.asignadoA()).isNull();
        assertThat(response.titulo()).isEqualTo("Barrer sala");
        assertThat(response.descripcion()).isEqualTo("Tarea sin responsable");
        assertThat(response.categoria()).isEqualTo(Tarea.CAT_LIMPIEZA);
        assertThat(response.estado()).isEqualTo(Tarea.ESTADO_PENDIENTE);

        verify(tareaRepo).save(any(Tarea.class));
    }

    @Test
    void shouldRejectAssignmentWhenUserNotInHousehold() {
        // Arrange
        Long tareaId = 11L;
        Long hogarId = 30L;
        Long editorId = 2L;
        Long externoId = 99L;

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar QA").build();

        Usuario editor = new Usuario();
        editor.setUsuarioId(editorId);
        editor.setEmail("editor2@example.com");

        Usuario externo = new Usuario();
        externo.setUsuarioId(externoId);
        externo.setNombre("Usuario Externo");
        externo.setEmail("external@example.com");

        Tarea tarea = Tarea.builder()
                .tareaId(tareaId)
                .hogar(hogar)
                .titulo("Lavar platos")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();

        UpdateTareaRequest request = new UpdateTareaRequest(
                null,
                null,
                null,
                null,
                externoId);

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail("editor2@example.com")).thenReturn(Optional.of(editor));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(editorId, hogarId)).thenReturn(true);
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(externoId, hogarId)).thenReturn(false);

        // Act & Assert: expect business exception about external user
        assertThatThrownBy(() -> tareaService.actualizarTarea(tareaId, request, "editor2@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El usuario asignado no pertenece a este hogar");

        verify(tareaRepo, never()).save(any(Tarea.class));
    }
}