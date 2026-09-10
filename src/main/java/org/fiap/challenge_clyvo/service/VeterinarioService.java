package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.NovoVeterinarioDTO;
import org.fiap.challenge_clyvo.dto.VeterinarioDTO;
import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.UsuarioMapper;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.fiap.challenge_clyvo.repository.VeterinarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeterinarioService {
    private final VeterinarioRepository veterinarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final ContaDeUsuarioService contaDeUsuario;

    public VeterinarioService(VeterinarioRepository veterinarioRepository,
                              UsuarioMapper usuarioMapper,
                              ContaDeUsuarioService contaDeUsuario) {
        this.veterinarioRepository = veterinarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.contaDeUsuario = contaDeUsuario;
    }

    /**
     * Cadastra um veterinario na equipe. Exclusivo da administracao da clinica.
     *
     * Nao ha senha: o veterinario entra pelo Firebase, e o primeiro login casa o
     * e-mail com este cadastro para gravar o firebase_uid.
     */
    @Transactional
    public VeterinarioDTO cadastrar(NovoVeterinarioDTO dto) {
        if (veterinarioRepository.findByCrmv(dto.crmv()).isPresent()) {
            throw new BusinessException("Ja existe um veterinario com o CRMV " + dto.crmv());
        }

        Veterinario veterinario = new Veterinario();
        // Reaproveita as checagens de e-mail e CPF ja usadas nos demais cadastros.
        contaDeUsuario.aplicarDadosDaConta(veterinario, dto.nome(), dto.email(), dto.cpf(), null);
        veterinario.setCrmv(dto.crmv());
        veterinario.setEspecialidade(dto.especialidade());

        return usuarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    /**
     * Remove o veterinario da equipe.
     *
     * Desativa em vez de apagar: as consultas e os prontuarios que ele atendeu
     * continuam apontando para o cadastro, e o historico clinico nao pode perder
     * quem assinou o atendimento. Desativado, ele deixa de aparecer para
     * agendamento e perde o acesso — o token dele para de ser aceito.
     */
    @Transactional
    public void desativar(Long id) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado com id: " + id));

        if (!veterinario.isAtivo()) {
            throw new BusinessException("Este veterinario ja esta inativo");
        }

        veterinario.setAtivo(false);
        veterinarioRepository.save(veterinario);
    }

    /**
     * Diz se o e-mail pertence a um veterinario ativo da clinica.
     *
     * A tela de primeiro acesso consulta isto antes de criar a conta no Firebase:
     * so quem a administracao cadastrou consegue virar veterinario no aplicativo.
     * Devolve apenas um booleano, sem expor nome, CPF ou CRMV a quem nao esta
     * autenticado.
     */
    @Transactional(readOnly = true)
    public boolean ehVeterinarioCadastrado(String email) {
        return veterinarioRepository.findByEmailIgnoreCase(email)
                .filter(Veterinario::isAtivo)
                .isPresent();
    }

    /** Lista enxuta usada pelo tutor na hora de escolher com quem marcar. */
    @Transactional(readOnly = true)
    public List<VeterinarioDTO> listarDisponiveis() {
        return veterinarioRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VeterinarioDTO buscarPorId(Long id) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado com id: " + id));
        return usuarioMapper.toDTO(veterinario);
    }

    @Transactional(readOnly = true)
    public Page<VeterinarioDTO> listarTodos(Pageable pageable) {
        return veterinarioRepository.findAll(pageable).map(usuarioMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<VeterinarioDTO> buscarPorNome(String nome, Pageable pageable) {
        return veterinarioRepository.findByNomeContainingIgnoreCase(nome, pageable).map(usuarioMapper::toDTO);
    }
}
