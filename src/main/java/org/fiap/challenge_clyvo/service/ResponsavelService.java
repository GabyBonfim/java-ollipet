package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.ResponsavelDTO;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.UsuarioMapper;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.repository.ResponsavelRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponsavelService {
    private final ResponsavelRepository responsavelRepository;
    private final UsuarioMapper usuarioMapper;
    private final UsuarioLogado usuarioLogado;

    public ResponsavelService(ResponsavelRepository responsavelRepository,
                              UsuarioMapper usuarioMapper,
                              UsuarioLogado usuarioLogado) {
        this.responsavelRepository = responsavelRepository;
        this.usuarioMapper = usuarioMapper;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional(readOnly = true)
    public ResponsavelDTO meuPerfil() {
        return usuarioMapper.toDTO(usuarioLogado.comoResponsavel());
    }

    @Transactional(readOnly = true)
    public ResponsavelDTO buscarPorId(Long id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Responsavel nao encontrado com id: " + id));
        return usuarioMapper.toDTO(responsavel);
    }

    @Transactional(readOnly = true)
    public Page<ResponsavelDTO> listarTodos(Pageable pageable) {
        return responsavelRepository.findAll(pageable).map(usuarioMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ResponsavelDTO> buscarPorNome(String nome, Pageable pageable) {
        return responsavelRepository.findByNomeContainingIgnoreCase(nome, pageable).map(usuarioMapper::toDTO);
    }
}
