package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Solicitacao de agendamento de consulta")
public record ConsultaRequestDTO(
        @Schema(description = "ID do pet que sera atendido", example = "1")
        @NotNull(message = "ID do pet e obrigatorio")
        Long petId,

        @Schema(description = "ID do veterinario desejado", example = "1")
        @NotNull(message = "ID do veterinario e obrigatorio")
        Long veterinarioId,

        @Schema(description = "Data e hora desejadas", example = "2026-09-15T14:30:00")
        @NotNull(message = "Data e hora sao obrigatorias")
        @Future(message = "So e possivel agendar para uma data futura")
        LocalDateTime dataHora,

        @Schema(description = "Motivo da consulta", example = "Coceira e queda de pelo ha duas semanas")
        @NotBlank(message = "Motivo e obrigatorio")
        @Size(max = 255, message = "Motivo deve ter no maximo 255 caracteres")
        String motivo,

        @Schema(description = "Triagem que originou o agendamento. Opcional: preenchido quando o tutor "
                + "aceita a recomendacao da triagem, e faz a consulta nascer com o relato anexado.",
                example = "7")
        Long triagemId
) {
    public boolean veioDeTriagem() {
        return triagemId != null;
    }
}
