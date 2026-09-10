package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.model.AcaoSugerida;
import org.fiap.challenge_clyvo.model.CategoriaQueixa;
import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;
import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.OpcaoResposta;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Queixa;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Decide a urgencia de uma triagem. Duas coisas acontecem aqui, nesta ordem:
 *
 * <ol>
 *   <li>a soma dos pesos das respostas, ajustada por modificadores que dependem do
 *       cadastro do pet;</li>
 *   <li>a checagem dos sinais de alerta, que sobrepoe qualquer pontuacao — um unico
 *       sinal marcado leva direto a EMERGENCIA.</li>
 * </ol>
 *
 * Os limites de cada faixa nao estao no codigo: vem da propria {@link Queixa}, porque
 * um problema de pele e um problema respiratorio nao tem a mesma sensibilidade.
 */
@Component
public class AvaliadorDeTriagem {
    private static final String MARCADOR_JEJUM_PROLONGADO = "JEJUM_PROLONGADO";

    private static final int IDADE_FILHOTE_EM_MESES = 6;
    private static final int IDADE_IDOSO_EM_ANOS = 8;

    private static final int PESO_FILHOTE = 3;
    private static final int PESO_IDOSO = 2;
    private static final int PESO_GASTRO_SEM_VACINA = 5;
    private static final int PESO_JEJUM_FELINO = 4;
    private static final int PESO_SUSPEITA_REACAO = 2;

    public ResultadoDaTriagem avaliar(Pet pet, Queixa queixa, List<OpcaoResposta> escolhas,
                                      ContextoClinico contexto) {
        List<String> observacoes = new ArrayList<>();
        int pontuacao = somarPesos(escolhas) + aplicarModificadores(pet, queixa, escolhas, contexto, observacoes);

        List<OpcaoResposta> alertas = escolhas.stream().filter(OpcaoResposta::isSinalAlerta).toList();
        if (!alertas.isEmpty()) {
            alertas.forEach(opcao -> observacoes.add(descreverAlerta(opcao)));
            return new ResultadoDaTriagem(pontuacao, ClassificacaoTriagem.EMERGENCIA,
                    AcaoSugerida.IR_AGORA, observacoes);
        }

        ClassificacaoTriagem classificacao = queixa.classificar(pontuacao);
        return new ResultadoDaTriagem(pontuacao, classificacao, decidirAcao(classificacao, contexto), observacoes);
    }

    private int somarPesos(List<OpcaoResposta> escolhas) {
        return escolhas.stream().mapToInt(OpcaoResposta::getPeso).sum();
    }

    /**
     * A opcao sozinha costuma ser um "Sim" solto. O veterinario precisa ler a pergunta
     * junto para entender o achado.
     */
    private String descreverAlerta(OpcaoResposta opcao) {
        if (opcao.getPergunta() == null) {
            return "Sinal de alerta: " + opcao.getTexto();
        }
        return "Sinal de alerta - %s %s".formatted(opcao.getPergunta().getTexto(), opcao.getTexto());
    }

    private int aplicarModificadores(Pet pet, Queixa queixa, List<OpcaoResposta> escolhas,
                                     ContextoClinico contexto, List<String> observacoes) {
        int extra = 0;
        extra += ajustePorIdade(pet, observacoes);
        extra += ajustePorVacinacao(queixa, contexto, observacoes);
        extra += ajustePorJejumFelino(pet, escolhas, observacoes);
        extra += ajustePorTratamentoEmCurso(queixa, contexto, observacoes);
        return extra;
    }

    /**
     * Queixa de pele em pet que esta tomando remedio pode ser reacao adversa, e nao um
     * problema dermatologico novo. O veterinario precisa ver isso antes de examinar.
     */
    private int ajustePorTratamentoEmCurso(Queixa queixa, ContextoClinico contexto, List<String> observacoes) {
        if (queixa.getCategoria() != CategoriaQueixa.DERMATOLOGICO || !contexto.estaEmTratamento()) {
            return 0;
        }
        observacoes.add("Pet em tratamento com %s: avaliar possivel reacao adversa"
                .formatted(String.join(", ", contexto.medicamentosEmUso())));
        return PESO_SUSPEITA_REACAO;
    }

    private int ajustePorIdade(Pet pet, List<String> observacoes) {
        Period idade = Period.between(pet.getDataNascimento(), LocalDate.now());

        if (idade.toTotalMonths() < IDADE_FILHOTE_EM_MESES) {
            observacoes.add("Filhote com %d meses: descompensa mais rapido que um adulto"
                    .formatted(idade.toTotalMonths()));
            return PESO_FILHOTE;
        }
        if (idade.getYears() >= IDADE_IDOSO_EM_ANOS) {
            observacoes.add("Animal idoso (%d anos): avaliar com mais cautela".formatted(idade.getYears()));
            return PESO_IDOSO;
        }
        return 0;
    }

    private int ajustePorVacinacao(Queixa queixa, ContextoClinico contexto, List<String> observacoes) {
        if (queixa.getCategoria() != CategoriaQueixa.GASTROINTESTINAL || !contexto.temVacinaAtrasada()) {
            return 0;
        }
        observacoes.add("Sintoma gastrointestinal com vacinacao em atraso (%s)"
                .formatted(String.join(", ", contexto.vacinasAtrasadas())));
        return PESO_GASTRO_SEM_VACINA;
    }

    private int ajustePorJejumFelino(Pet pet, List<OpcaoResposta> escolhas, List<String> observacoes) {
        boolean jejumProlongado = escolhas.stream()
                .anyMatch(opcao -> MARCADOR_JEJUM_PROLONGADO.equals(opcao.getMarcador()));

        if (pet.getEspecie() != Especie.GATO || !jejumProlongado) {
            return 0;
        }
        observacoes.add("Jejum prolongado em felino: risco de lipidose hepatica");
        return PESO_JEJUM_FELINO;
    }

    /** Se o pet foi atendido ha pouco pela mesma razao, o caminho e retorno e nao consulta nova. */
    private AcaoSugerida decidirAcao(ClassificacaoTriagem classificacao, ContextoClinico contexto) {
        if (classificacao.permiteAgendamento() && contexto.possuiConsultaRecenteConcluida()) {
            return AcaoSugerida.AGENDAR_RETORNO;
        }
        return classificacao.getAcaoSugerida();
    }
}
