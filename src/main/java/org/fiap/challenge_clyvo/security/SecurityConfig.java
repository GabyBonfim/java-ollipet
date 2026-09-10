package org.fiap.challenge_clyvo.security;

import jakarta.servlet.http.HttpServletRequest;
import org.fiap.challenge_clyvo.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] FERRAMENTAS_DE_APOIO = {
            "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/h2-console/**"
    };

    /**
     * Cadeia da API: sem sessao, sem cookie, autenticacao exclusivamente por token Bearer.
     * O aplicativo mobile envia o ID token do Firebase; Swagger e Insomnia enviam o token
     * emitido por /api/v1/auth/login. Quem resolve qual validador usar e o
     * {@link ConfiguracaoDeTokens#resolvedorPorEmissor}.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain cadeiaDaApi(HttpSecurity http,
                                           AuthenticationManagerResolver<HttpServletRequest> resolvedor,
                                           CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                // Sem isto o navegador bloqueia o app assim que a API sai do localhost.
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        // Consultada na tela de primeiro acesso, antes de existir token.
                        .requestMatchers("/api/v1/veterinarios/e-da-equipe").permitAll()
                        // O aplicativo consulta esta rota a cada login para saber o
                        // perfil de quem entrou, entao todos os perfis passam por aqui —
                        // nao apenas quem ainda nao tem cadastro local.
                        .requestMatchers("/api/v1/auth/registrar")
                        .hasAnyRole(JwtParaUsuarioConverter.PRE_CADASTRO,
                                Role.RESPONSAVEL.name(), Role.VETERINARIO.name(),
                                Role.ADMIN.name())
                        .requestMatchers("/api/v1/agenda/**", "/api/v1/vacinacao/pendencias",
                                "/api/v1/triagem/fila", "/api/v1/tratamentos/aderencia-baixa")
                        .hasRole(Role.VETERINARIO.name())
                        .anyRequest().hasAnyRole(Role.RESPONSAVEL.name(), Role.VETERINARIO.name(),
                                Role.ADMIN.name()))
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(resolvedor))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    /**
     * Fora da API so existem as ferramentas de apoio ao desenvolvimento. Declarar a cadeia
     * explicitamente evita que uma rota nova nasca sem nenhuma protecao por nao casar com
     * nenhum matcher.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain cadeiaDeApoio(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(cabecalhos -> cabecalhos.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(FERRAMENTAS_DE_APOIO).permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Usado por /api/v1/auth/login para conferir e-mail e senha antes de emitir o token. */
    @Bean
    public AuthenticationManager authenticationManager(UsuarioDetailsService detailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
