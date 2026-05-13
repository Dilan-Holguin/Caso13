package com.eap08.domesticas.service.impl;

import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldLoadUserByUsername() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash("hashed");

        when(usuarioRepository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("ana@example.com");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("ana@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashed");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        // Arrange
        when(usuarioRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("No existe un usuario");
    }
}
