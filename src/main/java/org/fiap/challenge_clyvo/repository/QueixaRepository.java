package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.Queixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueixaRepository extends JpaRepository<Queixa, Long> {

    /** Queixas gerais mais as especificas da especie informada. */
    @Query("""
            SELECT q FROM Queixa q
            WHERE q.especie IS NULL OR q.especie = :especie
            ORDER BY q.ordem ASC
            """)
    List<Queixa> findAplicaveisA(@Param("especie") Especie especie);
}
