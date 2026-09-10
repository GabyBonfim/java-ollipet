package org.fiap.challenge_clyvo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Libera o acesso do aplicativo a API.
 *
 * No celular a chamada nao passa por CORS, mas o Expo tambem roda no navegador
 * (expo start --web), e ali o browser bloqueia qualquer requisicao a outra
 * origem sem esta permissao. Sem isso a versao web para de funcionar assim que
 * a API sai do localhost.
 *
 * As origens permitidas vem de app.cors.origens; o padrao cobre as portas que
 * o Expo usa em desenvolvimento.
 */
@Configuration
public class ConfiguracaoDeCors {

    @Value("${app.cors.origens}")
    private String origensPermitidas;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOriginPatterns(List.of(origensPermitidas.split(",")));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        // A API e stateless e autentica por Bearer token, entao nao ha cookie a enviar.
        configuracao.setAllowCredentials(false);
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", configuracao);
        return fonte;
    }
}
