package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.model.DoseTratamento;
import org.fiap.challenge_clyvo.model.StatusDose;
import org.fiap.challenge_clyvo.model.Tratamento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduz a prescricao em horarios concretos e le a adesao de volta.
 *
 * "12/12h por 7 dias" nao fica guardado como texto: vira 14 registros com data e hora,
 * porque so assim o tutor tem o que confirmar e o veterinario tem o que conferir.
 */
@Component
public class PlanoDeDoses {
    private static final int HORAS_POR_DIA = 24;

    private final Duration tolerancia;
    private final int maximoDeDoses;

    public PlanoDeDoses(@Value("${app.tratamento.tolerancia-confirmacao-horas}") long toleranciaHoras,
                        @Value("${app.tratamento.maximo-doses}") int maximoDeDoses) {
        this.tolerancia = Duration.ofHours(toleranciaHoras);
        this.maximoDeDoses = maximoDeDoses;
    }

    /** Cria as doses dentro do proprio tratamento, uma para cada horario previsto. */
    public void gerarDoses(Tratamento tratamento) {
        int total = quantidadeDeDoses(tratamento.getDuracaoDias(), tratamento.getIntervaloHoras());

        for (int numero = 1; numero <= total; numero++) {
            LocalDateTime horario = tratamento.getInicioEm()
                    .plusHours((long) (numero - 1) * tratamento.getIntervaloHoras());
            tratamento.adicionarDose(numero, horario);
        }
    }

    public int quantidadeDeDoses(int duracaoDias, int intervaloHoras) {
        int total = (duracaoDias * HORAS_POR_DIA) / intervaloHoras;

        if (total < 1) {
            throw new BusinessException("A duracao informada nao cabe nem uma dose no intervalo escolhido");
        }
        if (total > maximoDeDoses) {
            throw new BusinessException(
                    "Este plano geraria %d doses e o limite e %d. Aumente o intervalo ou reduza a duracao."
                            .formatted(total, maximoDeDoses));
        }
        return total;
    }

    public StatusDose situacaoDe(DoseTratamento dose, LocalDateTime agora) {
        return dose.situacaoEm(agora, tolerancia);
    }

    /**
     * A adesao considera apenas as doses cujo horario ja passou: cobrar o tutor por dose
     * que ainda nem venceu daria um percentual sempre baixo no comeco do tratamento.
     */
    public ResumoDeAderencia resumir(List<DoseTratamento> doses, LocalDateTime agora) {
        int administradas = 0;
        int perdidas = 0;
        int pendentes = 0;

        for (DoseTratamento dose : doses) {
            StatusDose situacao = situacaoDe(dose, agora);
            if (situacao == StatusDose.ADMINISTRADA) {
                administradas++;
            } else if (situacao == StatusDose.PERDIDA) {
                perdidas++;
            } else {
                pendentes++;
            }
        }

        int vencidas = administradas + perdidas;
        int percentual = vencidas == 0 ? 100 : (administradas * 100) / vencidas;

        return new ResumoDeAderencia(doses.size(), administradas, perdidas, pendentes, percentual);
    }

    public Duration getTolerancia() {
        return tolerancia;
    }
}
