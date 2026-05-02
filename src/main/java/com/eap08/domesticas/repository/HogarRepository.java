package com.eap08.domesticas.repository;

import com.eap08.domesticas.model.Hogar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HogarRepository extends JpaRepository<Hogar, Long> {
}
