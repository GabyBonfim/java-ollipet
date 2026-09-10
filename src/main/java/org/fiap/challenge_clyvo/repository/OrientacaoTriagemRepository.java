package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;
import org.fiap.challenge_clyvo.model.OrientacaoTriagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrientacaoTriagemRepository extends JpaRepository<OrientacaoTriagem, Long> {
    List<OrientacaoTriagem> findByQueixaIdAndClassificacaoOrderByOrdemAsc(Long queixaId,
                                                                         ClassificacaoTriagem classificacao);
}
