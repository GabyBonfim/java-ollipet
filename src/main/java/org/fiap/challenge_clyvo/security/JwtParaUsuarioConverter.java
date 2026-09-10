package org.fiap.challenge_clyvo.security;

import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.repository.UsuarioRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * O token so prova quem a pessoa e; quem decide o que ela pode fazer e o banco.
 * Um token do Firebase e localizado pelo uid, um token da propria API pelo e-mail.
 *
 * Quem apresenta um token valido do Firebase mas ainda nao tem cadastro local recebe
 * apenas {@link #AUTORIDADE_PRE_CADASTRO}, suficiente para chamar o endpoint de
 * registro e nada mais.
 */
@Component
public class JwtParaUsuarioConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    public static final String PRE_CADASTRO = "PRE_CADASTRO";
    public static final SimpleGrantedAuthority AUTORIDADE_PRE_CADASTRO =
            new SimpleGrantedAuthority("ROLE_" + PRE_CADASTRO);

    private final UsuarioRepository usuarioRepository;

    public JwtParaUsuarioConverter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return localizarUsuario(jwt)
                .filter(Usuario::isAtivo)
                .<AbstractAuthenticationToken>map(usuario -> {
                    UsuarioAutenticado principal = new UsuarioAutenticado(usuario);
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                            principal, jwt, principal.getAuthorities());
                    // O token vai tambem em details porque o ProviderManager apaga as
                    // credenciais depois de autenticar, e quem ja tem cadastro local
                    // perderia o Jwt — deixando /auth/registrar sem token para ler.
                    autenticacao.setDetails(jwt);
                    return autenticacao;
                })
                .orElseGet(() -> new JwtAuthenticationToken(jwt, List.of(AUTORIDADE_PRE_CADASTRO)));
    }

    private Optional<Usuario> localizarUsuario(Jwt jwt) {
        // getClaimAsString em vez de getIssuer(): o emissor local e uma URN, e getIssuer()
        // insiste em converter a claim para URL.
        if (TokenService.EMISSOR.equals(jwt.getClaimAsString(JwtClaimNames.ISS))) {
            return usuarioRepository.findByEmailIgnoreCase(jwt.getSubject());
        }
        return usuarioRepository.findByFirebaseUid(jwt.getSubject());
    }
}
