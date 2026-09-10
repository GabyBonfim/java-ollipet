package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Veterinario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    List<Veterinario> findByAtivoTrueOrderByNomeAsc();

    Page<Veterinario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /** O CRMV e unico: dois cadastros nao podem dividir o mesmo registro. */
    Optional<Veterinario> findByCrmv(String crmv);

    /** Usada no primeiro acesso, para saber se o e-mail e de um vet da equipe. */
    Optional<Veterinario> findByEmailIgnoreCase(String email);
}
