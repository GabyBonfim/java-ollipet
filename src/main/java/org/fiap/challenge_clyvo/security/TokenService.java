package org.fiap.challenge_clyvo.security;

import org.fiap.challenge_clyvo.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Emite os tokens da propria API, usados pelo Swagger, pelo Insomnia e por qualquer
 * cliente que nao passe pelo Firebase. Os tokens do Firebase seguem caminho proprio
 * e sao apenas validados, nunca emitidos aqui.
 */
@Service
public class TokenService {
    public static final String EMISSOR = "urn:clyvo:ollipet:api";

    private final JwtEncoder jwtEncoder;
    private final Duration expiracao;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.security.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.expiracao = Duration.ofMinutes(expiracaoMinutos);
    }

    public String gerarPara(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracao))
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getRole().name())
                .build();

        // O cabecalho precisa ser explicito: com uma chave simetrica o encoder nao tem
        // como deduzir o algoritmo sozinho.
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    public long getExpiracaoEmSegundos() {
        return expiracao.toSeconds();
    }
}
