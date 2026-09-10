package org.fiap.challenge_clyvo.model;

public enum StatusTratamento {
    EM_ANDAMENTO("Em andamento"),
    /** Chegou ao fim do plano de doses. */
    CONCLUIDO("Concluido"),
    /** Suspenso pela clinica antes do previsto. */
    INTERROMPIDO("Interrompido");

    private final String descricao;

    StatusTratamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean estaAtivo() {
        return this == EM_ANDAMENTO;
    }
}
