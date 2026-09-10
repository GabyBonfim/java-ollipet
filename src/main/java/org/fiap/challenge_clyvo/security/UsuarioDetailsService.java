package org.fiap.challenge_clyvo.security;

import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));

        if (!usuario.possuiSenhaLocal()) {
            throw new UsernameNotFoundException("Usuario '%s' so acessa pelo aplicativo".formatted(email));
        }
        return new UsuarioAutenticado(usuario);
    }
}
