package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.OpcaoResposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpcaoRespostaRepository extends JpaRepository<OpcaoResposta, Long> {
    List<OpcaoResposta> findByIdIn(List<Long> ids);
}
