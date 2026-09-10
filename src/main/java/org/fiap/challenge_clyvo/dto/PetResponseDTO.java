package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.Especie;

import java.time.LocalDate;

@Schema(description = "Dados de saida do pet")
public record PetResponseDTO(
        @Schema(description = "ID do pet", example = "1")
        Long id,

        @Schema(description = "Nome do pet", example = "Thor")
        String nome,

        @Schema(description = "Observacoes gerais", example = "Cachorro docil e vacinado")
        String descricao,

        @Schema(description = "Raca do pet", example = "Golden Retriever")
        String raca,

        @Schema(description = "Especie do pet", example = "CAO")
        Especie especie,

        @Schema(description = "Data de nascimento", example = "2021-03-15")
        LocalDate dataNascimento,

        @Schema(description = "Idade em anos completos", example = "5")
        int idade,

        @Schema(description = "ID do tutor", example = "3")
        Long responsavelId,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nomeResponsavel
) {
}
