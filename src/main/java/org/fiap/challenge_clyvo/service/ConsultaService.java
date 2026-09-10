package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.CancelamentoConsultaDTO;
import org.fiap.challenge_clyvo.dto.ConsultaRequestDTO;
import org.fiap.challenge_clyvo.dto.ConsultaResponseDTO;
import org.fiap.challenge_clyvo.dto.EncerramentoConsultaDTO;
import org.fiap.challenge_clyvo.exception.AcessoNegadoException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.ConsultaMapper;
import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.StatusConsulta;
import org.fiap.challenge_clyvo.model.Usuario;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.fiap.challenge_clyvo.repository.ConsultaRepository;
import org.fiap.challenge_clyvo.repository.VeterinarioRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Fluxo 1 — agendamento de consulta.
 *
 * <pre>
 *   SOLICITADA --confirmar--> CONFIRMADA --iniciar--> EM_ATENDIMENTO --concluir--> CONCLUIDA
 *        |                        |                                                    |
 *        +---------cancelar-------+--------registrarFalta--> NAO_COMPARECEU      gera prontuario
 * </pre>
 *
 * Quem valida cada salto e o proprio enum {@link StatusConsulta}; quem valida horario e
 * {@link PoliticaDeAgendamento}. Aqui fica so a orquestracao e a checagem de quem pode agir.
 */
@Service
public class ConsultaService {
    private final ConsultaRepository consultaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PetService petService;
    private final ProntuarioService prontuarioService;
    private final TriagemService triagemService;
    private final PoliticaDeAgendamento politica;
    private final ConsultaMapper consultaMapper;
    private final UsuarioLogado usuarioLogado;

    public ConsultaService(ConsultaRepository consultaRepository,
                           VeterinarioRepository veterinarioRepository,
                           PetService petService,
                           ProntuarioService prontuarioService,
                           TriagemService triagemService,
                           PoliticaDeAgendamento politica,
                           ConsultaMapper consultaMapper,
                           UsuarioLogado usuarioLogado) {
        this.consultaRepository = consultaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.petService = petService;
        this.prontuarioService = prontuarioService;
        this.triagemService = triagemService;
        this.politica = politica;
        this.consultaMapper = consultaMapper;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional
    public ConsultaResponseDTO solicitar(ConsultaRequestDTO dto) {
        Pet pet = petService.buscarEntidadePermitida(dto.petId());
        Veterinario veterinario = buscarVeterinario(dto.veterinarioId());

        politica.validarNovoHorario(veterinario.getId(), dto.dataHora());

        Consulta consulta = new Consulta();
        consulta.setPet(pet);
        consulta.setVeterinario(veterinario);
        consulta.setDataHora(dto.dataHora());
        consulta.setMotivo(dto.motivo());
        consulta.setStatus(StatusConsulta.SOLICITADA);

        Consulta salva = consultaRepository.save(consulta);

        // Mesma transacao: se a triagem nao puder ser encaminhada, a consulta tambem nao nasce.
        if (dto.veioDeTriagem()) {
            triagemService.vincularAConsulta(dto.triagemId(), salva);
        }
        return consultaMapper.toDTO(salva);
    }

    @Transactional
    public ConsultaResponseDTO confirmar(Long id) {
        return moverComoVeterinario(id, StatusConsulta.CONFIRMADA);
    }

    @Transactional
    public ConsultaResponseDTO iniciarAtendimento(Long id) {
        return moverComoVeterinario(id, StatusConsulta.EM_ATENDIMENTO);
    }

    @Transactional
    public ConsultaResponseDTO registrarFalta(Long id) {
        return moverComoVeterinario(id, StatusConsulta.NAO_COMPARECEU);
    }

    /**
     * Encerrar o atendimento e gravar o prontuario sao o mesmo ato: se a gravacao falhar,
     * a consulta nao fica marcada como concluida sem historico clinico.
     */
    @Transactional
    public ConsultaResponseDTO concluir(Long id, EncerramentoConsultaDTO dto) {
        Consulta consulta = buscarDoVeterinarioLogado(id);
        consulta.moverPara(StatusConsulta.CONCLUIDA);
        consulta.setObservacoes(dto.observacoes());

        prontuarioService.registrarAPartirDaConsulta(consulta, dto);

        return consultaMapper.toDTO(consultaRepository.save(consulta));
    }

    /** Tutor e veterinario podem cancelar, cada um dentro das proprias restricoes. */
    @Transactional
    public ConsultaResponseDTO cancelar(Long id, CancelamentoConsultaDTO dto) {
        Consulta consulta = buscarComAcesso(id);

        if (usuarioLogado.atual() instanceof Responsavel) {
            politica.validarCancelamento(consulta);
        }
        consulta.moverPara(StatusConsulta.CANCELADA);
        consulta.setObservacoes(dto.motivo());

        return consultaMapper.toDTO(consultaRepository.save(consulta));
    }

    @Transactional(readOnly = true)
    public ConsultaResponseDTO buscarPorId(Long id) {
        return consultaMapper.toDTO(buscarComAcesso(id));
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> agendaDoDia(LocalDate data) {
        Veterinario veterinario = usuarioLogado.comoVeterinario();
        return consultaRepository.findByVeterinarioIdAndDataHoraBetweenOrderByDataHoraAsc(
                        veterinario.getId(), politica.inicioDoDia(data), politica.fimDoDia(data))
                .stream()
                .map(consultaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> solicitacoesPendentes(Pageable pageable) {
        Veterinario veterinario = usuarioLogado.comoVeterinario();
        return consultaRepository
                .findByVeterinarioIdAndStatusOrderByDataHoraAsc(veterinario.getId(), StatusConsulta.SOLICITADA, pageable)
                .map(consultaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> minhasConsultas(Pageable pageable) {
        Responsavel responsavel = usuarioLogado.comoResponsavel();
        return consultaRepository.findByPetResponsavelIdOrderByDataHoraDesc(responsavel.getId(), pageable)
                .map(consultaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> historicoDoPet(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        return consultaRepository.findByPetIdOrderByDataHoraDesc(pet.getId())
                .stream()
                .map(consultaMapper::toDTO)
                .toList();
    }

    private ConsultaResponseDTO moverComoVeterinario(Long id, StatusConsulta destino) {
        Consulta consulta = buscarDoVeterinarioLogado(id);
        consulta.moverPara(destino);
        return consultaMapper.toDTO(consultaRepository.save(consulta));
    }

    private Consulta buscarDoVeterinarioLogado(Long id) {
        Consulta consulta = buscarEntidade(id);
        if (!consulta.ehDoVeterinario(usuarioLogado.comoVeterinario())) {
            throw new AcessoNegadoException("Esta consulta esta na agenda de outro veterinario");
        }
        return consulta;
    }

    private Consulta buscarComAcesso(Long id) {
        Consulta consulta = buscarEntidade(id);
        Usuario usuario = usuarioLogado.atual();

        boolean permitido = false;
        if (usuario instanceof Veterinario veterinario) {
            permitido = consulta.ehDoVeterinario(veterinario);
        } else if (usuario instanceof Responsavel responsavel) {
            permitido = consulta.pertenceA(responsavel);
        }

        if (!permitido) {
            throw new AcessoNegadoException("Esta consulta nao pertence a voce");
        }
        return consulta;
    }

    private Consulta buscarEntidade(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada com id: " + id));
    }

    private Veterinario buscarVeterinario(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario nao encontrado com id: " + id));
    }
}
