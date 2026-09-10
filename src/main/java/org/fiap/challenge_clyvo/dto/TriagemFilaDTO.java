package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Triagem grave aguardando providencia da clinica")
public record TriagemFilaDTO(
        @Schema(description = "ID da triagem", example = "7")
        Long id,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Nome do tutor", example = "Maria Silva")
        String nomeResponsavel,

        @Schema(description = "Queixa relatada", example = "Vomito ou diarreia")
        String queixa,

        @Schema(description = "Classificacao de urgencia", example = "URGENTE")
        ClassificacaoTriagem classificacao,

        @Schema(description = "Pontuacao apurada, visivel apenas para a clinica", example = "14")
        Integer pontuacao,

        @Schema(description = "Achados deduzidos do cadastro do pet")
        List<String> observacoesClinicas,

        @Schema(description = "Quando o tutor preencheu")
        LocalDateTime criadoEm
) {
}
