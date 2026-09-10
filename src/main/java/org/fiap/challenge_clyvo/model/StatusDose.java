package org.fiap.challenge_clyvo.model;

/**
 * Situacao de uma dose. Nao existe como coluna: e deduzida de {@code confirmadoEm} e do
 * horario previsto, entao uma dose vira PERDIDA sozinha quando a janela passa, sem
 * depender de nenhuma rotina agendada.
 */
public enum StatusDose {
    PENDENTE("Pendente"),
    ADMINISTRADA("Administrada"),
    PERDIDA("Perdida");

    private final String descricao;

    StatusDose(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
