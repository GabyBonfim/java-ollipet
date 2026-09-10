package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.AplicacaoVacina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AplicacaoVacinaRepository extends JpaRepository<AplicacaoVacina, Long> {

    List<AplicacaoVacina> findByPetIdOrderByDataAplicacaoDesc(Long petId);

    @Query("""
            SELECT a FROM AplicacaoVacina a
            WHERE a.pet.id = :petId AND a.vacina.id = :vacinaId
            ORDER BY a.numeroDose DESC
            LIMIT 1
            """)
    Optional<AplicacaoVacina> findUltimaDose(@Param("petId") Long petId, @Param("vacinaId") Long vacinaId);

    /** Doses ja vencidas ou prestes a vencer, usadas na tela de pendencias da clinica. */
    @Query("""
            SELECT a FROM AplicacaoVacina a
            JOIN FETCH a.pet p
            JOIN FETCH a.vacina v
            WHERE a.proximaDose IS NOT NULL
              AND a.proximaDose <= :limite
              AND NOT EXISTS (
                  SELECT 1 FROM AplicacaoVacina posterior
                  WHERE posterior.pet.id = a.pet.id
                    AND posterior.vacina.id = a.vacina.id
                    AND posterior.numeroDose > a.numeroDose
              )
            ORDER BY a.proximaDose ASC
            """)
    List<AplicacaoVacina> findDosesPendentesAte(@Param("limite") LocalDate limite);
}
