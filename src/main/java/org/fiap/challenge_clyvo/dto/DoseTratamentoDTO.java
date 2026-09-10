package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.StatusDose;

import java.time.LocalDateTime;

@Schema(description = "Uma dose do plano de tratamento")
public record DoseTratamentoDTO(
        @Schema(description = "ID da dose", example = "3")
        Long id,

        @Schema(description = "ID do tratamento", example = "1")
        Long tratamentoId,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Medicamento", example = "Amoxicilina 250mg")
        String medicamento,

        @Schema(description = "Dosagem", example = "1 comprimido")
        String dosagem,

        @Schema(description = "Numero desta dose no plano", example = "3")
        Integer numeroDose,

        @Schema(description = "Total de doses do plano", example = "14")
        Integer totalDoses,

        @Schema(description = "Horario previsto", example = "2026-09-16T08:00:00")
        LocalDateTime horarioPrevisto,

        @Schema(description = "Quando o tutor confirmou, nulo se ainda nao confirmou")
        LocalDateTime confirmadoEm,

        @Schema(description = "Minutos de atraso na confirmacao", example = "0")
        long atrasoEmMinutos,

        @Schema(description = "Situacao deduzida do horario e da confirmacao", example = "PENDENTE")
        StatusDose situacao
) {
}
