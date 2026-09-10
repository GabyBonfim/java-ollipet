package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.LoginRequestDTO;
import org.fiap.challenge_clyvo.dto.LoginResponseDTO;
import org.fiap.challenge_clyvo.dto.RegistroFirebaseDTO;
import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.repository.ResponsavelRepository;
import org.fiap.challenge_clyvo.repository.UsuarioRepository;
import org.fiap.challenge_clyvo.security.TokenService;
import org.fiap.challenge_clyvo.security.UsuarioAutenticado;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ContaDeUsuarioService contaDeUsuario;
    private final UsuarioLogado usuarioLogado;

    public AutenticacaoService(AuthenticationManager authenticationManager,
                               TokenService tokenService,
                               UsuarioRepository usuarioRepository,
                               ResponsavelRepository responsavelRepository,
                               ContaDeUsuarioService contaDeUsuario,
                               UsuarioLogado usuarioLogado) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.responsavelRepository = responsavelRepository;
        this.contaDeUsuario = contaDeUsuario;
        this.usuarioLogado = usuarioLogado;
    }

    public LoginResponseDTO autenticar(LoginRequestDTO dto) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha()));

        Usuario usuario = ((UsuarioAutenticado) autenticacao.getPrincipal()).getUsuario();
        return montarResposta(usuario);
    }

    /**
     * Chamado pelo app logo apos o cadastro no Firebase. Nome e e-mail sao lidos do
     * proprio ID token, entao nao ha como o app declarar uma identidade que nao seja a dele.
     * Repetir a chamada com o mesmo token apenas devolve o cadastro existente.
     */
    @Transactional
    public LoginResponseDTO registrarContaDoFirebase(RegistroFirebaseDTO dto) {
        Jwt token = usuarioLogado.tokenAtual();
        String uid = token.getSubject();

        Usuario existente = usuarioRepository.findByFirebaseUid(uid).orElse(null);
        if (existente != null) {
            return montarResposta(existente);
        }

        String email = token.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new BusinessException("O token do Firebase nao traz um e-mail verificado");
        }

        // A equipe clinica vem da migration e nao nasce com firebase_uid. Quando o
        // e-mail do token ja pertence a um cadastro existente, vinculamos a conta do
        // Firebase a ele em vez de criar um tutor novo: e assim que o veterinario
        // passa a ser reconhecido como VETERINARIO ao entrar pelo aplicativo.
        Usuario cadastroDoEmail = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (cadastroDoEmail != null) {
            cadastroDoEmail.setFirebaseUid(uid);
            return montarResposta(usuarioRepository.save(cadastroDoEmail));
        }

        // Chegando aqui, o cadastro e novo e sera um tutor: o CPF, opcional para
        // quem apenas vincula uma conta existente, passa a ser exigido.
        if (dto.cpf() == null || dto.cpf().isBlank()) {
            throw new BusinessException("CPF e obrigatorio para criar um cadastro de tutor");
        }

        Responsavel responsavel = new Responsavel();
        contaDeUsuario.aplicarDadosDaConta(responsavel, nomeDoToken(token, email), email, dto.cpf(), null);
        responsavel.setDataNascimento(dto.dataNascimento());
        responsavel.setFirebaseUid(uid);

        return montarResposta(responsavelRepository.save(responsavel));
    }

    private String nomeDoToken(Jwt token, String email) {
        String nome = token.getClaimAsString("name");
        return nome == null || nome.isBlank() ? email.split("@")[0] : nome;
    }

    private LoginResponseDTO montarResposta(Usuario usuario) {
        return LoginResponseDTO.bearer(
                tokenService.gerarPara(usuario),
                tokenService.getExpiracaoEmSegundos(),
                usuario.getNome(),
                usuario.getRole());
    }
}
