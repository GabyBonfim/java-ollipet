package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.OpcaoRespostaDTO;
import org.fiap.challenge_clyvo.dto.PerguntaTriagemDTO;
import org.fiap.challenge_clyvo.dto.ProtocoloTriagemDTO;
import org.fiap.challenge_clyvo.dto.QueixaResumoDTO;
import org.fiap.challenge_clyvo.dto.TriagemFilaDTO;
import org.fiap.challenge_clyvo.dto.TriagemResponseDTO;
import org.fiap.challenge_clyvo.model.ObservacaoTriagem;
import org.fiap.challenge_clyvo.model.OpcaoResposta;
import org.fiap.challenge_clyvo.model.PerguntaTriagem;
import org.fiap.challenge_clyvo.model.Queixa;
import org.fiap.challenge_clyvo.model.RespostaTriagem;
import org.fiap.challenge_clyvo.model.Triagem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TriagemMapper {
    private static final String AVISO = "Esta orientacao nao substitui avaliacao veterinaria presencial.";
    private static final int TAMANHO_MAXIMO_DO_MOTIVO = 250;

    public QueixaResumoDTO toResumo(Queixa queixa) {
        return new QueixaResumoDTO(queixa.getId(), queixa.getNome(), queixa.getDescricao(), queixa.getOrdem());
    }

    public ProtocoloTriagemDTO toProtocolo(Queixa queixa, List<PerguntaTriagem> perguntas) {
        return new ProtocoloTriagemDTO(
                queixa.getId(),
                queixa.getNome(),
                queixa.getDescricao(),
                perguntas.stream().map(this::toPergunta).toList()
        );
    }

    public TriagemResponseDTO toResponse(Triagem triagem, List<String> orientacoes) {
        return new TriagemResponseDTO(
                triagem.getId(),
                triagem.getPet().getId(),
                triagem.getPet().getNome(),
                triagem.getQueixa().getNome(),
                triagem.getClassificacao(),
                triagem.getClassificacao().getPrazoRecomendado(),
                triagem.getAcaoSugerida().geraAgendamento(),
                triagem.getAcaoSugerida(),
                montarMotivoSugerido(triagem),
                textosDasObservacoes(triagem),
                orientacoes,
                triagem.getStatus(),
                triagem.getConsulta() == null ? null : triagem.getConsulta().getId(),
                triagem.getExpiraEm(),
                AVISO
        );
    }

    public TriagemFilaDTO toFila(Triagem triagem) {
        return new TriagemFilaDTO(
                triagem.getId(),
                triagem.getPet().getId(),
                triagem.getPet().getNome(),
                triagem.getPet().getResponsavel().getNome(),
                triagem.getQueixa().getNome(),
                triagem.getClassificacao(),
                triagem.getPontuacao(),
                textosDasObservacoes(triagem),
                triagem.getCriadoEm()
        );
    }

    /**
     * Texto pronto para o campo "motivo" do agendamento: junta a queixa com as respostas
     * que pesaram, para o veterinario abrir a agenda ja sabendo do que se trata.
     */
    public String montarMotivoSugerido(Triagem triagem) {
        String respostasRelevantes = triagem.getRespostas().stream()
                .map(RespostaTriagem::getOpcao)
                .filter(opcao -> opcao.getPeso() > 0 || opcao.isSinalAlerta())
                .map(OpcaoResposta::getTexto)
                .collect(Collectors.joining(", "));

        String motivo = respostasRelevantes.isEmpty()
                ? "%s (triagem #%d)".formatted(triagem.getQueixa().getNome(), triagem.getId())
                : "%s: %s (triagem #%d)".formatted(triagem.getQueixa().getNome(), respostasRelevantes,
                        triagem.getId());

        return motivo.length() <= TAMANHO_MAXIMO_DO_MOTIVO
                ? motivo
                : motivo.substring(0, TAMANHO_MAXIMO_DO_MOTIVO) + "...";
    }

    private PerguntaTriagemDTO toPergunta(PerguntaTriagem pergunta) {
        return new PerguntaTriagemDTO(
                pergunta.getId(),
                pergunta.getTexto(),
                pergunta.getOrdem(),
                pergunta.isObrigatoria(),
                pergunta.getOpcaoDependencia() == null ? null : pergunta.getOpcaoDependencia().getId(),
                pergunta.getOpcoes().stream().map(this::toOpcao).toList()
        );
    }

    private OpcaoRespostaDTO toOpcao(OpcaoResposta opcao) {
        return new OpcaoRespostaDTO(opcao.getId(), opcao.getTexto(), opcao.getOrdem());
    }

    private List<String> textosDasObservacoes(Triagem triagem) {
        return triagem.getObservacoes().stream().map(ObservacaoTriagem::getTexto).toList();
    }
}
