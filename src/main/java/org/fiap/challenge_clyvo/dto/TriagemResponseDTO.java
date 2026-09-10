package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.AcaoSugerida;
import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;
import org.fiap.challenge_clyvo.model.StatusTriagem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A pontuacao nao aparece aqui de proposito: numero solto assusta o tutor e nao ajuda
 * na decisao. Ela e exposta apenas na fila da clinica.
 */
@Schema(description = "Resultado da triagem")
public record TriagemResponseDTO(
        @Schema(description = "ID da triagem", example = "7")
        Long id,

        @Schema(description = "ID do pet", example = "1")
        Long petId,

        @Schema(description = "Nome do pet", example = "Thor")
        String nomePet,

        @Schema(description = "Queixa avaliada", example = "Vomito ou diarreia")
        String queixa,

        @Schema(description = "Classificacao de urgencia", example = "URGENTE")
        ClassificacaoTriagem classificacao,

        @Schema(description = "Prazo recomendado, nulo quando nao ha indicacao de consulta",
                example = "24 horas")
        String prazoRecomendado,

        @Schema(description = "Se a triagem indica consulta", example = "true")
        boolean recomendaConsulta,

        @Schema(description = "O que o aplicativo deve oferecer", example = "AGENDAR")
        AcaoSugerida acaoSugerida,

        @Schema(description = "Texto pronto para preencher o motivo do agendamento")
        String motivoSugerido,

        @Schema(description = "Achados deduzidos do cadastro do pet")
        List<String> observacoesClinicas,

        @Schema(description = "Cuidados recomendados")
        List<String> orientacoes,

        @Schema(description = "Situacao da triagem", example = "CLASSIFICADA")
        StatusTriagem status,

        @Schema(description = "Consulta gerada a partir desta triagem, se houver", example = "12")
        Long consultaId,

        @Schema(description = "Ate quando esta triagem pode virar agendamento")
        LocalDateTime expiraEm,

        @Schema(description = "Aviso obrigatorio exibido junto ao resultado")
        String aviso
) {
}
