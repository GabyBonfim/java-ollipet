package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Uma resposta escolhida pelo tutor")
public record RespostaInformadaDTO(
        @Schema(description = "ID da pergunta respondida", example = "101")
        @NotNull(message = "ID da pergunta e obrigatorio")
        Long perguntaId,

        @Schema(description = "ID da opcao escolhida", example = "1012")
        @NotNull(message = "ID da opcao e obrigatorio")
        Long opcaoId
) {
}
