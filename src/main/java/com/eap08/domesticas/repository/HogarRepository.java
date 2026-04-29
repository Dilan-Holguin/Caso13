package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.Hogar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la tabla "hogar".
 * La PK es Long (BIGSERIAL), no UUID.
 * Spring Data JPA genera el SQL automáticamente a partir del nombre del método.
 */
@Repository
public interface HogarRepository extends JpaRepository<Hogar, Long> {
    // Por ahora no necesitamos queries personalizadas —
    // findById(Long id) ya viene gratis con JpaRepository
}