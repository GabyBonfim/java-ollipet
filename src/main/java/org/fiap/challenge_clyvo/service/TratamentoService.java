package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.DoseTratamentoDTO;
import org.fiap.challenge_clyvo.dto.EncerramentoTratamentoDTO;
import org.fiap.challenge_clyvo.dto.TratamentoRequestDTO;
import org.fiap.challenge_clyvo.dto.TratamentoResponseDTO;
import org.fiap.challenge_clyvo.exception.AcessoNegadoException;
import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.TratamentoMapper;
import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.DoseTratamento;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.StatusConsulta;
import org.fiap.challenge_clyvo.model.StatusTratamento;
import org.fiap.challenge_clyvo.model.Tratamento;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.fiap.challenge_clyvo.repository.ConsultaRepository;
import org.fiap.challenge_clyvo.repository.DoseTratamentoRepository;
import org.fiap.challenge_clyvo.repository.TratamentoRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Fluxo 4 — tratamento em casa.
 *
 * O veterinario prescreve uma vez; a API transforma a prescricao em horarios concretos e
 * o tutor confirma dose a dose pelo aplicativo. O que volta para a clinica nao e "o
 * tutor disse que deu o remedio", e sim quantas doses vencidas foram confirmadas.
 */
@Service
public class TratamentoService {
    private final TratamentoRepository tratamentoRepository;
    private final DoseTratamentoRepository doseRepository;
    private final ConsultaRepository consultaRepository;
    private final PetService petService;
    private final PlanoDeDoses planoDeDoses;
    private final TratamentoMapper tratamentoMapper;
    private final UsuarioLogado usuarioLogado;
    private final int limiteDeAderenciaBaixa;

    public TratamentoService(TratamentoRepository tratamentoRepository,
                             DoseTratamentoRepository doseRepository,
                             ConsultaRepository consultaRepository,
                             PetService petService,
                             PlanoDeDoses planoDeDoses,
                             TratamentoMapper tratamentoMapper,
                             UsuarioLogado usuarioLogado,
                             @Value("${app.tratamento.limite-aderencia-baixa}") int limiteDeAderenciaBaixa) {
        this.tratamentoRepository = tratamentoRepository;
        this.doseRepository = doseRepository;
        this.consultaRepository = consultaRepository;
        this.petService = petService;
        this.planoDeDoses = planoDeDoses;
        this.tratamentoMapper = tratamentoMapper;
        this.usuarioLogado = usuarioLogado;
        this.limiteDeAderenciaBaixa = limiteDeAderenciaBaixa;
    }

    @Transactional
    public TratamentoResponseDTO prescrever(TratamentoRequestDTO dto) {
        Pet pet = petService.buscarEntidadePermitida(dto.petId());
        Veterinario veterinario = usuarioLogado.comoVeterinario();

        planoDeDoses.quantidadeDeDoses(dto.duracaoDias(), dto.intervaloHoras());
        exigirMedicamentoDisponivel(pet, dto.medicamento());

        Tratamento tratamento = new Tratamento();
        tratamento.setPet(pet);
        tratamento.setVeterinario(veterinario);
        tratamento.setConsulta(consultaDeOrigem(dto.consultaId(), pet, veterinario));
        tratamento.setMedicamento(dto.medicamento());
        tratamento.setDosagem(dto.dosagem());
        tratamento.setIntervaloHoras(dto.intervaloHoras());
        tratamento.setDuracaoDias(dto.duracaoDias());
        tratamento.setInicioEm(dto.inicioEm());
        tratamento.setObservacoes(dto.observacoes());
        tratamento.setStatus(StatusTratamento.EM_ANDAMENTO);

        planoDeDoses.gerarDoses(tratamento);

        return tratamentoMapper.toDTO(tratamentoRepository.save(tratamento), LocalDateTime.now());
    }

    /** Tela principal do tutor: o que precisa ser dado hoje, somando todos os pets. */
    @Transactional(readOnly = true)
    public List<DoseTratamentoDTO> dosesDeHoje() {
        Responsavel responsavel = usuarioLogado.comoResponsavel();
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();

        List<DoseTratamento> doses = tratamentoRepository
                .findAtivosDoResponsavel(responsavel.getId(), StatusTratamento.EM_ANDAMENTO)
                .stream()
                .flatMap(tratamento -> tratamento.getDoses().stream())
                .filter(dose -> dose.getHorarioPrevisto().toLocalDate().equals(hoje))
                .sorted(Comparator.comparing(DoseTratamento::getHorarioPrevisto))
                .toList();

        return tratamentoMapper.toDosesDTO(doses, agora);
    }

