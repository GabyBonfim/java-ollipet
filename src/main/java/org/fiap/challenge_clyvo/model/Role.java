package org.fiap.challenge_clyvo.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    RESPONSAVEL,
    VETERINARIO,
    /** Administracao da clinica: gerencia a equipe e enxerga todos os cadastros. */
    ADMIN;

    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + name());
    }
}
