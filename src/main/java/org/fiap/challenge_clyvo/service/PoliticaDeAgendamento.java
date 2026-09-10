package org.fiap.challenge_clyvo.service;

import org.fiap.challenge_clyvo.exception.BusinessException;
import org.fiap.challenge_clyvo.model.Consulta;
import org.fiap.challenge_clyvo.model.StatusConsulta;
import org.fiap.challenge_clyvo.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Todas as restricoes de horario da clinica em um lugar so. O servico de consultas
 * apenas pergunta "pode?" e nao precisa conhecer expediente, duracao nem colisao.
 */
@Component
public class PoliticaDeAgendamento {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ConsultaRepository consultaRepository;
    private final Duration duracaoConsulta;
    private final Duration antecedenciaCancelamento;
    private final LocalTime abertura;
    private final LocalTime fechamento;

    public PoliticaDeAgendamento(ConsultaRepository consultaRepository,
                                 @Value("${app.agenda.duracao-consulta-minutos}") long duracaoMinutos,
                                 @Value("${app.agenda.antecedencia-minima-cancelamento-horas}") long antecedenciaHoras,
                                 @Value("${app.agenda.hora-abertura}") LocalTime abertura,
                                 @Value("${app.agenda.hora-fechamento}") LocalTime fechamento) {
        this.consultaRepository = consultaRepository;
        this.duracaoConsulta = Duration.ofMinutes(duracaoMinutos);
        this.antecedenciaCancelamento = Duration.ofHours(antecedenciaHoras);
        this.abertura = abertura;
        this.fechamento = fechamento;
    }

    public void validarNovoHorario(Long veterinarioId, LocalDateTime dataHora) {
        exigirFuturo(dataHora);
        exigirDiaUtil(dataHora);
        exigirDentroDoExpediente(dataHora);
        exigirAgendaLivre(veterinarioId, dataHora);
    }

    public void validarCancelamento(Consulta consulta) {
        if (consulta.getStatus() != StatusConsulta.CONFIRMADA) {
            return;
        }
        LocalDateTime limite = consulta.getDataHora().minus(antecedenciaCancelamento);
        if (LocalDateTime.now().isAfter(limite)) {
            throw new BusinessException(
                    "Consultas confirmadas so podem ser canceladas ate %d horas antes. Entre em contato com a clinica."
                            .formatted(antecedenciaCancelamento.toHours()));
        }
    }

    public LocalDateTime inicioDoDia(LocalDate data) {
        return data.atStartOfDay();
    }

    public LocalDateTime fimDoDia(LocalDate data) {
        return data.atTime(LocalTime.MAX);
    }

    public Duration getDuracaoConsulta() {
        return duracaoConsulta;
    }

    private void exigirFuturo(LocalDateTime dataHora) {
        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Nao e possivel agendar uma consulta no passado");
        }
    }

    private void exigirDiaUtil(LocalDateTime dataHora) {
        if (dataHora.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new BusinessException("A clinica nao atende aos domingos");
        }
    }

    private void exigirDentroDoExpediente(LocalDateTime dataHora) {
        LocalTime inicio = dataHora.toLocalTime();
        LocalTime termino = inicio.plus(duracaoConsulta);

        if (inicio.isBefore(abertura) || termino.isAfter(fechamento)) {
            throw new BusinessException("A clinica atende das %s as %s; o horario escolhido esta fora do expediente"
                    .formatted(abertura.format(HORA), fechamento.format(HORA)));
        }
    }

    private void exigirAgendaLivre(Long veterinarioId, LocalDateTime dataHora) {
        // Como toda consulta dura o mesmo tempo, duas se sobrepoem exatamente quando o
        // intervalo entre elas e menor que essa duracao — em qualquer ordem.
        boolean ocupado = consultaRepository.existeConflitoDeAgenda(
                veterinarioId,
                StatusConsulta.statusQueOcupamAgenda(),
                dataHora.minus(duracaoConsulta),
                dataHora.plus(duracaoConsulta));

        if (ocupado) {
            throw new BusinessException("O veterinario ja tem atendimento marcado proximo a esse horario");
        }
    }
}
