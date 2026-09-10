package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /**
     * Todas as consultas tem a mesma duracao, entao dois atendimentos colidem quando o
     * intervalo entre eles e menor que essa duracao — em qualquer ordem.
     */
    @Query("""
            SELECT COUNT(c) > 0 FROM Consulta c
            WHERE c.veterinario.id = :veterinarioId
              AND c.status IN :statusOcupados
              AND c.dataHora > :inicioJanela
              AND c.dataHora < :fimJanela
            """)
    boolean existeConflitoDeAgenda(@Param("veterinarioId") Long veterinarioId,
                                   @Param("statusOcupados") Collection<StatusConsulta> statusOcupados,
                                   @Param("inicioJanela") LocalDateTime inicioJanela,
                                   @Param("fimJanela") LocalDateTime fimJanela);

    List<Consulta> findByVeterinarioIdAndDataHoraBetweenOrderByDataHoraAsc(Long veterinarioId,
                                                                          LocalDateTime inicio,
                                                                          LocalDateTime fim);

    Page<Consulta> findByPetResponsavelIdOrderByDataHoraDesc(Long responsavelId, Pageable pageable);

    Page<Consulta> findByVeterinarioIdAndStatusOrderByDataHoraAsc(Long veterinarioId,
                                                                 StatusConsulta status,
                                                                 Pageable pageable);

    List<Consulta> findByPetIdOrderByDataHoraDesc(Long petId);
}
