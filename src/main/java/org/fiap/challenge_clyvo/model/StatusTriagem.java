package org.fiap.challenge_clyvo.model;

public enum StatusTriagem {
    /** Concluida pelo tutor, ainda nao virou consulta. */
    CLASSIFICADA("Classificada"),
    /** Ja originou uma consulta agendada. */
    ENCAMINHADA("Encaminhada");

    private final String descricao;

    StatusTriagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
