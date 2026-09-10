package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Identidade unica do sistema. Responsavel e Veterinario sao especializacoes gravadas
 * em tabelas proprias (estrategia JOINED), o que permite um unico login para os dois perfis.
 *
 * A senha so existe para quem entra pelo frontend web; usuarios vindos do app mobile
 * sao identificados pelo firebaseUid e nunca tem senha local.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 20)
@NoArgsConstructor
@Getter
@Setter
public abstract class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(length = 100)
    private String senha;

    @Column(name = "firebase_uid", unique = true, length = 128)
    private String firebaseUid;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public abstract Role getRole();

    public boolean possuiSenhaLocal() {
        return senha != null && !senha.isBlank();
    }
}
