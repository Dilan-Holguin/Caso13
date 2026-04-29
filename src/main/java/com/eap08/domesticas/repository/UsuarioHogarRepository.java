package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.UsuarioHogar;
import com.eap08.domesticas.model.UsuarioHogarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface UsuarioHogarRepository extends JpaRepository<UsuarioHogar, UsuarioHogarId> {

    // Todos los miembros de un hogar específico
    List<UsuarioHogar> findByIdHogarId(Long hogarId);

    // Verificar si un usuario ya pertenece a un hogar
    boolean existsByIdUsuarioIdAndIdHogarId(Long usuarioId, Long hogarId);

    // Obtener el registro específico para verificar el rol del usuario
    Optional<UsuarioHogar> findByIdUsuarioIdAndIdHogarId(Long usuarioId, Long hogarId);
}