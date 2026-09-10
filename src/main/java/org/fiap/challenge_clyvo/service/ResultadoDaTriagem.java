package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.model.AcaoSugerida;
import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;

import java.util.List;

public record ResultadoDaTriagem(
        int pontuacao,
        ClassificacaoTriagem classificacao,
        AcaoSugerida acaoSugerida,
        List<String> observacoes
) {
}
