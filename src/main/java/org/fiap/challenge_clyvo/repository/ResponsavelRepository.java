package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Responsavel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {
    Page<Responsavel> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
