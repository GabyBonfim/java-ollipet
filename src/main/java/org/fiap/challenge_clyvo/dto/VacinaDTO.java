package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.Especie;

@Schema(description = "Vacina do catalogo, com o protocolo previsto")
public record VacinaDTO(
        @Schema(description = "ID da vacina", example = "1")
        Long id,

        @Schema(description = "Nome da vacina", example = "V10")
        String nome,

        @Schema(description = "Especie a que se destina", example = "CAO")
        Especie especie,

        @Schema(description = "Doses da serie inicial", example = "3")
        Integer dosesProtocolo,

        @Schema(description = "Dias entre as doses da serie inicial", example = "21")
        Integer intervaloDias,

        @Schema(description = "Periodicidade do reforco em meses, nulo quando nao ha reforco",
                example = "12")
        Integer mesesReforco
) {
}
