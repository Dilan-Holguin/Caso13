package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldFindByEmailWhenUserExists() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash("hashed");
        usuario.setNombre("Ana");
        usuarioRepository.saveAndFlush(usuario);

        // Act
        var result = usuarioRepository.findByEmail("ana@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        // Arrange
        usuarioRepository.deleteAll();

        // Act
        boolean exists = usuarioRepository.existsByEmail("missing@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash("hashed");
        usuarioRepository.saveAndFlush(usuario);

        // Act
        boolean exists = usuarioRepository.existsByEmail("ana@example.com");

        // Assert
        assertThat(exists).isTrue();
    }
}
