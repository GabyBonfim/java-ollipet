package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.EncerramentoConsultaDTO;
import org.fiap.challenge_clyvo.dto.ProntuarioResponseDTO;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.ProntuarioMapper;
import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Prontuario;
import org.fiap.challenge_clyvo.repository.ProntuarioRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProntuarioService {
    private final ProntuarioRepository prontuarioRepository;
    private final PetService petService;
    private final ProntuarioMapper prontuarioMapper;
    private final UsuarioLogado usuarioLogado;

    public ProntuarioService(ProntuarioRepository prontuarioRepository,
                             PetService petService,
                             ProntuarioMapper prontuarioMapper,
                             UsuarioLogado usuarioLogado) {
        this.prontuarioRepository = prontuarioRepository;
        this.petService = petService;
        this.prontuarioMapper = prontuarioMapper;
        this.usuarioLogado = usuarioLogado;
    }

    /**
     * Unica forma de nascer um prontuario: o encerramento de uma consulta. Roda dentro
     * da transacao do atendimento, entao nao existe consulta concluida sem historico.
     */
    @Transactional
    public Prontuario registrarAPartirDaConsulta(Consulta consulta, EncerramentoConsultaDTO dto) {
        Prontuario prontuario = new Prontuario();
        prontuario.setPet(consulta.getPet());
        prontuario.setVeterinario(consulta.getVeterinario());
        prontuario.setConsulta(consulta);
        prontuario.setProcedimento(dto.procedimento());
        prontuario.setLocalAtendimento(dto.localAtendimento());
        prontuario.setDataProcedimento(consulta.getDataHora().toLocalDate());

        return prontuarioRepository.save(prontuario);
    }

    @Transactional(readOnly = true)
    public ProntuarioResponseDTO buscarPorId(Long id) {
        Prontuario prontuario = prontuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuario nao encontrado com id: " + id));

        petService.buscarEntidadePermitida(prontuario.getPet().getId());
        return prontuarioMapper.toDTO(prontuario);
    }

    @Transactional(readOnly = true)
    public List<ProntuarioResponseDTO> historicoDoPet(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        return prontuarioRepository.findByPetIdOrderByDataProcedimentoDesc(pet.getId())
                .stream()
                .map(prontuarioMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProntuarioResponseDTO> listarDoVeterinarioLogado(Pageable pageable) {
        return prontuarioRepository.findByVeterinarioId(usuarioLogado.comoVeterinario().getId(), pageable)
                .map(prontuarioMapper::toDTO);
    }
}
