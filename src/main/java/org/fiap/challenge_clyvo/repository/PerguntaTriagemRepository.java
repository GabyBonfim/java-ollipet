package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.PerguntaTriagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerguntaTriagemRepository extends JpaRepository<PerguntaTriagem, Long> {

    /**
     * Traz as perguntas ja com as opcoes carregadas: o protocolo inteiro e devolvido de
     * uma vez ao aplicativo, entao nao faz sentido buscar as opcoes uma a uma depois.
     */
    @Query("""
            SELECT DISTINCT p FROM PerguntaTriagem p
            LEFT JOIN FETCH p.opcoes
            WHERE p.queixa.id = :queixaId
            ORDER BY p.ordem ASC
            """)
    List<PerguntaTriagem> findComOpcoesPorQueixa(@Param("queixaId") Long queixaId);
}
