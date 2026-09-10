package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de saida do veterinario")
public record VeterinarioDTO(
        @Schema(description = "ID do veterinario", example = "1")
        Long id,

        @Schema(description = "Nome do veterinario", example = "Dra. Camila Duarte")
        String nome,

        @Schema(description = "E-mail do veterinario", example = "camila.duarte@ollipet.com")
        String email,

        @Schema(description = "CPF do veterinario, somente numeros", example = "98765432100")
        String cpf,

        @Schema(description = "CRMV do veterinario", example = "SP-12345")
        String crmv,

        @Schema(description = "Especialidade", example = "Clinica Geral")
        String especialidade
) {
}
