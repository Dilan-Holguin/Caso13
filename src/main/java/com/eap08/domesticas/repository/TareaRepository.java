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

    @Query("SELECT t FROM Tarea t WHERE t.hogar.hogarId = :hogarId " +
       "AND t.fechaLimite < CURRENT_TIMESTAMP " +
       "AND t.estado <> 'Completada' " +
       "ORDER BY t.fechaLimite ASC")
    List<Tarea> findTareasVencidas(@Param("hogarId") Long hogarId);

    @Query("SELECT u.usuarioId, u.nombre, COUNT(t) AS total, " +
           "SUM(CASE WHEN t.estado = 'Pendiente' THEN 1 ELSE 0 END) AS pendientes, " +
           "SUM(CASE WHEN t.estado = 'En_progreso' THEN 1 ELSE 0 END) AS enProgreso, " +
           "SUM(CASE WHEN t.estado = 'Completada' THEN 1 ELSE 0 END) AS completadas " +
           "FROM Tarea t LEFT JOIN t.asignadoA u " +
           "WHERE t.hogar.hogarId = :hogarId " +
           "GROUP BY u.usuarioId, u.nombre ORDER BY total DESC")
    List<Object[]> distribucionPorMiembro(@Param("hogarId") Long hogarId);

    @Query("SELECT u.usuarioId, u.nombre, COUNT(t) AS totalAsignadas, " +
           "SUM(CASE WHEN t.estado = 'Completada' THEN 1 ELSE 0 END) AS completadas, " +
           "SUM(CASE WHEN t.estado = 'Completada' AND t.completadaAt IS NOT NULL AND t.fechaLimite IS NOT NULL AND t.completadaAt <= t.fechaLimite THEN 1 ELSE 0 END) AS aTiempo, " +
           "SUM(CASE WHEN t.estado = 'Completada' AND t.completadaAt IS NOT NULL AND t.fechaLimite IS NOT NULL AND t.completadaAt > t.fechaLimite THEN 1 ELSE 0 END) AS tarde " +
           "FROM Tarea t LEFT JOIN t.asignadoA u " +
           "WHERE t.hogar.hogarId = :hogarId " +
           "GROUP BY u.usuarioId, u.nombre ORDER BY completadas DESC")
    List<Object[]> cumplimientoPorUsuario(@Param("hogarId") Long hogarId);

}
