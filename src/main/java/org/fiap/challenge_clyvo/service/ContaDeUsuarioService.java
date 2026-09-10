package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Dados que todo usuario tem — nome, e-mail, CPF e senha — sao tratados aqui, para que
 * tutores e veterinarios nao repitam a mesma validacao de duplicidade e a mesma
 * codificacao de senha em dois servicos diferentes.
 */
@Service
public class ContaDeUsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaDeUsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Aplica os dados comuns sobre o usuario. A senha so e sobrescrita quando vem
     * preenchida, para que uma atualizacao de cadastro nao apague a senha existente.
     */
    public void aplicarDadosDaConta(Usuario usuario, String nome, String email, String cpf, String senha) {
        exigirEmailDisponivel(email, usuario.getId());
        exigirCpfDisponivel(cpf, usuario.getId());

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCpf(cpf);

        if (senha != null && !senha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(senha));
        }
    }

    private void exigirEmailDisponivel(String email, Long idAtual) {
        if (jaUsadoPorOutro(usuarioRepository.findByEmailIgnoreCase(email), idAtual)) {
            throw new BusinessException("Ja existe um usuario cadastrado com o e-mail " + email);
        }
    }

    private void exigirCpfDisponivel(String cpf, Long idAtual) {
        if (jaUsadoPorOutro(usuarioRepository.findByCpf(cpf), idAtual)) {
            throw new BusinessException("Ja existe um usuario cadastrado com o CPF " + cpf);
        }
    }

    private boolean jaUsadoPorOutro(Optional<Usuario> encontrado, Long idAtual) {
        return encontrado.filter(usuario -> !usuario.getId().equals(idAtual)).isPresent();
    }
}
