package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Encerrar a consulta e o mesmo ato de registrar o prontuario: por isso o procedimento
 * e o local vem aqui, e nao em uma chamada separada que poderia nunca acontecer.
 */
@Schema(description = "Dados do atendimento realizado, gravados como prontuario ao concluir a consulta")
public record EncerramentoConsultaDTO(
        @Schema(description = "Procedimento realizado", example = "Consulta dermatologica com raspado de pele")
        @NotBlank(message = "Procedimento e obrigatorio")
        @Size(max = 255, message = "Procedimento deve ter no maximo 255 caracteres")
        String procedimento,

        @Schema(description = "Local do atendimento", example = "Clinica Clyvo - Unidade Paulista")
        @NotBlank(message = "Local do atendimento e obrigatorio")
        @Size(max = 120, message = "Local deve ter no maximo 120 caracteres")
        String localAtendimento,

        @Schema(description = "Observacoes clinicas", example = "Retorno em 15 dias")
        @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres")
        String observacoes
) {
}
