package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByResponsavelIdOrderByNomeAsc(Long responsavelId);

    Page<Pet> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Pet> findByRacaContainingIgnoreCase(String raca, Pageable pageable);

    Page<Pet> findByResponsavelId(Long responsavelId, Pageable pageable);
}
