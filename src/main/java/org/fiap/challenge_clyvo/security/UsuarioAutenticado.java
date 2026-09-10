package org.fiap.challenge_clyvo.security;

import org.fiap.challenge_clyvo.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal unico do sistema. Tanto o login por formulario (web) quanto o token JWT
 * (mobile e Swagger) terminam neste objeto, entao o restante do codigo nunca precisa
 * saber por qual porta o usuario entrou.
 */
public class UsuarioAutenticado implements UserDetails {
    private final transient Usuario usuario;

    public UsuarioAutenticado(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(usuario.getRole().toAuthority());
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isAtivo();
    }
}
