package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.StatusTratamento;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Tratamento com o plano de doses e o resumo de adesao")
public record TratamentoResponseDTO(
        @Schema(description = "ID do tratamento", example = "1")
        Long id,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nomeResponsavel,

        @Schema(description = "Veterinario que prescreveu", example = "Dra. Camila Duarte")
        String nomeVeterinario,

        @Schema(description = "Consulta de origem, nulo em prescricao avulsa", example = "1")
        Long consultaId,

        @Schema(description = "Medicamento", example = "Amoxicilina 250mg")
        String medicamento,

        @Schema(description = "Dosagem", example = "1 comprimido")
        String dosagem,

        @Schema(description = "Intervalo entre doses em horas", example = "12")
        Integer intervaloHoras,

        @Schema(description = "Duracao em dias", example = "7")
        Integer duracaoDias,

        @Schema(description = "Primeira dose", example = "2026-09-15T08:00:00")
        LocalDateTime inicioEm,

        @Schema(description = "Fim previsto", example = "2026-09-22T08:00:00")
        LocalDateTime terminaEm,

        @Schema(description = "Situacao do tratamento", example = "EM_ANDAMENTO")
        StatusTratamento status,

        @Schema(description = "Orientacoes registradas")
        String observacoes,

        @Schema(description = "Total de doses previstas", example = "14")
        int totalDoses,

        @Schema(description = "Doses confirmadas pelo tutor", example = "12")
        int dosesAdministradas,

        @Schema(description = "Doses cujo horario passou sem confirmacao", example = "2")
        int dosesPerdidas,

        @Schema(description = "Doses ainda dentro do prazo", example = "0")
        int dosesPendentes,

        @Schema(description = "Percentual de adesao sobre as doses ja vencidas", example = "86")
        int aderenciaPercentual,

        @Schema(description = "Plano completo, dose a dose")
        List<DoseTratamentoDTO> doses
) {
}
