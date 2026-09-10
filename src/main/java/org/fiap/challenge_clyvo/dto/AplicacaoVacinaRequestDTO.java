package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Registro de uma dose aplicada")
public record AplicacaoVacinaRequestDTO(
        @Schema(description = "ID da vacina do catalogo", example = "1")
        @NotNull(message = "ID da vacina e obrigatorio")
        Long vacinaId,

        @Schema(description = "Data da aplicacao", example = "2026-08-20")
        @NotNull(message = "Data da aplicacao e obrigatoria")
        @PastOrPresent(message = "Nao e possivel registrar uma aplicacao futura")
        LocalDate dataAplicacao,

        @Schema(description = "Lote do frasco utilizado", example = "LT-2026-0834")
        @Size(max = 40, message = "Lote deve ter no maximo 40 caracteres")
        String lote
) {
}
