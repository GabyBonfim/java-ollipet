package org.fiap.challenge_clyvo.model;

public enum SituacaoVacina {
    NAO_INICIADA("Nao iniciada"),
    EM_DIA("Em dia"),
    A_VENCER("A vencer"),
    ATRASADA("Atrasada"),
    CONCLUIDA("Protocolo concluido");

    private final String descricao;

    SituacaoVacina(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean exigeAtencao() {
        return this == A_VENCER || this == ATRASADA || this == NAO_INICIADA;
    }
}
