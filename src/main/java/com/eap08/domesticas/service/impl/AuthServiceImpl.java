package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.dto.*;
import com.eap08.domesticas.model.PasswordResetToken;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.PasswordResetTokenRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.eap08.domesticas.security.JwtUtil;
import com.eap08.domesticas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuarioRepository.save(usuario);
        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre());
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // Buscamos el usuario — si no existe, devolvemos el mismo mensaje de éxito
        // deliberadamente. Esto es una práctica de seguridad: si dijéramos "ese correo
        // no está registrado", le estaríamos dando información útil a alguien que
        // está tratando de descubrir qué emails están registrados en el sistema.
        usuarioRepository.findByEmail(request.getEmail()).ifPresent(usuario -> {
            // Eliminamos tokens anteriores para que no queden tokens huérfanos
            tokenRepository.deleteByUsuario_UsuarioId(usuario.getUsuarioId());

            // Generamos un token aleatorio criptográficamente seguro
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUsuario(usuario);
            resetToken.setToken(token);
            // El token expira en 30 minutos desde ahora
            resetToken.setExpiracion(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(resetToken);

            // Enviamos el email con el enlace de recuperación
            emailService.enviarEmailRecuperacion(usuario.getEmail(), token);
        });

        return new MessageResponse(
            "Si ese correo está registrado, recibirás un enlace de recuperación en breve"
        );
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Buscamos el token en la BD
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("El token no es válido"));

        // Verificamos que no haya expirado
        if (resetToken.getExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado. Solicita uno nuevo");
        }

        // Verificamos que no haya sido usado antes
        if (resetToken.isUsado()) {
            throw new RuntimeException("Este token ya fue utilizado");
        }

        // Todo está bien — actualizamos la contraseña
        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);

        // Marcamos el token como usado para que no pueda reutilizarse
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        return new MessageResponse("Contraseña actualizada correctamente");
    }
}