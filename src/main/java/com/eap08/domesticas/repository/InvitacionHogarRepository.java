package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.InvitacionHogar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitacionHogarRepository extends JpaRepository<InvitacionHogar, Long> {

    // Búsqueda principal: el invitado llega con su token y lo buscamos aquí
    Optional<InvitacionHogar> findByToken(String token);

    // Evitar invitaciones duplicadas al mismo email en el mismo hogar
    boolean existsByEmailInvitadoAndHogarHogarIdAndEstado(
        String emailInvitado, Long hogarId, String estado
    );
}