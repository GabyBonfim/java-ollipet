package org.fiap.challenge_clyvo.model;

/**
 * Resultado da triagem. Cada faixa ja carrega o prazo recomendado e a acao que o
 * aplicativo deve oferecer, para que a tela nao precise decidir isso por conta propria.
 */
public enum ClassificacaoTriagem {
    EMERGENCIA("Emergencia", "imediato", AcaoSugerida.IR_AGORA),
    URGENTE("Urgente", "24 horas", AcaoSugerida.AGENDAR),
    POUCO_URGENTE("Pouco urgente", "3 a 5 dias", AcaoSugerida.AGENDAR),
    ORIENTACAO("Orientacao", null, AcaoSugerida.ACOMPANHAR);

    private final String descricao;
    private final String prazoRecomendado;
    private final AcaoSugerida acaoSugerida;

    ClassificacaoTriagem(String descricao, String prazoRecomendado, AcaoSugerida acaoSugerida) {
        this.descricao = descricao;
        this.prazoRecomendado = prazoRecomendado;
        this.acaoSugerida = acaoSugerida;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getPrazoRecomendado() {
        return prazoRecomendado;
    }

    public AcaoSugerida getAcaoSugerida() {
        return acaoSugerida;
    }

    public boolean permiteAgendamento() {
        return acaoSugerida == AcaoSugerida.AGENDAR;
    }

    /** Casos que a clinica precisa enxergar sem depender de o tutor agendar. */
    public boolean exigeAtencaoDaClinica() {
        return this == EMERGENCIA || this == URGENTE;
    }
}
