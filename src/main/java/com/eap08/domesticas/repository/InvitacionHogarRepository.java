package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.InvitacionHogar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitacionHogarRepository extends JpaRepository<InvitacionHogar, Long> {

    Optional<InvitacionHogar> findByToken(String token);

    boolean existsByEmailInvitadoAndHogarHogarIdAndEstado(
        String emailInvitado, Long hogarId, String estado
    );
}
