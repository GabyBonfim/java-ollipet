package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.AplicacaoVacinaResponseDTO;
import org.fiap.challenge_clyvo.dto.ItemCarteiraDTO;
import org.fiap.challenge_clyvo.dto.PendenciaVacinaDTO;
import org.fiap.challenge_clyvo.model.AplicacaoVacina;
import org.fiap.challenge_clyvo.model.Vacina;
import org.fiap.challenge_clyvo.service.AvaliadorSituacaoVacina;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VacinacaoMapper {
    private final AvaliadorSituacaoVacina avaliador;

    public VacinacaoMapper(AvaliadorSituacaoVacina avaliador) {
        this.avaliador = avaliador;
    }

    public AplicacaoVacinaResponseDTO toDTO(AplicacaoVacina aplicacao) {
        return new AplicacaoVacinaResponseDTO(
                aplicacao.getId(),
                aplicacao.getPet().getId(),
                aplicacao.getPet().getNome(),
                aplicacao.getVacina().getId(),
                aplicacao.getVacina().getNome(),
                aplicacao.getNumeroDose(),
                aplicacao.getVacina().getDosesProtocolo(),
                aplicacao.getDataAplicacao(),
                aplicacao.getProximaDose(),
                aplicacao.getLote(),
                aplicacao.getVeterinario().getNome()
        );
    }

    public ItemCarteiraDTO toItemCarteira(Vacina vacina, AplicacaoVacina ultimaDose, LocalDate hoje) {
        return new ItemCarteiraDTO(
                vacina.getId(),
                vacina.getNome(),
                ultimaDose == null ? 0 : ultimaDose.getNumeroDose(),
                vacina.getDosesProtocolo(),
                ultimaDose == null ? null : ultimaDose.getDataAplicacao(),
                ultimaDose == null ? null : ultimaDose.getProximaDose(),
                avaliador.diasEmAtraso(ultimaDose, hoje),
                avaliador.avaliar(ultimaDose, hoje)
        );
    }

    public PendenciaVacinaDTO toPendencia(AplicacaoVacina ultimaDose, LocalDate hoje) {
        return new PendenciaVacinaDTO(
                ultimaDose.getPet().getId(),
                ultimaDose.getPet().getNome(),
                ultimaDose.getPet().getResponsavel().getNome(),
                ultimaDose.getVacina().getNome(),
                ultimaDose.getNumeroDose() + 1,
                ultimaDose.getProximaDose(),
                avaliador.diasEmAtraso(ultimaDose, hoje),
                avaliador.avaliar(ultimaDose, hoje)
        );
    }
}
