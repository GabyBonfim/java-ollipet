package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pergunta do protocolo de triagem")
public record PerguntaTriagemDTO(
        @Schema(description = "ID da pergunta", example = "101")
        Long id,

        @Schema(description = "Enunciado", example = "Ha quanto tempo comecou?")
        String texto,

        @Schema(description = "Ordem de exibicao", example = "1")
        Integer ordem,

        @Schema(description = "Se a resposta e obrigatoria", example = "true")
        boolean obrigatoria,

        @Schema(description = "Quando preenchido, exiba esta pergunta apenas se a opcao "
                + "indicada tiver sido escolhida. Ignorar o campo e seguro: a API nao "
                + "cobra resposta de pergunta cuja condicao nao foi satisfeita.",
                example = "1032")
        Long dependeDeOpcaoId,

        @Schema(description = "Opcoes de resposta, em ordem")
        List<OpcaoRespostaDTO> opcoes
) {
}
