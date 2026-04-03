package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Spring Security llama a este método automáticamente durante la autenticación.
    // El parámetro "username" en tu caso es el email, porque ese es tu identificador único.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> User.builder()
                        .username(usuario.getEmail())
                        // Le pasamos el hash que ya está en la BD — Spring Security
                        // se encarga de compararlo con la contraseña que envió el cliente
                        .password(usuario.getPasswordHash())
                        // Por ahora todos los usuarios tienen el mismo rol.
                        // Cuando implementemos roles (Administrador/Miembro) esto cambiará.
                        .roles("USER")
                        .build())
                // Si no encuentra el email, lanza esta excepción que Spring Security
                // intercepta y convierte en un error de autenticación
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario con el correo: " + email));
    }
}