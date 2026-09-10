package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.dto.AplicacaoVacinaRequestDTO;
import org.fiap.challenge_clyvo.dto.AplicacaoVacinaResponseDTO;
import org.fiap.challenge_clyvo.dto.CarteiraVacinacaoDTO;
import org.fiap.challenge_clyvo.dto.ItemCarteiraDTO;
import org.fiap.challenge_clyvo.dto.PendenciaVacinaDTO;
import org.fiap.challenge_clyvo.dto.VacinaDTO;
import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.exception.ResourceNotFoundException;
import org.fiap.challenge_clyvo.mapper.VacinacaoMapper;
import org.fiap.challenge_clyvo.model.AplicacaoVacina;
import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Vacina;
import org.fiap.challenge_clyvo.repository.AplicacaoVacinaRepository;
import org.fiap.challenge_clyvo.repository.VacinaRepository;
import org.fiap.challenge_clyvo.security.UsuarioLogado;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fluxo 2 — carteira de vacinacao.
 *
 * Cada dose registrada calcula sozinha quando a proxima vence, seguindo o protocolo da
 * vacina: dentro da serie inicial vale o intervalo em dias, depois dela vale o reforco.
 * Dai saem a carteira do tutor e o painel de pendencias da clinica, sem recalculo manual.
 */
@Service
public class VacinacaoService {
    private final AplicacaoVacinaRepository aplicacaoRepository;
    private final VacinaRepository vacinaRepository;
    private final PetService petService;
    private final VacinacaoMapper vacinacaoMapper;
    private final AvaliadorSituacaoVacina avaliador;
    private final UsuarioLogado usuarioLogado;

    public VacinacaoService(AplicacaoVacinaRepository aplicacaoRepository,
                            VacinaRepository vacinaRepository,
                            PetService petService,
                            VacinacaoMapper vacinacaoMapper,
                            AvaliadorSituacaoVacina avaliador,
                            UsuarioLogado usuarioLogado) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.vacinaRepository = vacinaRepository;
        this.petService = petService;
        this.vacinacaoMapper = vacinacaoMapper;
        this.avaliador = avaliador;
        this.usuarioLogado = usuarioLogado;
    }

    @Transactional
    public AplicacaoVacinaResponseDTO registrarAplicacao(Long petId, AplicacaoVacinaRequestDTO dto) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        Vacina vacina = buscarVacina(dto.vacinaId());

        exigirEspecieCompativel(pet, vacina);

        AplicacaoVacina anterior = aplicacaoRepository.findUltimaDose(pet.getId(), vacina.getId()).orElse(null);
        int numeroDose = proximaDoseDepoisDe(anterior, vacina, dto.dataAplicacao());

        AplicacaoVacina aplicacao = new AplicacaoVacina();
        aplicacao.setPet(pet);
        aplicacao.setVacina(vacina);
        aplicacao.setVeterinario(usuarioLogado.comoVeterinario());
        aplicacao.setNumeroDose(numeroDose);
        aplicacao.setDataAplicacao(dto.dataAplicacao());
        aplicacao.setProximaDose(vacina.calcularVencimentoAposDose(numeroDose, dto.dataAplicacao()));
        aplicacao.setLote(dto.lote());

        return vacinacaoMapper.toDTO(aplicacaoRepository.save(aplicacao));
    }

    @Transactional(readOnly = true)
    public CarteiraVacinacaoDTO carteiraDoPet(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        LocalDate hoje = LocalDate.now();

        Map<Long, AplicacaoVacina> ultimaPorVacina = ultimaDosePorVacina(pet.getId());

        List<ItemCarteiraDTO> itens = vacinaRepository.findByEspecieOrderByNomeAsc(pet.getEspecie())
                .stream()
                .map(vacina -> vacinacaoMapper.toItemCarteira(vacina, ultimaPorVacina.get(vacina.getId()), hoje))
                .toList();

        long pendencias = itens.stream().filter(item -> item.situacao().exigeAtencao()).count();

        return new CarteiraVacinacaoDTO(pet.getId(), pet.getNome(), pet.getEspecie(), pendencias, itens);
    }

    @Transactional(readOnly = true)
    public List<AplicacaoVacinaResponseDTO> historicoDoPet(Long petId) {
        Pet pet = petService.buscarEntidadePermitida(petId);
        return aplicacaoRepository.findByPetIdOrderByDataAplicacaoDesc(pet.getId())
                .stream()
                .map(vacinacaoMapper::toDTO)
                .toList();
    }

    /** Painel da clinica: doses vencidas ou a vencer dentro da janela de alerta. */
    @Transactional(readOnly = true)
    public List<PendenciaVacinaDTO> pendencias() {
        LocalDate hoje = LocalDate.now();
        return aplicacaoRepository.findDosesPendentesAte(hoje.plusDays(avaliador.getDiasDeAlerta()))
                .stream()
                .map(aplicacao -> vacinacaoMapper.toPendencia(aplicacao, hoje))
                .filter(pendencia -> pendencia.situacao().exigeAtencao())
                .sorted(Comparator.comparing(PendenciaVacinaDTO::proximaDose))
                .toList();
    }

    /** O catalogo e o mesmo para todo mundo e muda muito pouco, entao vale cache. */
    @Cacheable(value = "catalogoVacinas", key = "#especie")
    @Transactional(readOnly = true)
    public List<VacinaDTO> catalogoPara(Especie especie) {
        return vacinaRepository.findByEspecieOrderByNomeAsc(especie)
                .stream()
                .map(vacina -> new VacinaDTO(vacina.getId(), vacina.getNome(), vacina.getEspecie(),
                        vacina.getDosesProtocolo(), vacina.getIntervaloDias(), vacina.getMesesReforco()))
                .toList();
    }

    private Map<Long, AplicacaoVacina> ultimaDosePorVacina(Long petId) {
        return aplicacaoRepository.findByPetIdOrderByDataAplicacaoDesc(petId)
                .stream()
                .collect(Collectors.toMap(
                        aplicacao -> aplicacao.getVacina().getId(),
                        Function.identity(),
                        (uma, outra) -> uma.getNumeroDose() >= outra.getNumeroDose() ? uma : outra));
    }

    private int proximaDoseDepoisDe(AplicacaoVacina anterior, Vacina vacina, LocalDate dataAplicacao) {
        if (anterior == null) {
            return 1;
        }
        if (dataAplicacao.isBefore(anterior.getDataAplicacao())) {
            throw new BusinessException("A data informada e anterior a ultima dose aplicada em "
                    + anterior.getDataAplicacao());
        }
        if (anterior.getProximaDose() == null) {
            throw new BusinessException("O protocolo da vacina %s ja foi concluido para este pet"
                    .formatted(vacina.getNome()));
        }
        if (dataAplicacao.isBefore(anterior.getProximaDose())) {
            throw new BusinessException("A proxima dose de %s so pode ser aplicada a partir de %s"
                    .formatted(vacina.getNome(), anterior.getProximaDose()));
        }
        return anterior.getNumeroDose() + 1;
    }

    private void exigirEspecieCompativel(Pet pet, Vacina vacina) {
        if (pet.getEspecie() != vacina.getEspecie()) {
            throw new BusinessException("A vacina %s e destinada a especie %s e nao pode ser aplicada em %s"
                    .formatted(vacina.getNome(), vacina.getEspecie(), pet.getNome()));
        }
    }

    private Vacina buscarVacina(Long id) {
        return vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina nao encontrada com id: " + id));
    }
}
