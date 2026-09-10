package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Justificativa do encerramento ou da interrupcao do tratamento")
public record EncerramentoTratamentoDTO(
        @Schema(description = "Motivo ou observacao final",
                example = "Quadro resolvido, sem necessidade de continuar")
        @NotBlank(message = "Informe o motivo")
        @Size(max = 255, message = "Motivo deve ter no maximo 255 caracteres")
        String motivo
) {
}
