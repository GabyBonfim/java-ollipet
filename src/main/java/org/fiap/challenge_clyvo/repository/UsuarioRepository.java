package org.fiap.challenge_clyvo.repository;

import org.fiap.challenge_clyvo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByFirebaseUid(String firebaseUid);
}
