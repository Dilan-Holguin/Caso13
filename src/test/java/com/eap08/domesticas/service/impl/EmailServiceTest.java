package com.eap08.domesticas.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendRecoveryEmail() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.enviarEmailRecuperacion("ana@example.com", "token-123");

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("ana@example.com");
        assertThat(message.getSubject()).contains("Recuperacion");
        assertThat(message.getText())
                .contains("http://localhost:3000/reset-password?token=token-123")
                .contains("restablecer tu contrasena");
    }
}
