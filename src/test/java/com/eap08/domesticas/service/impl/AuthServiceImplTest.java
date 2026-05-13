package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.ForgotPasswordRequest;
import com.eap08.domesticas.dto.LoginRequest;
import com.eap08.domesticas.dto.MessageResponse;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.dto.ResetPasswordRequest;
import com.eap08.domesticas.model.PasswordResetToken;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.PasswordResetTokenRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.eap08.domesticas.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Ana");
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed");
        when(jwtUtil.generateToken("ana@example.com")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.getNombre()).isEqualTo("Ana");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario saved = usuarioCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getNombre()).isEqualTo("Ana");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        verify(passwordEncoder).encode("Password123");
        verify(jwtUtil).generateToken("ana@example.com");
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Ana");
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una cuenta");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setNombre("Ana");
        usuario.setPasswordHash("hashed");
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken("ana@example.com")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.getNombre()).isEqualTo("Ana");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken token = authCaptor.getValue();
        assertThat(token.getPrincipal()).isEqualTo("ana@example.com");
        assertThat(token.getCredentials()).isEqualTo("Password123");
        verify(jwtUtil).generateToken("ana@example.com");
    }

    @Test
    void shouldThrowWhenCredentialsAreInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verifyNoInteractions(usuarioRepository, jwtUtil);
    }

    @Test
    void shouldThrowWhenUserNotFoundAfterAuthentication() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void shouldSendRecoveryEmailWhenEmailExists() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("ana@example.com");

        Usuario usuario = new Usuario();
        usuario.setUsuarioId(42L);
        usuario.setEmail("ana@example.com");
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        LocalDateTime start = LocalDateTime.now();
        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        // Act
        MessageResponse response = authService.forgotPassword(request);

        // Assert
        verify(tokenRepository).deleteByUsuario_UsuarioId(42L);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUsuario()).isEqualTo(usuario);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiracion()).isAfterOrEqualTo(start);
        assertThat(savedToken.getExpiracion()).isBeforeOrEqualTo(start.plusMinutes(31));
        verify(emailService).enviarEmailRecuperacion(eq("ana@example.com"), eq(savedToken.getToken()));
        assertThat(response.getMessage()).contains("Si ese correo");
    }

    @Test
    void shouldReturnGenericMessageWhenEmailNotFound() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");
        when(usuarioRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // Act
        MessageResponse response = authService.forgotPassword(request);

        // Assert
        assertThat(response.getMessage()).contains("Si ese correo");
        verify(tokenRepository, never()).deleteByUsuario_UsuarioId(anyLong());
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).enviarEmailRecuperacion(anyString(), anyString());
    }

    @Test
    void shouldThrowWhenResetTokenNotFound() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("missing-token");
        request.setNuevaPassword("NewPassword123");

        when(tokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El token no es válido");

        verifyNoInteractions(usuarioRepository);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldThrowWhenResetTokenExpired() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNuevaPassword("NewPassword123");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setExpiracion(LocalDateTime.now().minusMinutes(1));
        token.setUsado(false);
        token.setUsuario(new Usuario());
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        // Act + Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El token ha expirado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void shouldThrowWhenResetTokenAlreadyUsed() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("used-token");
        request.setNuevaPassword("NewPassword123");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("used-token");
        token.setExpiracion(LocalDateTime.now().plusMinutes(5));
        token.setUsado(true);
        token.setUsuario(new Usuario());
        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        // Act + Assert
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya fue utilizado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNuevaPassword("NewPassword123");

        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUsuario(usuario);
        token.setExpiracion(LocalDateTime.now().plusMinutes(10));
        token.setUsado(false);

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");

        // Act
        MessageResponse response = authService.resetPassword(request);

        // Assert
        assertThat(response.getMessage()).contains("actualizada correctamente");
        assertThat(usuario.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.isUsado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }
}
