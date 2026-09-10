package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dose registrada na carteira de vacinacao")
public record AplicacaoVacinaResponseDTO(
        @Schema(description = "ID da aplicacao", example = "1")
        Long id,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "ID da vacina", example = "1")
        Long vacinaId,

        @Schema(description = "Nome da vacina", example = "V10")
        String nomeVacina,

        @Schema(description = "Numero desta dose na serie", example = "2")
        Integer numeroDose,

        @Schema(description = "Total de doses da serie inicial", example = "3")
        Integer dosesProtocolo,

        @Schema(description = "Data em que foi aplicada", example = "2026-08-20")
        LocalDate dataAplicacao,

        @Schema(description = "Vencimento da proxima dose, nulo quando o protocolo terminou",
                example = "2026-09-10")
        LocalDate proximaDose,

        @Schema(description = "Lote utilizado", example = "LT-2026-0834")
        String lote,

        @Schema(description = "Veterinario que aplicou", example = "Dra. Camila Duarte")
        String nomeVeterinario
) {
}
