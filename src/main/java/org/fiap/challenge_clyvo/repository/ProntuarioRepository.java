package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Prontuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long> {
    List<Prontuario> findByPetIdOrderByDataProcedimentoDesc(Long petId);

    Page<Prontuario> findByVeterinarioId(Long veterinarioId, Pageable pageable);
}
