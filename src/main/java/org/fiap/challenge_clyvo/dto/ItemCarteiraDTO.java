package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.SituacaoVacina;

import java.time.LocalDate;

@Schema(description = "Situacao de uma vacina especifica na carteira do pet")
public record ItemCarteiraDTO(
        @Schema(description = "ID da vacina", example = "1")
        Long vacinaId,

        @Schema(description = "Nome da vacina", example = "V10")
        String nomeVacina,

        @Schema(description = "Doses ja aplicadas", example = "2")
        int dosesAplicadas,

        @Schema(description = "Doses previstas na serie inicial", example = "3")
        int dosesProtocolo,

        @Schema(description = "Data da ultima dose aplicada", example = "2026-08-20")
        LocalDate ultimaAplicacao,

        @Schema(description = "Vencimento da proxima dose", example = "2026-09-10")
        LocalDate proximaDose,

        @Schema(description = "Dias de atraso, zero quando esta em dia", example = "0")
        long diasEmAtraso,

        @Schema(description = "Situacao consolidada", example = "A_VENCER")
        SituacaoVacina situacao
) {
}
