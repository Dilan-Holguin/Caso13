package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.eap08.domesticas.security.JwtUtil;
import com.eap08.domesticas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Regla de negocio: no puede existir dos cuentas con el mismo correo
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        // Hasheamos la contraseña ANTES de persistir — nunca se guarda en texto plano
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre());
    }
}