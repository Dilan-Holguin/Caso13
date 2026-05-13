package com.eap08.domesticas.controller;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.ForgotPasswordRequest;
import com.eap08.domesticas.dto.LoginRequest;
import com.eap08.domesticas.dto.MessageResponse;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.dto.ResetPasswordRequest;
import com.eap08.domesticas.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Ana");
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        AuthResponse response = new AuthResponse("token-123", "ana@example.com", "Ana");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void shouldRejectRegisterWhenEmailIsInvalid() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Ana");
        request.setEmail("bad-email");
        request.setPassword("Password123");

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void shouldRejectRegisterWhenPasswordIsWeak() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Ana");
        request.setEmail("ana@example.com");
        request.setPassword("short");

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void shouldRejectRegisterWhenFieldsAreBlank() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setNombre("");
        request.setEmail("");
        request.setPassword("");

        // Act + Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("ana@example.com");
        request.setPassword("Password123");

        AuthResponse response = new AuthResponse("token-123", "ana@example.com", "Ana");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void shouldRejectLoginWhenEmailIsInvalid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("bad-email");
        request.setPassword("Password123");

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void shouldRejectLoginWhenFieldsAreBlank() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        // Arrange + Act + Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cerrada correctamente")));
    }

    @Test
    void shouldSendRecoveryEmailWhenEmailExists() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("ana@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenReturn(new MessageResponse("OK"));

        // Act + Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void shouldRejectRecoveryWhenEmailIsInvalid() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("bad-email");

        // Act + Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void shouldRejectRecoveryWhenEmailIsBlank() throws Exception {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");

        // Act + Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token-123");
        request.setNuevaPassword("Password123");

        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn(new MessageResponse("Password updated"));

        // Act + Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated"));
    }

    @Test
    void shouldRejectResetPasswordWhenPasswordIsWeak() throws Exception {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token-123");
        request.setNuevaPassword("short");

        // Act + Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any(ResetPasswordRequest.class));
    }
}
