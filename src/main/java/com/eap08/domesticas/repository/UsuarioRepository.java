package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring Data genera automáticamente: SELECT * FROM usuario WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // Spring Data genera automáticamente: SELECT COUNT(*) > 0 FROM usuario WHERE email = ?
    boolean existsByEmail(String email);
}