package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.model.AplicacaoVacina;
import org.fiap.challenge_clyvo.model.SituacaoVacina;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Traduz "ultima dose aplicada" em uma situacao legivel. Isolado porque a carteira do
 * tutor, o painel da clinica e a resposta da API precisam exatamente do mesmo criterio.
 */
@Component
public class AvaliadorSituacaoVacina {
    private final int diasDeAlerta;

    public AvaliadorSituacaoVacina(@Value("${app.vacinacao.dias-alerta-vencimento}") int diasDeAlerta) {
        this.diasDeAlerta = diasDeAlerta;
    }

    public SituacaoVacina avaliar(AplicacaoVacina ultimaDose, LocalDate hoje) {
        if (ultimaDose == null) {
            return SituacaoVacina.NAO_INICIADA;
        }
        LocalDate vencimento = ultimaDose.getProximaDose();
        if (vencimento == null) {
            return SituacaoVacina.CONCLUIDA;
        }
        if (vencimento.isBefore(hoje)) {
            return SituacaoVacina.ATRASADA;
        }
        return vencimento.isAfter(hoje.plusDays(diasDeAlerta)) ? SituacaoVacina.EM_DIA : SituacaoVacina.A_VENCER;
    }

    public long diasEmAtraso(AplicacaoVacina ultimaDose, LocalDate hoje) {
        if (ultimaDose == null || ultimaDose.getProximaDose() == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(ultimaDose.getProximaDose(), hoje));
    }

    public int getDiasDeAlerta() {
        return diasDeAlerta;
    }
}
