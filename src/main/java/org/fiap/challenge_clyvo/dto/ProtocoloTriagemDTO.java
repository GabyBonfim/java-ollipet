package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Protocolo completo de uma queixa, usado pelo aplicativo para montar o formulario")
public record ProtocoloTriagemDTO(
        @Schema(description = "ID da queixa", example = "1")
        Long queixaId,

        @Schema(description = "Nome da queixa", example = "Vomito ou diarreia")
        String nome,

        @Schema(description = "Texto de apoio")
        String descricao,

        @Schema(description = "Perguntas em ordem de exibicao")
        List<PerguntaTriagemDTO> perguntas
) {
}
