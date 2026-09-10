package org.fiap.challenge_clyvo.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A API aceita duas origens de token e escolhe o validador pela claim {@code iss}:
 *
 * <ul>
 *   <li>{@link TokenService#EMISSOR} — tokens HMAC emitidos aqui, usados pelo frontend
 *       web, pelo Swagger e por clientes de teste;</li>
 *   <li>{@code https://securetoken.google.com/<projeto>} — ID tokens RS256 do Firebase
 *       Authentication, validados contra as chaves publicas do Google. O app mobile
 *       manda o mesmo token que ja recebe no login, sem precisar de um segundo login.</li>
 * </ul>
 *
 * Sem {@code app.security.firebase.project-id} configurado, so o emissor local e aceito
 * e a aplicacao continua subindo normalmente.
 */
@Configuration
public class ConfiguracaoDeTokens {
    private static final String JWK_SET_FIREBASE =
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
    private static final String PREFIXO_EMISSOR_FIREBASE = "https://securetoken.google.com/";

    private final String segredoLocal;
    private final String projetoFirebase;

    public ConfiguracaoDeTokens(@Value("${app.security.jwt.secret}") String segredoLocal,
                                @Value("${app.security.firebase.project-id}") String projetoFirebase) {
        this.segredoLocal = segredoLocal;
        this.projetoFirebase = projetoFirebase;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveLocal()));
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> resolvedorPorEmissor(
            JwtParaUsuarioConverter conversor) {

        Map<String, AuthenticationManager> porEmissor = new HashMap<>();
        porEmissor.put(TokenService.EMISSOR, gerenciadorPara(decoderLocal(), conversor));

        if (StringUtils.hasText(projetoFirebase)) {
            porEmissor.put(PREFIXO_EMISSOR_FIREBASE + projetoFirebase,
                    gerenciadorPara(decoderFirebase(), conversor));
        }
        return new JwtIssuerAuthenticationManagerResolver(porEmissor::get);
    }

    private AuthenticationManager gerenciadorPara(JwtDecoder decoder, JwtParaUsuarioConverter conversor) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(conversor);
        return new ProviderManager(provider);
    }

    private JwtDecoder decoderLocal() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chaveLocal())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.EMISSOR));
        return decoder;
    }

    private JwtDecoder decoderFirebase() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_FIREBASE)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(validadoresDoFirebase());
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> validadoresDoFirebase() {
        // O Firebase usa o proprio ID do projeto como audience; sem esta checagem um token
        // legitimo de outro projeto Firebase qualquer seria aceito.
        OAuth2TokenValidator<Jwt> audience = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, aud -> aud != null && aud.contains(projetoFirebase));

        return new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(PREFIXO_EMISSOR_FIREBASE + projetoFirebase),
                audience);
    }

    private SecretKey chaveLocal() {
        return new SecretKeySpec(segredoLocal.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
