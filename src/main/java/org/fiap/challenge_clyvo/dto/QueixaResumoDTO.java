package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card de queixa exibido na primeira tela da triagem")
public record QueixaResumoDTO(
        @Schema(description = "ID da queixa", example = "1")
        Long id,

        @Schema(description = "Titulo do card", example = "Vomito ou diarreia")
        String nome,

        @Schema(description = "Texto de apoio", example = "Enjoo, vomito, fezes moles ou liquidas.")
        String descricao,

        @Schema(description = "Ordem de exibicao", example = "1")
        Integer ordem
) {
}
