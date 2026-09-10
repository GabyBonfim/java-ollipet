package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.model.AcaoSugerida;
import org.fiap.challenge_clyvo.model.CategoriaQueixa;
import org.fiap.challenge_clyvo.model.ClassificacaoTriagem;
import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.model.OpcaoResposta;
import org.fiap.challenge_clyvo.model.Pet;
import org.fiap.challenge_clyvo.model.Queixa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliadorDeTriagemTest {
    private static final ContextoClinico SEM_PENDENCIA = new ContextoClinico(List.of(), List.of(), false);

    private final AvaliadorDeTriagem avaliador = new AvaliadorDeTriagem();

    @Test
    @DisplayName("soma dos pesos abaixo do limite resulta em orientacao em casa")
    void pontuacaoBaixaViraOrientacao() {
        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaGastro(), List.of(opcao(1), opcao(2)), SEM_PENDENCIA);

        assertEquals(3, resultado.pontuacao());
        assertEquals(ClassificacaoTriagem.ORIENTACAO, resultado.classificacao());
        assertEquals(AcaoSugerida.ACOMPANHAR, resultado.acaoSugerida());
    }

    @Test
    @DisplayName("pontuacao acima do limite da queixa resulta em urgente")
    void pontuacaoAltaViraUrgente() {
        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaGastro(), List.of(opcao(5), opcao(5), opcao(4)), SEM_PENDENCIA);

        assertEquals(ClassificacaoTriagem.URGENTE, resultado.classificacao());
        assertEquals(AcaoSugerida.AGENDAR, resultado.acaoSugerida());
    }

    @Test
    @DisplayName("um unico sinal de alerta leva a emergencia mesmo com pontuacao baixa")
    void sinalDeAlertaSobrepoePontuacao() {
        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaGastro(), List.of(opcao(0), alerta()), SEM_PENDENCIA);

        assertEquals(ClassificacaoTriagem.EMERGENCIA, resultado.classificacao());
        assertEquals(AcaoSugerida.IR_AGORA, resultado.acaoSugerida());
        assertTrue(resultado.observacoes().stream().anyMatch(texto -> texto.startsWith("Sinal de alerta")));
    }

    @Test
    @DisplayName("emergencia nao oferece agendamento")
    void emergenciaNaoAgenda() {
        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaGastro(), List.of(alerta()), SEM_PENDENCIA);

        assertEquals(false, resultado.acaoSugerida().geraAgendamento());
    }

    @Test
    @DisplayName("vacinacao atrasada agrava queixa gastrointestinal")
    void vacinacaoAtrasadaAgravaQueixaGastro() {
        List<OpcaoResposta> respostas = List.of(opcao(2), opcao(3));
        ContextoClinico comAtraso = new ContextoClinico(List.of("V10 atrasada ha 186 dias"), List.of(), false);

        ResultadoDaTriagem semAtraso = avaliador.avaliar(petAdulto(Especie.CAO), queixaGastro(),
                respostas, SEM_PENDENCIA);
        ResultadoDaTriagem resultado = avaliador.avaliar(petAdulto(Especie.CAO), queixaGastro(),
                respostas, comAtraso);

        assertEquals(semAtraso.pontuacao() + 5, resultado.pontuacao());
        assertEquals(ClassificacaoTriagem.ORIENTACAO, semAtraso.classificacao());
        assertEquals(ClassificacaoTriagem.POUCO_URGENTE, resultado.classificacao());
    }

    @Test
    @DisplayName("o mesmo atraso nao pesa em queixa que nao seja gastrointestinal")
    void vacinacaoAtrasadaNaoAfetaOutrasQueixas() {
        ContextoClinico comAtraso = new ContextoClinico(List.of("V10 atrasada ha 186 dias"), List.of(), false);

        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaDePele(), List.of(opcao(2), opcao(3)), comAtraso);

        assertEquals(5, resultado.pontuacao());
    }

    @Test
    @DisplayName("filhote soma peso extra sobre a mesma resposta")
    void filhoteSomaPesoExtra() {
        List<OpcaoResposta> respostas = List.of(opcao(2));

        ResultadoDaTriagem adulto = avaliador.avaliar(petAdulto(Especie.CAO), queixaGastro(),
                respostas, SEM_PENDENCIA);
        ResultadoDaTriagem filhote = avaliador.avaliar(petComIdadeEmMeses(3, Especie.CAO), queixaGastro(),
                respostas, SEM_PENDENCIA);

        assertEquals(adulto.pontuacao() + 3, filhote.pontuacao());
    }

    @Test
    @DisplayName("jejum prolongado pesa em gato e nao pesa em cao")
    void jejumProlongadoSoPesaEmFelino() {
        List<OpcaoResposta> respostas = List.of(opcaoComMarcador(5, "JEJUM_PROLONGADO"));

        ResultadoDaTriagem cao = avaliador.avaliar(petAdulto(Especie.CAO), queixaAlimentar(),
                respostas, SEM_PENDENCIA);
        ResultadoDaTriagem gato = avaliador.avaliar(petAdulto(Especie.GATO), queixaAlimentar(),
                respostas, SEM_PENDENCIA);

        assertEquals(5, cao.pontuacao());
        assertEquals(9, gato.pontuacao());
    }

    @Test
    @DisplayName("tratamento em curso levanta suspeita de reacao em queixa de pele")
    void tratamentoEmCursoAgravaQueixaDePele() {
        List<OpcaoResposta> respostas = List.of(opcao(2), opcao(3));
        ContextoClinico emTratamento = new ContextoClinico(List.of(), List.of("Amoxicilina 250mg"), false);

        ResultadoDaTriagem semTratamento = avaliador.avaliar(petAdulto(Especie.CAO), queixaDePele(),
                respostas, SEM_PENDENCIA);
        ResultadoDaTriagem resultado = avaliador.avaliar(petAdulto(Especie.CAO), queixaDePele(),
                respostas, emTratamento);

        assertEquals(semTratamento.pontuacao() + 2, resultado.pontuacao());
        assertTrue(resultado.observacoes().stream()
                .anyMatch(texto -> texto.contains("possivel reacao adversa")));
    }

    @Test
    @DisplayName("tratamento em curso nao pesa em queixa que nao seja de pele")
    void tratamentoEmCursoNaoAfetaOutrasQueixas() {
        ContextoClinico emTratamento = new ContextoClinico(List.of(), List.of("Amoxicilina 250mg"), false);

        ResultadoDaTriagem resultado = avaliador.avaliar(
                petAdulto(Especie.CAO), queixaGastro(), List.of(opcao(2), opcao(3)), emTratamento);

        assertEquals(5, resultado.pontuacao());
    }

    @Test
    @DisplayName("atendimento recente transforma a indicacao em retorno")
    void consultaRecenteViraRetorno() {
        ContextoClinico comConsultaRecente = new ContextoClinico(List.of(), List.of(), true);

        ResultadoDaTriagem resultado = avaliador.avaliar(petAdulto(Especie.CAO), queixaGastro(),
                List.of(opcao(5), opcao(4)), comConsultaRecente);

        assertEquals(AcaoSugerida.AGENDAR_RETORNO, resultado.acaoSugerida());
        assertTrue(resultado.acaoSugerida().geraAgendamento());
    }

    private Queixa queixaGastro() {
        return queixa(CategoriaQueixa.GASTROINTESTINAL, 12, 6);
    }

    private Queixa queixaDePele() {
        return queixa(CategoriaQueixa.DERMATOLOGICO, 12, 5);
    }

    private Queixa queixaAlimentar() {
        return queixa(CategoriaQueixa.ALIMENTAR, 11, 5);
    }

    private Queixa queixa(CategoriaQueixa categoria, int limiteUrgente, int limitePoucoUrgente) {
        Queixa queixa = new Queixa();
        queixa.setId(1L);
        queixa.setNome("Queixa de teste");
        queixa.setCategoria(categoria);
        queixa.setLimiteUrgente(limiteUrgente);
        queixa.setLimitePoucoUrgente(limitePoucoUrgente);
        return queixa;
    }

    private Pet petAdulto(Especie especie) {
        return petComIdadeEmMeses(36, especie);
    }

    private Pet petComIdadeEmMeses(int meses, Especie especie) {
        Pet pet = new Pet();
        pet.setNome("Teste");
        pet.setEspecie(especie);
        pet.setDataNascimento(LocalDate.now().minusMonths(meses));
        return pet;
    }

    private OpcaoResposta opcao(int peso) {
        return opcaoComMarcador(peso, null);
    }

    private OpcaoResposta opcaoComMarcador(int peso, String marcador) {
        OpcaoResposta opcao = new OpcaoResposta();
        opcao.setTexto("Resposta de teste");
        opcao.setPeso(peso);
        opcao.setMarcador(marcador);
        opcao.setSinalAlerta(false);
        return opcao;
    }

    private OpcaoResposta alerta() {
        OpcaoResposta opcao = opcao(0);
        opcao.setTexto("Nao levanta, muito prostrado");
        opcao.setSinalAlerta(true);
        return opcao;
    }
}
