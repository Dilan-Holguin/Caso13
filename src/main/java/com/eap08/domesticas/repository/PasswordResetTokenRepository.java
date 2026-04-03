package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Busca el token en la BD — lo usaremos en el segundo paso del flujo
    Optional<PasswordResetToken> findByToken(String token);

    // Elimina todos los tokens anteriores de un usuario antes de generar uno nuevo
    // Esto evita que un usuario acumule múltiples tokens activos
    void deleteByUsuario_UsuarioId(Long usuarioId);
}