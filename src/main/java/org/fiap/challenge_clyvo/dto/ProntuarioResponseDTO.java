package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados de saida do prontuario")
public record ProntuarioResponseDTO(
        @Schema(description = "ID do prontuario", example = "1")
        Long id,

        @Schema(description = "Procedimento realizado", example = "Vacinacao antirrabica")
        String procedimento,

        @Schema(description = "Data do atendimento", example = "2026-05-20")
        LocalDate dataProcedimento,

        @Schema(description = "Local do atendimento", example = "Clinica Clyvo - Unidade Paulista")
        String localAtendimento,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "ID do veterinario", example = "1")
        Long veterinarioId,

        @Schema(description = "Nome do veterinario", example = "Dra. Camila Duarte")
        String nomeVeterinario,

        @Schema(description = "Consulta que originou o prontuario, nulo em atendimentos avulsos",
                example = "7")
        Long consultaId
) {
}
