package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Justificativa do cancelamento")
public record CancelamentoConsultaDTO(
        @Schema(description = "Motivo do cancelamento", example = "Imprevisto, vou remarcar")
        @NotBlank(message = "Motivo do cancelamento e obrigatorio")
        @Size(max = 255, message = "Motivo deve ter no maximo 255 caracteres")
        String motivo
) {
}
