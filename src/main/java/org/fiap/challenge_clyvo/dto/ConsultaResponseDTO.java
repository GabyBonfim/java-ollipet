package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.StatusConsulta;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Dados de saida da consulta")
public record ConsultaResponseDTO(
        @Schema(description = "ID da consulta", example = "1")
        Long id,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "ID do veterinario", example = "1")
        Long veterinarioId,

        @Schema(description = "Nome do veterinario", example = "Dra. Camila Duarte")
        String nomeVeterinario,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nomeResponsavel,

        @Schema(description = "Data e hora do atendimento", example = "2026-09-15T14:30:00")
        LocalDateTime dataHora,

        @Schema(description = "Motivo informado no agendamento")
        String motivo,

        @Schema(description = "Situacao atual", example = "CONFIRMADA")
        StatusConsulta status,

        @Schema(description = "Observacoes registradas pelo veterinario")
        String observacoes,

        @Schema(description = "Para quais situacoes esta consulta ainda pode ir")
        Set<StatusConsulta> proximosStatus
) {
}
