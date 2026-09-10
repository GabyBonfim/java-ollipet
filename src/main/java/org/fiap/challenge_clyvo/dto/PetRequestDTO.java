package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.fiap.challenge_clyvo.model.Especie;

import java.time.LocalDate;

/**
 * O dono nunca vem no corpo da requisicao: o pet e sempre vinculado ao tutor autenticado.
 */
@Schema(description = "Dados de entrada para cadastro e atualizacao de pet")
public record PetRequestDTO(
        @Schema(description = "Nome do pet", example = "Thor")
        @NotBlank(message = "Nome do pet e obrigatorio")
        @Size(max = 80, message = "Nome do pet deve ter no maximo 80 caracteres")
        String nome,

        @Schema(description = "Observacoes gerais sobre o pet", example = "Cachorro docil e vacinado")
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres")
        String descricao,

        @Schema(description = "Raca do pet", example = "Golden Retriever")
        @NotBlank(message = "Raca e obrigatoria")
        @Size(max = 80, message = "Raca deve ter no maximo 80 caracteres")
        String raca,

        @Schema(description = "Especie do pet", example = "CAO")
        @NotNull(message = "Especie e obrigatoria")
        Especie especie,

        @Schema(description = "Data de nascimento do pet", example = "2021-03-15")
        @NotNull(message = "Data de nascimento e obrigatoria")
        @PastOrPresent(message = "Data de nascimento nao pode ser futura")
        LocalDate dataNascimento
) {
}
