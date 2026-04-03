package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.LoginRequest;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.eap08.domesticas.security.JwtUtil;
import com.eap08.domesticas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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
        // authenticate() internamente llama a UserDetailsServiceImpl.loadUserByUsername()
        // para cargar el usuario, y luego compara la contraseña con BCrypt.
        // Si las credenciales son incorrectas, lanza BadCredentialsException automáticamente
        // que el GlobalExceptionHandler capturará como un error de negocio.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si llegamos aquí, la autenticación fue exitosa.
        // Buscamos el usuario completo para incluir el nombre en la respuesta.
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre());
    }
}