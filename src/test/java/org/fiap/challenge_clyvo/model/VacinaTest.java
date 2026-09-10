package org.fiap.challenge_clyvo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VacinaTest {
    private static final LocalDate APLICACAO = LocalDate.of(2026, 3, 10);

    @Test
    @DisplayName("dentro da serie inicial a proxima dose usa o intervalo em dias")
    void intervaloDentroDaSerie() {
        Vacina v10 = vacina(3, 21, 12);

        assertEquals(LocalDate.of(2026, 3, 31), v10.calcularVencimentoAposDose(1, APLICACAO));
        assertEquals(LocalDate.of(2026, 3, 31), v10.calcularVencimentoAposDose(2, APLICACAO));
    }

    @Test
    @DisplayName("concluida a serie, a proxima dose passa a ser o reforco anual")
    void reforcoAposASerie() {
        Vacina v10 = vacina(3, 21, 12);

        assertEquals(LocalDate.of(2027, 3, 10), v10.calcularVencimentoAposDose(3, APLICACAO));
        assertTrue(v10.serieConcluidaCom(3));
    }

    @Test
    @DisplayName("vacina de dose unica com reforco anual vence em um ano")
    void doseUnicaComReforco() {
        Vacina antirrabica = vacina(1, 0, 12);

        assertEquals(LocalDate.of(2027, 3, 10), antirrabica.calcularVencimentoAposDose(1, APLICACAO));
    }

    @Test
    @DisplayName("protocolo sem reforco encerra e nao gera proxima dose")
    void protocoloSemReforcoEncerra() {
        Vacina semReforco = vacina(2, 30, null);

        assertEquals(LocalDate.of(2026, 4, 9), semReforco.calcularVencimentoAposDose(1, APLICACAO));
        assertNull(semReforco.calcularVencimentoAposDose(2, APLICACAO));
    }

    private Vacina vacina(int doses, int intervaloDias, Integer mesesReforco) {
        Vacina vacina = new Vacina();
        vacina.setNome("Teste");
        vacina.setEspecie(Especie.CAO);
        vacina.setDosesProtocolo(doses);
        vacina.setIntervaloDias(intervaloDias);
        vacina.setMesesReforco(mesesReforco);
        return vacina;
    }
}
