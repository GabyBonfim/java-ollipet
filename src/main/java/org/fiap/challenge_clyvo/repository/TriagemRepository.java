package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.StatusTriagem;
import org.fiap.challenge_clyvo.model.Triagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    Page<Triagem> findByPetResponsavelIdOrderByCriadoEmDesc(Long responsavelId, Pageable pageable);

    long countByPetIdAndCriadoEmAfter(Long petId, LocalDateTime desde);

    /**
     * Fila da clinica: triagens graves que ainda nao viraram consulta e ainda estao no
     * prazo de validade.
     */
    @Query("""
            SELECT t FROM Triagem t
            JOIN FETCH t.pet p
            JOIN FETCH t.queixa q
            WHERE t.status = :status
              AND t.expiraEm > :agora
              AND t.classificacao IN (
                  org.fiap.challenge_clyvo.model.ClassificacaoTriagem.EMERGENCIA,
                  org.fiap.challenge_clyvo.model.ClassificacaoTriagem.URGENTE)
            ORDER BY t.classificacao ASC, t.criadoEm ASC
            """)
    List<Triagem> findFilaDeAtendimento(@Param("status") StatusTriagem status,
                                        @Param("agora") LocalDateTime agora);
}
