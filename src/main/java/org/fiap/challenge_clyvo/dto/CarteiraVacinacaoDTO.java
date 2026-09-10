package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.Especie;

import java.util.List;

@Schema(description = "Carteira de vacinacao consolidada do pet")
public record CarteiraVacinacaoDTO(
        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Especie do pet", example = "CAO")
        Especie especie,

        @Schema(description = "Quantidade de vacinas que exigem atencao", example = "2")
        long pendencias,

        @Schema(description = "Uma linha por vacina prevista para a especie")
        List<ItemCarteiraDTO> itens
) {
}
