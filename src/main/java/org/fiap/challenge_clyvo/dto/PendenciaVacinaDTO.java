package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.SituacaoVacina;

import java.time.LocalDate;

@Schema(description = "Dose vencida ou proxima do vencimento, para o painel da clinica")
public record PendenciaVacinaDTO(
        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nomeResponsavel,

        @Schema(description = "Nome da vacina", example = "V10")
        String nomeVacina,

        @Schema(description = "Numero da proxima dose", example = "3")
        int proximaDoseNumero,

        @Schema(description = "Data de vencimento", example = "2026-09-10")
        LocalDate proximaDose,

        @Schema(description = "Dias de atraso, zero quando ainda nao venceu", example = "12")
        long diasEmAtraso,

        @Schema(description = "Situacao consolidada", example = "ATRASADA")
        SituacaoVacina situacao
) {
}
