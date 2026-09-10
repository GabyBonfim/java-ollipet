package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.Vacina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByEspecieOrderByNomeAsc(Especie especie);
}
