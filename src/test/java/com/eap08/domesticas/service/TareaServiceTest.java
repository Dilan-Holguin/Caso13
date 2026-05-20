package com.eap08.domesticas.service;

import com.eap08.domesticas.dto.TareaRequest;
import com.eap08.domesticas.dto.TareaResponse;
import com.eap08.domesticas.model.Hogar;
import com.eap08.domesticas.model.Tarea;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    //funciones utiles para las pruebas
    private Usuario crearUsuario(String email, Long id) {
        Usuario usuario = new Usuario();
        usuario.setUsuarioId(id);
        usuario.setEmail(email);
        return usuario;
    }

    private Hogar crearHogar(Long id) {
        return Hogar.builder()
                .hogarId(id)
                .nombre("Hogar Test")
                .build();
    }

    private Tarea crearTarea(Long tareaId, Hogar hogar) {
        return Tarea.builder()
                .tareaId(tareaId)
                .hogar(hogar)
                .titulo("Lavar la loza")
                .descripcion("Descripción")
                .categoria("Cocina")
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();
    }


    @Test
    void shouldCreateTaskSuccessfully() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "ana@example.com";

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Lavar la loza", "Después del almuerzo", "Cocina", null, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(10L);
        usuario.setEmail(emailCreador);

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar Test").build();

        Tarea tareaGuardada = Tarea.builder()
                .tareaId(100L)
                .hogar(hogar)
                .titulo("Lavar la loza")
                .descripcion("Después del almuerzo")
                .categoria("Cocina")
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(hogarRepo.getReferenceById(hogarId)).thenReturn(hogar);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(tareaGuardada);

        // Act
        TareaResponse.TareaData result = tareaService.crearTarea(hogarId, request, emailCreador);

        // Assert
        assertThat(result.titulo()).isEqualTo("Lavar la loza");
        assertThat(result.categoria()).isEqualTo("Cocina");
        assertThat(result.estado()).isEqualTo(Tarea.ESTADO_PENDIENTE);
        assertThat(result.hogarId()).isEqualTo(hogarId);

        ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepo).save(tareaCaptor.capture());
        Tarea saved = tareaCaptor.getValue();
        assertThat(saved.getTitulo()).isEqualTo("Lavar la loza");
        assertThat(saved.getCategoria()).isEqualTo("Cocina");
        assertThat(saved.getEstado()).isEqualTo(Tarea.ESTADO_PENDIENTE);
        assertThat(saved.getDescripcion()).isEqualTo("Después del almuerzo");
    }

    @Test
    void shouldThrowWhenCategoryIsInvalid() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "ana@example.com";

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Hacer ejercicio", "", "Deportes", null, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(10L);
        usuario.setEmail(emailCreador);

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> tareaService.crearTarea(hogarId, request, emailCreador))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Categoria no valida");

        verify(tareaRepo, never()).save(any(Tarea.class));
    }

    @Test
    void shouldThrowWhenUserIsNotMemberOfHousehold() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "externo@example.com";

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Lavar la loza", "", "Cocina", null, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(20L);
        usuario.setEmail(emailCreador);

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(20L, hogarId)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> tareaService.crearTarea(hogarId, request, emailCreador))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No perteneces a este hogar");

        verify(tareaRepo, never()).save(any(Tarea.class));
    }

    @Test
    void shouldAcceptTaskCreationWithFutureFechaLimite() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "ana@example.com";
        LocalDateTime futureDate = LocalDateTime.parse("2026-12-31T23:59:00");

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Pagar servicios", "Pago de servicios básicos", "Otro", futureDate, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(10L);
        usuario.setEmail(emailCreador);

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar Test").build();

        Tarea tareaGuardada = Tarea.builder()
                .tareaId(100L)
                .hogar(hogar)
                .titulo("Pagar servicios")
                .descripcion("Pago de servicios básicos")
                .categoria("Otro")
                .estado(Tarea.ESTADO_PENDIENTE)
                .fechaLimite(futureDate)
                .build();

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(hogarRepo.getReferenceById(hogarId)).thenReturn(hogar);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(tareaGuardada);

        // Act
        TareaResponse.TareaData result = tareaService.crearTarea(hogarId, request, emailCreador);

        // Assert
        assertThat(result.titulo()).isEqualTo("Pagar servicios");
        assertThat(result.fechaLimite()).isEqualTo(futureDate);

        ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepo).save(tareaCaptor.capture());
        Tarea saved = tareaCaptor.getValue();
        assertThat(saved.getFechaLimite()).isEqualTo(futureDate);
    }

    @Test
    void shouldCorrectlyAssignFechaLimiteToTaskEntity() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "ana@example.com";
        LocalDateTime futureDate = LocalDateTime.parse("2026-12-31T23:59:00");

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Pagar servicios", "Pago de servicios básicos", "Otro", futureDate, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(10L);
        usuario.setEmail(emailCreador);

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar Test").build();

        Tarea tareaGuardada = Tarea.builder()
                .tareaId(101L)
                .hogar(hogar)
                .titulo("Pagar servicios")
                .descripcion("Pago de servicios básicos")
                .categoria("Otro")
                .estado(Tarea.ESTADO_PENDIENTE)
                .fechaLimite(futureDate)
                .build();

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(hogarRepo.getReferenceById(hogarId)).thenReturn(hogar);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(tareaGuardada);

        // Act
        TareaResponse.TareaData result = tareaService.crearTarea(hogarId, request, emailCreador);

        // Assert: Verify that fechaLimite is assigned correctly in the entity
        assertThat(result.fechaLimite()).isNotNull();
        assertThat(result.fechaLimite()).isEqualTo(LocalDateTime.parse("2026-12-31T23:59:00"));

        ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepo).save(tareaCaptor.capture());
        Tarea saved = tareaCaptor.getValue();
        assertThat(saved.getFechaLimite()).isNotNull();
        assertThat(saved.getFechaLimite()).isEqualTo(futureDate);
    }

    @Test
    void shouldUpdateFechaLimiteAndRefreshUpdatedAt() {
        // Arrange
        Long tareaId = 50L;
        Long hogarId = 1L;
        String emailEditor = "ana@example.com";

        LocalDateTime originalFecha = LocalDateTime.parse("2026-01-02T12:00:00");
        LocalDateTime originalUpdatedAt = LocalDateTime.parse("2026-01-01T12:00:00");
        LocalDateTime newFecha = LocalDateTime.parse("2027-01-15T12:00:00");
        LocalDateTime laterUpdatedAt = originalUpdatedAt.plusMinutes(10);

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar Test").build();

        Tarea existing = Tarea.builder()
                .tareaId(tareaId)
                .hogar(hogar)
                .titulo("Tarea actualizable")
                .descripcion("")
                .categoria("Otro")
                .estado(Tarea.ESTADO_PENDIENTE)
                .fechaLimite(originalFecha)
                .updatedAt(originalUpdatedAt)
                .build();

        Tarea saved = Tarea.builder()
                .tareaId(tareaId)
                .hogar(hogar)
                .titulo("Tarea actualizable")
                .descripcion("")
                .categoria("Otro")
                .estado(Tarea.ESTADO_PENDIENTE)
                .fechaLimite(newFecha)
                .updatedAt(laterUpdatedAt)
                .build();

        TareaRequest.UpdateTareaRequest request = new TareaRequest.UpdateTareaRequest(null, null, null, newFecha, null);

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(existing));
        Usuario editor = new Usuario();
        editor.setUsuarioId(10L);
        editor.setEmail(emailEditor);
        when(usuarioRepo.findByEmail(emailEditor)).thenReturn(Optional.of(editor));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(saved);

        // Act
        TareaResponse.TareaData result = tareaService.actualizarTarea(tareaId, request, emailEditor);

        // Assert
        assertThat(result.fechaLimite()).isEqualTo(newFecha);
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.updatedAt()).isAfter(originalUpdatedAt);

        ArgumentCaptor<Tarea> captor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepo).save(captor.capture());
        Tarea captured = captor.getValue();
        assertThat(captured.getFechaLimite()).isEqualTo(newFecha);
    }

    @Test
    void shouldPersistNullFechaLimiteWhenNotProvided() {
        // Arrange
        Long hogarId = 1L;
        String emailCreador = "ana@example.com";

        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Recordar comprar leche", "Comprar leche en la tienda", "Compras", null, null);

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(10L);
        usuario.setEmail(emailCreador);

        Hogar hogar = Hogar.builder().hogarId(hogarId).nombre("Hogar Test").build();

        Tarea tareaGuardada = Tarea.builder()
                .tareaId(200L)
                .hogar(hogar)
                .titulo("Recordar comprar leche")
                .descripcion("Comprar leche en la tienda")
                .categoria("Compras")
                .estado(Tarea.ESTADO_PENDIENTE)
                .fechaLimite(null)
                .build();

        when(usuarioRepo.findByEmail(emailCreador)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(hogarRepo.getReferenceById(hogarId)).thenReturn(hogar);
        when(tareaRepo.save(any(Tarea.class))).thenReturn(tareaGuardada);

        // Act
        TareaResponse.TareaData result = tareaService.crearTarea(hogarId, request, emailCreador);

        // Assert
        assertThat(result.titulo()).isEqualTo("Recordar comprar leche");
        assertThat(result.fechaLimite()).isNull();

        ArgumentCaptor<Tarea> tareaCaptor = ArgumentCaptor.forClass(Tarea.class);
        verify(tareaRepo).save(tareaCaptor.capture());
        Tarea saved = tareaCaptor.getValue();
        assertThat(saved.getFechaLimite()).isNull();
    }

    //CASO FELIZ

    //CP 18 edición exitosa
    @Test
    void shouldUpdateTaskSuccessfully() {

        // Arrange
        Long tareaId = 1L;
        Long hogarId = 1L;
        String email = "ana@example.com";

        Usuario usuario = crearUsuario(email, 10L);
        Hogar hogar = crearHogar(hogarId);
        Tarea tarea = crearTarea(tareaId, hogar);

        TareaRequest.UpdateTareaRequest request =
                new TareaRequest.UpdateTareaRequest(
                        "Lavar la loza y secarla",
                        null,
                        "Cocina",
                        null,
                        null
                );

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TareaResponse.TareaData result = tareaService.actualizarTarea(tareaId, request, email);

        // Assert
        assertThat(result.titulo()).isEqualTo("Lavar la loza y secarla");
        assertThat(result.categoria()).isEqualTo("Cocina");
    }

    //CASOS EXCEPCIONALES
    //CP 20 tarea inexistente
    @Test
    void shouldThrowWhenTaskDoesNotExist() {
        // Arrange
        Long tareaId = 99999L;

        TareaRequest.UpdateTareaRequest request =
                new TareaRequest.UpdateTareaRequest("Nuevo titulo", null, "Cocina", null, null);

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> tareaService.actualizarTarea(tareaId, request, "ana@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tarea no encontrada");
    }

    //CP 21 usuario sin permisos
    @Test
    void shouldThrowWhenUserDoesNotBelongToHousehold() {
        // Arrange
        Long tareaId = 1L;
        Long hogarId = 1L;

        String email = "externo@example.com";
        Usuario usuario = crearUsuario(email, 20L);
        Hogar hogar = crearHogar(hogarId);
        Tarea tarea = crearTarea(tareaId, hogar);

        TareaRequest.UpdateTareaRequest request = new TareaRequest.UpdateTareaRequest("Nuevo titulo", null, "Cocina", null, null);

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(20L, hogarId)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> tareaService.actualizarTarea(tareaId, request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No perteneces a este hogar");
    }
    //CP 22 cambiar estado a En_progreso

    @Test
    void shouldUpdateTaskStatusToEnProgreso() {

        // Arrange
        Long tareaId = 1L;
        Long hogarId = 1L;

        String email = "ana@example.com";

        Usuario usuario = crearUsuario(email, 10L);
        Hogar hogar = crearHogar(hogarId);
        Tarea tarea = crearTarea(tareaId, hogar);

        TareaRequest.UpdateStatusRequest request = new TareaRequest.UpdateStatusRequest("En_progreso");

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TareaResponse.TareaData result = tareaService.actualizarEstado(tareaId, request, email);

        // Assert
        assertThat(result.estado()).isEqualTo("En_progreso");
    }

    //CP 23 cambiar a Completada

    @Test
    void shouldUpdateTaskStatusToCompleted() {

        // Arrange
        Long tareaId = 1L;
        Long hogarId = 1L;

        String email = "ana@example.com";

        Usuario usuario = crearUsuario(email, 10L);
        Hogar hogar = crearHogar(hogarId);
        Tarea tarea = crearTarea(tareaId, hogar);

        TareaRequest.UpdateStatusRequest request = new TareaRequest.UpdateStatusRequest("Completada");

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);
        when(tareaRepo.save(any(Tarea.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TareaResponse.TareaData result = tareaService.actualizarEstado(tareaId, request, email);

        // Assert
        assertThat(result.estado()).isEqualTo("Completada");
    }

    // CP 24 estado inválido
    @Test
    void shouldThrowWhenStatusIsInvalid() {

        // Arrange
        Long tareaId = 1L;
        Long hogarId = 1L;

        String email = "ana@example.com";

        Usuario usuario = crearUsuario(email, 10L);
        Hogar hogar = crearHogar(hogarId);
        Tarea tarea = crearTarea(tareaId, hogar);

        TareaRequest.UpdateStatusRequest request = new TareaRequest.UpdateStatusRequest("Cancelada");

        when(tareaRepo.findById(tareaId)).thenReturn(Optional.of(tarea));
        when(usuarioRepo.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(usuarioHogarRepo.existsByIdUsuarioIdAndIdHogarId(10L, hogarId)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> tareaService.actualizarEstado(tareaId, request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Estado no valido");
    }
}
