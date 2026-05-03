package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.UsuarioHogar;
import com.eap08.domesticas.model.UsuarioHogarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioHogarRepository extends JpaRepository<UsuarioHogar, UsuarioHogarId> {

    List<UsuarioHogar> findByIdHogarId(Long hogarId);

    boolean existsByIdUsuarioIdAndIdHogarId(Long usuarioId, Long hogarId);

    Optional<UsuarioHogar> findByIdUsuarioIdAndIdHogarId(Long usuarioId, Long hogarId);

    List<UsuarioHogar> findByIdUsuarioId(Long usuarioId);
}
