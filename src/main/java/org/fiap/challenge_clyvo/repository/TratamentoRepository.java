package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.StatusTratamento;
import org.fiap.challenge_clyvo.model.Tratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TratamentoRepository extends JpaRepository<Tratamento, Long> {

    List<Tratamento> findByPetIdOrderByInicioEmDesc(Long petId);

    List<Tratamento> findByPetIdAndStatus(Long petId, StatusTratamento status);

    boolean existsByPetIdAndMedicamentoIgnoreCaseAndStatus(Long petId, String medicamento,
                                                           StatusTratamento status);

    /** Tratamentos ativos de todos os pets de um tutor, com as doses ja carregadas. */
    @Query("""
            SELECT DISTINCT t FROM Tratamento t
            JOIN FETCH t.doses
            JOIN FETCH t.pet p
            WHERE p.responsavel.id = :responsavelId AND t.status = :status
            ORDER BY t.inicioEm ASC
            """)
    List<Tratamento> findAtivosDoResponsavel(@Param("responsavelId") Long responsavelId,
                                             @Param("status") StatusTratamento status);

    /** Tratamentos ativos da clinica, para o painel de acompanhamento de adesao. */
    @Query("""
            SELECT DISTINCT t FROM Tratamento t
            JOIN FETCH t.doses
            JOIN FETCH t.pet p
            JOIN FETCH p.responsavel
            WHERE t.veterinario.id = :veterinarioId AND t.status = :status
            ORDER BY t.inicioEm ASC
            """)
    List<Tratamento> findAtivosDoVeterinario(@Param("veterinarioId") Long veterinarioId,
                                             @Param("status") StatusTratamento status);
}
