package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de saida do tutor")
public record ResponsavelDTO(
        @Schema(description = "ID do tutor", example = "3")
        Long id,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nome,

        @Schema(description = "E-mail do tutor", example = "maria.silva@email.com")
        String email,

        @Schema(description = "CPF do tutor, somente numeros", example = "12345678901")
        String cpf,

        @Schema(description = "Data de nascimento", example = "1995-08-10")
        LocalDate dataNascimento
) {
}
