package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Peso e sinal de alerta ficam de fora de proposito: se o aplicativo conhecesse a
 * pontuacao, ele calcularia o resultado sozinho e a regra clinica deixaria de ser do
 * servidor.
 */
@Schema(description = "Opcao de resposta de uma pergunta da triagem")
public record OpcaoRespostaDTO(
        @Schema(description = "ID da opcao", example = "1012")
        Long id,

        @Schema(description = "Texto exibido", example = "1 a 2 dias")
        String texto,

        @Schema(description = "Ordem de exibicao", example = "2")
        Integer ordem
) {
}
