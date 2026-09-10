package org.fiap.challenge_clyvo.model;

import org.fiap.challenge_clyvo.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusConsultaTest {

    @Test
    @DisplayName("o caminho feliz vai de solicitada ate concluida")
    void percursoCompleto() {
        Consulta consulta = novaConsulta();

        consulta.moverPara(StatusConsulta.CONFIRMADA);
        consulta.moverPara(StatusConsulta.EM_ATENDIMENTO);
        consulta.moverPara(StatusConsulta.CONCLUIDA);

        assertEquals(StatusConsulta.CONCLUIDA, consulta.getStatus());
    }

    @Test
    @DisplayName("nao permite concluir uma consulta que nunca foi atendida")
    void naoPulaEtapas() {
        Consulta consulta = novaConsulta();

        assertThrows(TransicaoInvalidaException.class, () -> consulta.moverPara(StatusConsulta.CONCLUIDA));
        assertEquals(StatusConsulta.SOLICITADA, consulta.getStatus());
    }

    @Test
    @DisplayName("consulta concluida nao volta atras")
    void estadoFinalNaoMuda() {
        Consulta consulta = novaConsulta();
        consulta.moverPara(StatusConsulta.CONFIRMADA);
        consulta.moverPara(StatusConsulta.EM_ATENDIMENTO);
        consulta.moverPara(StatusConsulta.CONCLUIDA);

        assertTrue(consulta.getStatus().isFinal());
        assertThrows(TransicaoInvalidaException.class, () -> consulta.moverPara(StatusConsulta.CANCELADA));
    }

    @Test
    @DisplayName("consulta ja em atendimento nao pode mais ser cancelada")
    void naoCancelaEmAtendimento() {
        Consulta consulta = novaConsulta();
        consulta.moverPara(StatusConsulta.CONFIRMADA);
        consulta.moverPara(StatusConsulta.EM_ATENDIMENTO);

        assertThrows(TransicaoInvalidaException.class, () -> consulta.moverPara(StatusConsulta.CANCELADA));
    }

    @Test
    @DisplayName("apenas os estados ativos bloqueiam o horario do veterinario")
    void ocupacaoDaAgenda() {
        assertTrue(StatusConsulta.SOLICITADA.ocupaAgenda());
        assertTrue(StatusConsulta.CONFIRMADA.ocupaAgenda());
        assertTrue(StatusConsulta.EM_ATENDIMENTO.ocupaAgenda());

        assertFalse(StatusConsulta.CANCELADA.ocupaAgenda());
        assertFalse(StatusConsulta.CONCLUIDA.ocupaAgenda());
        assertFalse(StatusConsulta.NAO_COMPARECEU.ocupaAgenda());
    }

    private Consulta novaConsulta() {
        Consulta consulta = new Consulta();
        consulta.setDataHora(LocalDateTime.now().plusDays(1));
        consulta.setMotivo("Checkup");
        consulta.setStatus(StatusConsulta.SOLICITADA);
        return consulta;
    }
}
