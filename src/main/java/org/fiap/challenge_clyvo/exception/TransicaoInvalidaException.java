package org.fiap.challenge_clyvo.exception;

import org.fiap.challenge_clyvo.model.StatusConsulta;

public class TransicaoInvalidaException extends BusinessException {
    public TransicaoInvalidaException(StatusConsulta origem, StatusConsulta destino) {
        super("Nao e possivel mudar a consulta de '%s' para '%s'. A partir de '%s' os estados validos sao: %s"
                .formatted(origem.getDescricao(), destino.getDescricao(), origem.getDescricao(),
                        origem.isFinal() ? "nenhum (estado final)" : origem.proximosPossiveis()));
    }
}
