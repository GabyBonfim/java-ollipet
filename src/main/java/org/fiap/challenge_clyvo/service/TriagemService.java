package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.ItemCarteiraDTO;
import org.fiap.challenge_clyvo.dto.ProtocoloTriagemDTO;
import org.fiap.challenge_clyvo.dto.QueixaResumoDTO;
import org.fiap.challenge_clyvo.dto.RespostaInformadaDTO;
import org.fiap.challenge_clyvo.dto.TriagemFilaDTO;
import org.fiap.challenge_clyvo.dto.TriagemRequestDTO;
import org.fiap.challenge_clyvo.dto.TriagemResponseDTO;
import org.fiap.challenge_clyvo.exception.AcessoNegadoException;
import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.exception.LimiteExcedidoException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.TriagemMapper;
import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.OpcaoResposta;
import org.fiap.challenge_clyvo.model.OrientacaoTriagem;
import org.fiap.challenge_clyvo.model.PerguntaTriagem;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Queixa;
import org.fiap.challenge_clyvo.model.SituacaoVacina;
import org.fiap.challenge_clyvo.model.StatusConsulta;
import org.fiap.challenge_clyvo.model.StatusTriagem;
import org.fiap.challenge_clyvo.model.Triagem;
import org.fiap.challenge_clyvo.repository.ConsultaRepository;
import org.fiap.challenge_clyvo.repository.OrientacaoTriagemRepository;
import org.fiap.challenge_clyvo.repository.PerguntaTriagemRepository;
import org.fiap.challenge_clyvo.repository.QueixaRepository;
import org.fiap.challenge_clyvo.repository.TriagemRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fluxo 3 — triagem por questionario.
 *
 * O tutor responde um protocolo fechado; a API pontua, cruza com o cadastro do pet e
 * devolve uma classificacao de urgencia. Quando o resultado indica consulta, a triagem
 * alimenta o Fluxo 1: o agendamento nasce com o motivo ja preenchido e fica anexado
 * ao que o tutor relatou.
 */
@Service
public class TriagemService {
    private final TriagemRepository triagemRepository;
    private final QueixaRepository queixaRepository;
    private final PerguntaTriagemRepository perguntaRepository;
    private final OrientacaoTriagemRepository orientacaoRepository;
    private final ConsultaRepository consultaRepository;
    private final PetService petService;
    private final VacinacaoService vacinacaoService;
    private final TratamentoService tratamentoService;
    private final AvaliadorDeTriagem avaliador;
    private final TriagemMapper triagemMapper;
    private final UsuarioLogado usuarioLogado;

    private final int validadeEmHoras;
    private final int limitePorPet;
    private final int diasDeConsultaRecente;

    public TriagemService(TriagemRepository triagemRepository,
                          QueixaRepository queixaRepository,
                          PerguntaTriagemRepository perguntaRepository,
                          OrientacaoTriagemRepository orientacaoRepository,
                          ConsultaRepository consultaRepository,
                          PetService petService,
                          VacinacaoService vacinacaoService,
                          TratamentoService tratamentoService,
                          AvaliadorDeTriagem avaliador,
                          TriagemMapper triagemMapper,
                          UsuarioLogado usuarioLogado,
                          @Value("${app.triagem.validade-horas}") int validadeEmHoras,
                          @Value("${app.triagem.limite-por-pet-em-24h}") int limitePorPet,
                          @Value("${app.triagem.dias-consulta-recente}") int diasDeConsultaRecente) {
        this.triagemRepository = triagemRepository;
        this.queixaRepository = queixaRepository;
        this.perguntaRepository = perguntaRepository;
        this.orientacaoRepository = orientacaoRepository;
        this.consultaRepository = consultaRepository;
        this.petService = petService;
        this.vacinacaoService = vacinacaoService;
        this.tratamentoService = tratamentoService;
        this.avaliador = avaliador;
        this.triagemMapper = triagemMapper;
        this.usuarioLogado = usuarioLogado;
        this.validadeEmHoras = validadeEmHoras;
        this.limitePorPet = limitePorPet;
        this.diasDeConsultaRecente = diasDeConsultaRecente;
    }

