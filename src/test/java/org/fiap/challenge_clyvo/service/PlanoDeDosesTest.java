package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.model.DoseTratamento;
import org.fiap.challenge_clyvo.model.StatusDose;
import org.fiap.challenge_clyvo.model.Tratamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanoDeDosesTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 8, 28, 8, 0);

    private final PlanoDeDoses plano = new PlanoDeDoses(2, 120);

    @Test
    @DisplayName("12 em 12 horas por 7 dias gera 14 doses a partir do horario inicial")
    void geraQuatorzeDoses() {
        Tratamento tratamento = tratamento(12, 7);

        plano.gerarDoses(tratamento);

        assertEquals(14, tratamento.getDoses().size());
        assertEquals(INICIO, tratamento.getDoses().get(0).getHorarioPrevisto());
        assertEquals(INICIO.plusHours(12), tratamento.getDoses().get(1).getHorarioPrevisto());
        assertEquals(INICIO.plusHours(156), tratamento.getDoses().get(13).getHorarioPrevisto());
    }

    @Test
    @DisplayName("uma vez ao dia por 5 dias gera 5 doses")
    void geraUmaDosePorDia() {
        Tratamento tratamento = tratamento(24, 5);

        plano.gerarDoses(tratamento);

        assertEquals(5, tratamento.getDoses().size());
    }

    @Test
    @DisplayName("plano que estouraria o limite de doses e recusado")
    void recusaPlanoExcessivo() {
        assertThrows(BusinessException.class, () -> plano.quantidadeDeDoses(90, 1));
    }

    @Test
    @DisplayName("dose ainda dentro da tolerancia continua pendente")
    void dosePendenteDentroDaTolerancia() {
        DoseTratamento dose = dose(INICIO);

        assertEquals(StatusDose.PENDENTE, plano.situacaoDe(dose, INICIO.plusHours(1)));
    }

    @Test
    @DisplayName("dose vira perdida sozinha depois da tolerancia, sem nada ser gravado")
    void dosePerdidaAposTolerancia() {
        DoseTratamento dose = dose(INICIO);

        assertEquals(StatusDose.PERDIDA, plano.situacaoDe(dose, INICIO.plusHours(3)));
    }

    @Test
    @DisplayName("dose confirmada continua administrada mesmo muito depois")
    void doseConfirmadaPermaneceAdministrada() {
        DoseTratamento dose = dose(INICIO);
        dose.confirmar(INICIO.plusMinutes(30));

        assertEquals(StatusDose.ADMINISTRADA, plano.situacaoDe(dose, INICIO.plusDays(10)));
        assertEquals(30, dose.atrasoEmMinutos());
    }

    @Test
    @DisplayName("nao e possivel confirmar dose futura nem confirmar duas vezes")
    void confirmacoesInvalidas() {
        DoseTratamento futura = dose(INICIO.plusDays(1));
        assertThrows(BusinessException.class, () -> futura.confirmar(INICIO));

        DoseTratamento jaDada = dose(INICIO);
        jaDada.confirmar(INICIO.plusMinutes(5));
        assertThrows(BusinessException.class, () -> jaDada.confirmar(INICIO.plusMinutes(10)));
    }

    @Test
    @DisplayName("a adesao ignora doses que ainda nem venceram")
    void aderenciaConsideraApenasDosesVencidas() {
        DoseTratamento primeira = dose(INICIO);
        DoseTratamento segunda = dose(INICIO.plusHours(12));
        DoseTratamento terceira = dose(INICIO.plusHours(24));
        primeira.confirmar(INICIO.plusMinutes(10));

        // Momento em que a 1a foi dada, a 2a passou da tolerancia e a 3a ainda nem chegou.
        ResumoDeAderencia resumo = plano.resumir(List.of(primeira, segunda, terceira), INICIO.plusHours(20));

        assertEquals(3, resumo.total());
        assertEquals(1, resumo.administradas());
        assertEquals(1, resumo.perdidas());
        assertEquals(1, resumo.pendentes());
        assertEquals(50, resumo.percentual());
    }

    @Test
    @DisplayName("tratamento que ainda nao teve nenhuma dose vencida comeca com 100%")
    void aderenciaInicialNaoPunePorDoseFutura() {
        ResumoDeAderencia resumo = plano.resumir(List.of(dose(INICIO.plusDays(1))), INICIO);

        assertEquals(100, resumo.percentual());
        assertEquals(1, resumo.pendentes());
    }

    private Tratamento tratamento(int intervaloHoras, int duracaoDias) {
        Tratamento tratamento = new Tratamento();
        tratamento.setMedicamento("Amoxicilina 250mg");
        tratamento.setDosagem("1 comprimido");
        tratamento.setIntervaloHoras(intervaloHoras);
        tratamento.setDuracaoDias(duracaoDias);
        tratamento.setInicioEm(INICIO);
        return tratamento;
    }

    private DoseTratamento dose(LocalDateTime horarioPrevisto) {
        DoseTratamento dose = new DoseTratamento();
        dose.setNumeroDose(1);
        dose.setHorarioPrevisto(horarioPrevisto);
        return dose;
    }
}
