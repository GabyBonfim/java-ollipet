package org.fiap.challenge_clyvo.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados possiveis de uma consulta e as transicoes permitidas entre eles.
 * Concentrar o grafo aqui impede que cada servico invente sua propria regra de fluxo.
 */
public enum StatusConsulta {
    SOLICITADA("Solicitada"),
    CONFIRMADA("Confirmada"),
    EM_ATENDIMENTO("Em atendimento"),
    CONCLUIDA("Concluida"),
    CANCELADA("Cancelada"),
    NAO_COMPARECEU("Nao compareceu");

    private static final Map<StatusConsulta, Set<StatusConsulta>> TRANSICOES = new EnumMap<>(StatusConsulta.class);
    private static final Set<StatusConsulta> OCUPAM_AGENDA = EnumSet.of(SOLICITADA, CONFIRMADA, EM_ATENDIMENTO);

    static {
        TRANSICOES.put(SOLICITADA, EnumSet.of(CONFIRMADA, CANCELADA));
        TRANSICOES.put(CONFIRMADA, EnumSet.of(EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU));
        TRANSICOES.put(EM_ATENDIMENTO, EnumSet.of(CONCLUIDA));
        TRANSICOES.put(CONCLUIDA, EnumSet.noneOf(StatusConsulta.class));
        TRANSICOES.put(CANCELADA, EnumSet.noneOf(StatusConsulta.class));
        TRANSICOES.put(NAO_COMPARECEU, EnumSet.noneOf(StatusConsulta.class));
    }

    private final String descricao;

    StatusConsulta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean permiteTransicaoPara(StatusConsulta destino) {
        return TRANSICOES.get(this).contains(destino);
    }

    public Set<StatusConsulta> proximosPossiveis() {
        return Collections.unmodifiableSet(TRANSICOES.get(this));
    }

    public boolean isFinal() {
        return TRANSICOES.get(this).isEmpty();
    }

    /** Consultas nestes estados bloqueiam o horario do veterinario. */
    public boolean ocupaAgenda() {
        return OCUPAM_AGENDA.contains(this);
    }

    public static Set<StatusConsulta> statusQueOcupamAgenda() {
        return Collections.unmodifiableSet(OCUPAM_AGENDA);
    }
}