    @Transactional(readOnly = true)
    public List<QueixaResumoDTO> queixasPara(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        return queixaRepository.findAplicaveisA(pet.getEspecie())
                .stream()
                .map(triagemMapper::toResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProtocoloTriagemDTO protocoloDe(Long queixaId) {
        Queixa queixa = buscarQueixa(queixaId);
        return triagemMapper.toProtocolo(queixa, perguntaRepository.findComOpcoesPorQueixa(queixaId));
    }

    @Transactional
    public TriagemResponseDTO registrar(TriagemRequestDTO dto) {
        Pet pet = petService.buscarEntidadePermitida(dto.petId());
        Queixa queixa = buscarQueixa(dto.queixaId());

        exigirLimiteDisponivel(pet);
        exigirQueixaCompativel(queixa, pet.getEspecie());

        List<PerguntaTriagem> protocolo = perguntaRepository.findComOpcoesPorQueixa(queixa.getId());
        List<OpcaoResposta> escolhas = validarRespostas(dto.respostas(), protocolo);

        ResultadoDaTriagem resultado = avaliador.avaliar(pet, queixa, escolhas, montarContexto(pet));

        Triagem triagem = montarTriagem(pet, queixa, resultado);
        vincularRespostas(triagem, dto.respostas(), protocolo, escolhas);
        resultado.observacoes().forEach(triagem::adicionarObservacao);

        Triagem salva = triagemRepository.save(triagem);
        return triagemMapper.toResponse(salva, orientacoesDe(salva));
    }

    /**
     * Refaz uma triagem ja registrada com novas respostas.
     *
     * Serve para quando o tutor percebe que respondeu algo errado ou o quadro do
     * animal mudou. A triagem e reavaliada do zero pelo mesmo algoritmo, entao a
     * classificacao pode subir ou descer.
     *
     * Nao altera triagem que ja virou consulta: naquele ponto o relato ja foi
     * anexado ao agendamento e mudar a origem tornaria o historico inconsistente.
     * Tambem nao consome o limite diario, por nao ser uma triagem nova.
     */
    @Transactional
    public TriagemResponseDTO refazer(Long id, TriagemRequestDTO dto) {
        Triagem triagem = buscarComAcesso(id);

        if (triagem.getStatus() == StatusTriagem.ENCAMINHADA) {
            throw new BusinessException(
                    "Esta triagem ja originou uma consulta e nao pode mais ser alterada");
        }

        Pet pet = petService.buscarEntidadePermitida(dto.petId());

        if (!pet.getId().equals(triagem.getPet().getId())) {
            throw new BusinessException("Nao e possivel trocar o pet de uma triagem ja registrada");
        }

        Queixa queixa = buscarQueixa(dto.queixaId());
        exigirQueixaCompativel(queixa, pet.getEspecie());

        List<PerguntaTriagem> protocolo = perguntaRepository.findComOpcoesPorQueixa(queixa.getId());
        List<OpcaoResposta> escolhas = validarRespostas(dto.respostas(), protocolo);

        ResultadoDaTriagem resultado = avaliador.avaliar(pet, queixa, escolhas, montarContexto(pet));

        // Substitui o conteudo anterior: as respostas e observacoes antigas nao
        // valem mais, e a nova avaliacao passa a responder pelo registro.
        //
        // O flush entre a limpeza e a insercao e necessario: sem ele o Hibernate
        // insere as respostas novas antes de remover as antigas e esbarra na
        // chave unica (id_triagem, id_pergunta).
        triagem.getRespostas().clear();
        triagem.getObservacoes().clear();
        triagemRepository.flush();

        triagem.setQueixa(queixa);
        triagem.setPontuacao(resultado.pontuacao());
        triagem.setClassificacao(resultado.classificacao());
        triagem.setAcaoSugerida(resultado.acaoSugerida());

        vincularRespostas(triagem, dto.respostas(), protocolo, escolhas);
        resultado.observacoes().forEach(triagem::adicionarObservacao);

        Triagem salva = triagemRepository.save(triagem);
        return triagemMapper.toResponse(salva, orientacoesDe(salva));
    }

    /**
     * Exclui uma triagem do historico do tutor.
     *
     * Uma triagem ja encaminhada a consulta permanece: ela e a justificativa
     * clinica daquele agendamento.
     */
    @Transactional
    public void excluir(Long id) {
        Triagem triagem = buscarComAcesso(id);

        if (triagem.getStatus() == StatusTriagem.ENCAMINHADA) {
            throw new BusinessException(
                    "Esta triagem originou uma consulta e por isso nao pode ser excluida");
        }

        triagemRepository.delete(triagem);
    }

    @Transactional(readOnly = true)
    public TriagemResponseDTO buscarPorId(Long id) {
        Triagem triagem = buscarComAcesso(id);
        return triagemMapper.toResponse(triagem, orientacoesDe(triagem));
    }

    @Transactional(readOnly = true)
    public Page<TriagemResponseDTO> minhasTriagens(Pageable pageable) {
        Long responsavelId = usuarioLogado.comoResponsavel().getId();
        return triagemRepository.findByPetResponsavelIdOrderByCriadoEmDesc(responsavelId, pageable)
                .map(triagem -> triagemMapper.toResponse(triagem, orientacoesDe(triagem)));
    }

    @Transactional(readOnly = true)
    public List<TriagemFilaDTO> filaDaClinica() {
        return triagemRepository.findFilaDeAtendimento(StatusTriagem.CLASSIFICADA, LocalDateTime.now())
                .stream()
                .map(triagemMapper::toFila)
                .toList();
    }

    /**
     * Chamado pelo agendamento quando o tutor aceita a recomendacao. Roda dentro da
     * transacao da consulta, entao ou os dois nascem ligados ou nenhum e gravado.
     */
    @Transactional
    public Triagem vincularAConsulta(Long triagemId, Consulta consulta) {
        Triagem triagem = buscarComAcesso(triagemId);

        if (!triagem.getPet().getId().equals(consulta.getPet().getId())) {
            throw new BusinessException("A triagem informada e de outro pet");
        }
        if (triagem.getStatus() == StatusTriagem.ENCAMINHADA) {
            throw new BusinessException("Esta triagem ja gerou a consulta #" + triagem.getConsulta().getId());
        }
        if (triagem.estaExpirada()) {
            throw new BusinessException("Esta triagem expirou em %s. Refaca a triagem antes de agendar."
                    .formatted(triagem.getExpiraEm()));
        }
        if (!triagem.getAcaoSugerida().geraAgendamento()) {
            throw new BusinessException("Esta triagem nao indicou agendamento: "
                    + triagem.getAcaoSugerida().getDescricao());
        }

        triagem.vincularA(consulta);
        return triagemRepository.save(triagem);
    }

    /** Texto pronto para o campo motivo, usado quando o tutor agenda a partir da triagem. */
    @Transactional(readOnly = true)
    public String motivoSugeridoDe(Long triagemId) {
        return triagemMapper.montarMotivoSugerido(buscarComAcesso(triagemId));
    }

    private Triagem montarTriagem(Pet pet, Queixa queixa, ResultadoDaTriagem resultado) {
        Triagem triagem = new Triagem();
        triagem.setPet(pet);
        triagem.setQueixa(queixa);
        triagem.setPontuacao(resultado.pontuacao());
        triagem.setClassificacao(resultado.classificacao());
        triagem.setAcaoSugerida(resultado.acaoSugerida());
        triagem.setStatus(StatusTriagem.CLASSIFICADA);
        triagem.setExpiraEm(LocalDateTime.now().plusHours(validadeEmHoras));
        return triagem;
    }

    private void vincularRespostas(Triagem triagem, List<RespostaInformadaDTO> informadas,
                                   List<PerguntaTriagem> protocolo, List<OpcaoResposta> escolhas) {
        for (int i = 0; i < informadas.size(); i++) {
            PerguntaTriagem pergunta = localizarPergunta(protocolo, informadas.get(i).perguntaId());
            triagem.adicionarResposta(pergunta, escolhas.get(i));
        }
    }

    /**
     * Confere que cada resposta existe, pertence ao protocolo da queixa e que nenhuma
     * pergunta obrigatoria exigivel ficou de fora. Devolve as opcoes na mesma ordem em
     * que foram informadas.
     */
    private List<OpcaoResposta> validarRespostas(List<RespostaInformadaDTO> informadas,
                                                 List<PerguntaTriagem> protocolo) {
        Set<Long> perguntasRespondidas = new HashSet<>();
        List<OpcaoResposta> escolhas = new ArrayList<>();

        for (RespostaInformadaDTO informada : informadas) {
            if (!perguntasRespondidas.add(informada.perguntaId())) {
                throw new BusinessException("A pergunta " + informada.perguntaId() + " foi respondida duas vezes");
            }
            PerguntaTriagem pergunta = localizarPergunta(protocolo, informada.perguntaId());
            escolhas.add(localizarOpcao(pergunta, informada.opcaoId()));
        }

        exigirObrigatoriasRespondidas(protocolo, escolhas, perguntasRespondidas);
        return escolhas;
    }

    private void exigirObrigatoriasRespondidas(List<PerguntaTriagem> protocolo, List<OpcaoResposta> escolhas,
                                               Set<Long> perguntasRespondidas) {
        List<Long> opcoesEscolhidas = escolhas.stream().map(OpcaoResposta::getId).toList();

        List<String> faltantes = protocolo.stream()
                .filter(pergunta -> pergunta.exigidaCom(opcoesEscolhidas))
                .filter(pergunta -> !perguntasRespondidas.contains(pergunta.getId()))
                .map(pergunta -> "%d (%s)".formatted(pergunta.getId(), pergunta.getTexto()))
                .toList();

        if (!faltantes.isEmpty()) {
            throw new BusinessException("Responda todas as perguntas obrigatorias. Faltaram: "
                    + String.join("; ", faltantes));
        }
    }

    private PerguntaTriagem localizarPergunta(List<PerguntaTriagem> protocolo, Long perguntaId) {
        return protocolo.stream()
                .filter(pergunta -> pergunta.getId().equals(perguntaId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "A pergunta " + perguntaId + " nao pertence ao protocolo desta queixa"));
    }

    private OpcaoResposta localizarOpcao(PerguntaTriagem pergunta, Long opcaoId) {
        return pergunta.getOpcoes().stream()
                .filter(opcao -> opcao.getId().equals(opcaoId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("A opcao %d nao pertence a pergunta %d"
                        .formatted(opcaoId, pergunta.getId())));
    }

    private ContextoClinico montarContexto(Pet pet) {
        List<String> vacinasAtrasadas = vacinacaoService.carteiraDoPet(pet.getId()).itens().stream()
                .filter(item -> item.situacao() == SituacaoVacina.ATRASADA)
                .map(this::descreverAtraso)
                .toList();

        return new ContextoClinico(
                vacinasAtrasadas,
                tratamentoService.medicamentosEmUso(pet.getId()),
                possuiConsultaRecenteConcluida(pet));
    }

    private String descreverAtraso(ItemCarteiraDTO item) {
        return "%s atrasada ha %d dias".formatted(item.nomeVacina(), item.diasEmAtraso());
    }

    private boolean possuiConsultaRecenteConcluida(Pet pet) {
        LocalDateTime limite = LocalDateTime.now().minusDays(diasDeConsultaRecente);

        return consultaRepository.findByPetIdOrderByDataHoraDesc(pet.getId()).stream()
                .anyMatch(consulta -> consulta.getStatus() == StatusConsulta.CONCLUIDA
                        && consulta.getDataHora().isAfter(limite));
    }

    private List<String> orientacoesDe(Triagem triagem) {
        return orientacaoRepository
                .findByQueixaIdAndClassificacaoOrderByOrdemAsc(triagem.getQueixa().getId(),
                        triagem.getClassificacao())
                .stream()
                .map(OrientacaoTriagem::getTexto)
                .toList();
    }

    private void exigirLimiteDisponivel(Pet pet) {
        long ultimas24h = triagemRepository.countByPetIdAndCriadoEmAfter(pet.getId(),
                LocalDateTime.now().minusHours(24));

        if (ultimas24h >= limitePorPet) {
            throw new LimiteExcedidoException(
                    "Limite de %d triagens por pet em 24 horas atingido. Procure a clinica."
                            .formatted(limitePorPet));
        }
    }

    private void exigirQueixaCompativel(Queixa queixa, Especie especie) {
        if (!queixa.aplicaSeA(especie)) {
            throw new BusinessException("A queixa '%s' nao se aplica a especie %s"
                    .formatted(queixa.getNome(), especie));
        }
    }

    private Triagem buscarComAcesso(Long id) {
        Triagem triagem = triagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Triagem nao encontrada com id: " + id));

        // Reaproveita a checagem de posse do pet: quem pode ver o pet pode ver a triagem dele.
        try {
            petService.buscarEntidadePermitida(triagem.getPet().getId());
        } catch (AcessoNegadoException naoEDono) {
            throw new AcessoNegadoException("Esta triagem pertence a outro tutor");
        }
        return triagem;
    }

    private Queixa buscarQueixa(Long id) {
        return queixaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Queixa nao encontrada com id: " + id));
    }
}