    @Transactional
    public DoseTratamentoDTO confirmarDose(Long doseId) {
        DoseTratamento dose = doseRepository.findById(doseId)
                .orElseThrow(() -> new ResourceNotFoundException("Dose nao encontrada com id: " + doseId));

        Tratamento tratamento = dose.getTratamento();
        exigirTutorDoTratamento(tratamento);

        if (!tratamento.getStatus().estaAtivo()) {
            throw new BusinessException("Este tratamento esta " + tratamento.getStatus().getDescricao().toLowerCase());
        }

        LocalDateTime agora = LocalDateTime.now();
        dose.confirmar(agora);

        return tratamentoMapper.toDoseDTO(doseRepository.save(dose), agora);
    }

    @Transactional
    public TratamentoResponseDTO encerrar(Long id, EncerramentoTratamentoDTO dto) {
        Tratamento tratamento = buscarDoVeterinarioLogado(id);
        tratamento.encerrar(dto.motivo());
        return tratamentoMapper.toDTO(tratamentoRepository.save(tratamento), LocalDateTime.now());
    }

    @Transactional
    public TratamentoResponseDTO interromper(Long id, EncerramentoTratamentoDTO dto) {
        Tratamento tratamento = buscarDoVeterinarioLogado(id);
        tratamento.interromper(dto.motivo());
        return tratamentoMapper.toDTO(tratamentoRepository.save(tratamento), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public TratamentoResponseDTO buscarPorId(Long id) {
        return tratamentoMapper.toDTO(buscarComAcesso(id), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<TratamentoResponseDTO> historicoDoPet(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        LocalDateTime agora = LocalDateTime.now();

        return tratamentoRepository.findByPetIdOrderByInicioEmDesc(pet.getId())
                .stream()
                .map(tratamento -> tratamentoMapper.toDTO(tratamento, agora))
                .toList();
    }

    /** Painel da clinica: quem esta esquecendo de medicar. */
    @Transactional(readOnly = true)
    public List<TratamentoResponseDTO> aderenciaBaixa() {
        Veterinario veterinario = usuarioLogado.comoVeterinario();
        LocalDateTime agora = LocalDateTime.now();

        return tratamentoRepository.findAtivosDoVeterinario(veterinario.getId(), StatusTratamento.EM_ANDAMENTO)
                .stream()
                .map(tratamento -> tratamentoMapper.toDTO(tratamento, agora))
                .filter(dto -> dto.dosesPerdidas() > 0 && dto.aderenciaPercentual() < limiteDeAderenciaBaixa)
                .sorted(Comparator.comparingInt(TratamentoResponseDTO::aderenciaPercentual))
                .toList();
    }

    /** Usado pela triagem para sinalizar possivel reacao ao medicamento em uso. */
    @Transactional(readOnly = true)
    public List<String> medicamentosEmUso(Long petId) {
        return tratamentoRepository.findByPetIdAndStatus(petId, StatusTratamento.EM_ANDAMENTO)
                .stream()
                .map(Tratamento::getMedicamento)
                .toList();
    }

    private Consulta consultaDeOrigem(Long consultaId, Pet pet, Veterinario veterinario) {
        if (consultaId == null) {
            return null;
        }
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada com id: " + consultaId));

        if (!consulta.getPet().getId().equals(pet.getId())) {
            throw new BusinessException("A consulta informada e de outro pet");
        }
        if (!consulta.ehDoVeterinario(veterinario)) {
            throw new AcessoNegadoException("Esta consulta foi atendida por outro veterinario");
        }
        if (consulta.getStatus() != StatusConsulta.CONCLUIDA) {
            throw new BusinessException("So e possivel prescrever a partir de uma consulta concluida");
        }
        return consulta;
    }

    private void exigirMedicamentoDisponivel(Pet pet, String medicamento) {
        boolean jaEmUso = tratamentoRepository.existsByPetIdAndMedicamentoIgnoreCaseAndStatus(
                pet.getId(), medicamento, StatusTratamento.EM_ANDAMENTO);

        if (jaEmUso) {
            throw new BusinessException("%s ja possui um tratamento em andamento com %s"
                    .formatted(pet.getNome(), medicamento));
        }
    }

    private void exigirTutorDoTratamento(Tratamento tratamento) {
        Responsavel responsavel = usuarioLogado.comoResponsavel();
        if (!tratamento.pertenceA(responsavel)) {
            throw new AcessoNegadoException("Este tratamento e de um pet de outro tutor");
        }
    }

    private Tratamento buscarDoVeterinarioLogado(Long id) {
        Tratamento tratamento = buscarEntidade(id);
        if (!tratamento.getVeterinario().getId().equals(usuarioLogado.comoVeterinario().getId())) {
            throw new AcessoNegadoException("Este tratamento foi prescrito por outro veterinario");
        }
        return tratamento;
    }

    private Tratamento buscarComAcesso(Long id) {
        Tratamento tratamento = buscarEntidade(id);
        petService.buscarEntidadePermitida(tratamento.getPet().getId());
        return tratamento;
    }

    private Tratamento buscarEntidade(Long id) {
        return tratamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tratamento nao encontrado com id: " + id));
    }
}
