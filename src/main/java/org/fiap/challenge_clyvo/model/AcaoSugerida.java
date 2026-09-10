package org.fiap.challenge_clyvo.model;

/** O que o aplicativo deve oferecer ao tutor depois do resultado da triagem. */
public enum AcaoSugerida {
    IR_AGORA("Procure a clinica imediatamente"),
    AGENDAR("Agende uma consulta"),
    AGENDAR_RETORNO("Agende um retorno da consulta recente"),
    ACOMPANHAR("Acompanhe em casa e refaca a triagem se piorar");

    private final String descricao;

    AcaoSugerida(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Emergencia nao agenda: manda ir agora. Acompanhamento em casa tambem nao gera consulta. */
    public boolean geraAgendamento() {
        return this == AGENDAR || this == AGENDAR_RETORNO;
    }
}
