package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByHogarHogarId(Long hogarId);

    List<Tarea> findByHogarHogarIdAndEstado(Long hogarId, String estado);

    List<Tarea> findByHogarHogarIdAndCategoria(Long hogarId, String categoria);

    List<Tarea> findByHogarHogarIdAndAsignadoAUsuarioId(Long hogarId, Long usuarioId);

    @Query("SELECT t FROM Tarea t WHERE t.hogar.hogarId = :hogarId " +
           "AND (:estado IS NULL OR t.estado = :estado) " +
           "AND (:categoria IS NULL OR t.categoria = :categoria) " +
           "AND (:asignadoA IS NULL OR t.asignadoA.usuarioId = :asignadoA)")
    List<Tarea> findWithFilters(@Param("hogarId") Long hogarId,
                                @Param("estado") String estado,
                                @Param("categoria") String categoria,
                                @Param("asignadoA") Long asignadoA);
}
