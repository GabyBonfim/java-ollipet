package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.PetRequestDTO;
import org.fiap.challenge_clyvo.dto.PetResponseDTO;
import org.fiap.challenge_clyvo.exception.AcessoNegadoException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.PetMapper;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.fiap.challenge_clyvo.repository.PetRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetService {
    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final UsuarioLogado usuarioLogado;

    public PetService(PetRepository petRepository, PetMapper petMapper, UsuarioLogado usuarioLogado) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.usuarioLogado = usuarioLogado;
    }

    /**
     * Ponto unico de leitura de um pet com verificacao de dono. Consultas e vacinacao
     * passam por aqui em vez de repetir a checagem — se a regra mudar, muda em um lugar so.
     */
    @Transactional(readOnly = true)
    public Pet buscarEntidadePermitida(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet nao encontrado com id: " + id));

        Usuario usuario = usuarioLogado.atual();
        boolean permitido = usuario instanceof Veterinario
                || (usuario instanceof Responsavel responsavel && responsavel.ehDonoDe(pet));

        if (!permitido) {
            throw new AcessoNegadoException("Este pet pertence a outro tutor");
        }
        return pet;
    }

    @Transactional(readOnly = true)
    public PetResponseDTO buscarPorId(Long id) {
        return petMapper.toDTO(buscarEntidadePermitida(id));
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> listarDoTutorLogado() {
        return petRepository.findByResponsavelIdOrderByNomeAsc(usuarioLogado.comoResponsavel().getId())
                .stream()
                .map(petMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable).map(petMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return petRepository.findByNomeContainingIgnoreCase(nome, pageable).map(petMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> buscarPorRaca(String raca, Pageable pageable) {
        return petRepository.findByRacaContainingIgnoreCase(raca, pageable).map(petMapper::toDTO);
    }

    @Transactional
    public PetResponseDTO salvar(PetRequestDTO dto) {
        Responsavel dono = usuarioLogado.comoResponsavel();

        Pet pet = new Pet();
        petMapper.copiarParaEntidade(dto, pet);
        pet.setResponsavel(dono);

        return petMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public PetResponseDTO atualizar(Long id, PetRequestDTO dto) {
        Pet pet = buscarEntidadePermitida(id);
        petMapper.copiarParaEntidade(dto, pet);
        return petMapper.toDTO(petRepository.save(pet));
    }

    @Transactional
    public void deletar(Long id) {
        petRepository.delete(buscarEntidadePermitida(id));
    }
}
