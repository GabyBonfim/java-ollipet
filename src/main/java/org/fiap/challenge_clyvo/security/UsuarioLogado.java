package org.fiap.challenge_clyvo.security;

import org.fiap.challenge_clyvo.exception.AcessoNegadoException;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Ponto unico de leitura do usuario da requisicao atual. Evita que cada servico
 * repita o mesmo trecho de SecurityContextHolder com um cast diferente.
 */
@Component
public class UsuarioLogado {

    public Usuario atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioAutenticado principal) {
            return principal.getUsuario();
        }
        throw new AcessoNegadoException("Nenhum usuario autenticado na requisicao");
    }

    /**
     * O token bruto da requisicao. Vem como principal enquanto o usuario ainda nao tem
     * cadastro local, e como credencial depois que passa a ter — por isso os dois casos.
     */
    public Jwt tokenAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null) {
            if (autenticacao.getPrincipal() instanceof Jwt token) {
                return token;
            }
            if (autenticacao.getCredentials() instanceof Jwt token) {
                return token;
            }
            // O ProviderManager apaga as credenciais apos autenticar, entao quem ja
            // tem cadastro local encontra o token aqui.
            if (autenticacao.getDetails() instanceof Jwt token) {
                return token;
            }
        }
        throw new AcessoNegadoException("Esta operacao exige autenticacao por token");
    }

    public Responsavel comoResponsavel() {
        return exigirTipo(Responsavel.class, "responsavel");
    }

    public Veterinario comoVeterinario() {
        return exigirTipo(Veterinario.class, "veterinario");
    }

    private <T extends Usuario> T exigirTipo(Class<T> tipo, String descricao) {
        Usuario usuario = atual();
        if (!tipo.isInstance(usuario)) {
            throw new AcessoNegadoException("Esta operacao e exclusiva do perfil " + descricao);
        }
        return tipo.cast(usuario);
    }
}
