package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.PasswordResetToken;
import com.eap08.domesticas.model.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Test
    void shouldFindByTokenWhenTokenExists() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash("hashed");
        usuarioRepository.saveAndFlush(usuario);

        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setToken("token-123");
        token.setExpiracion(LocalDateTime.now().plusMinutes(30));
        tokenRepository.saveAndFlush(token);

        // Act
        var result = tokenRepository.findByToken("token-123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-123");
        assertThat(result.get().getUsuario().getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void shouldDeleteTokensByUsuarioId() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@example.com");
        usuario.setPasswordHash("hashed");
        usuarioRepository.saveAndFlush(usuario);

        PasswordResetToken token = new PasswordResetToken();
        token.setUsuario(usuario);
        token.setToken("token-123");
        token.setExpiracion(LocalDateTime.now().plusMinutes(30));
        tokenRepository.saveAndFlush(token);

        // Act
        tokenRepository.deleteByUsuario_UsuarioId(usuario.getUsuarioId());
        tokenRepository.flush();

        // Assert
        assertThat(tokenRepository.findByToken("token-123")).isEmpty();
    }
}
