package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Prescricao de um tratamento para casa")
public record TratamentoRequestDTO(
        @Schema(description = "ID do pet tratado", example = "1")
        @NotNull(message = "ID do pet e obrigatorio")
        Long petId,

        @Schema(description = "Consulta que originou a prescricao. Opcional.", example = "1")
        Long consultaId,

        @Schema(description = "Medicamento prescrito", example = "Amoxicilina 250mg")
        @NotBlank(message = "Medicamento e obrigatorio")
        @Size(max = 120, message = "Medicamento deve ter no maximo 120 caracteres")
        String medicamento,

        @Schema(description = "Dosagem por administracao", example = "1 comprimido")
        @NotBlank(message = "Dosagem e obrigatoria")
        @Size(max = 80, message = "Dosagem deve ter no maximo 80 caracteres")
        String dosagem,

        @Schema(description = "Intervalo entre as doses, em horas", example = "12")
        @NotNull(message = "Intervalo e obrigatorio")
        @Min(value = 1, message = "O intervalo minimo entre doses e de 1 hora")
        @Max(value = 24, message = "O intervalo maximo entre doses e de 24 horas")
        Integer intervaloHoras,

        @Schema(description = "Duracao do tratamento, em dias", example = "7")
        @NotNull(message = "Duracao e obrigatoria")
        @Min(value = 1, message = "A duracao minima e de 1 dia")
        @Max(value = 90, message = "A duracao maxima e de 90 dias")
        Integer duracaoDias,

        @Schema(description = "Data e hora da primeira dose", example = "2026-09-15T08:00:00")
        @NotNull(message = "Horario da primeira dose e obrigatorio")
        LocalDateTime inicioEm,

        @Schema(description = "Orientacoes ao tutor", example = "Dar junto com a comida")
        @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres")
        String observacoes
) {
}
