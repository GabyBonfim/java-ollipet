package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Respostas do questionario de triagem")
public record TriagemRequestDTO(
        @Schema(description = "ID do pet avaliado", example = "1")
        @NotNull(message = "ID do pet e obrigatorio")
        Long petId,

        @Schema(description = "ID da queixa escolhida", example = "1")
        @NotNull(message = "ID da queixa e obrigatorio")
        Long queixaId,

        @Schema(description = "Uma entrada por pergunta respondida")
        @NotEmpty(message = "Informe ao menos uma resposta")
        @Valid
        List<RespostaInformadaDTO> respostas
) {
}
