package com.eap08.domesticas.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void enviarEmailRecuperacion(String destinatario, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("no-reply@domesticas.com");
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña — Domésticas");
        mensaje.setText(
            "Hola,\n\n" +
            "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
            "Usa el siguiente enlace para crear una nueva contraseña. " +
            "Este enlace expira en 30 minutos:\n\n" +
            frontendUrl + "/reset-password?token=" + token + "\n\n" +
            "Si no solicitaste este cambio, puedes ignorar este correo.\n\n" +
            "El equipo de Domésticas"
        );
        mailSender.send(mensaje);
    }
}