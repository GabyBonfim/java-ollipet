package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.DoseTratamentoDTO;
import org.fiap.challenge_clyvo.dto.TratamentoResponseDTO;
import org.fiap.challenge_clyvo.model.DoseTratamento;
import org.fiap.challenge_clyvo.model.Tratamento;
import org.fiap.challenge_clyvo.service.PlanoDeDoses;
import org.fiap.challenge_clyvo.service.ResumoDeAderencia;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TratamentoMapper {
    private final PlanoDeDoses planoDeDoses;

    public TratamentoMapper(PlanoDeDoses planoDeDoses) {
        this.planoDeDoses = planoDeDoses;
    }

    public TratamentoResponseDTO toDTO(Tratamento tratamento, LocalDateTime agora) {
        ResumoDeAderencia resumo = planoDeDoses.resumir(tratamento.getDoses(), agora);

        return new TratamentoResponseDTO(
                tratamento.getId(),
                tratamento.getPet().getId(),
                tratamento.getPet().getNome(),
                tratamento.getPet().getResponsavel().getNome(),
                tratamento.getVeterinario().getNome(),
                tratamento.getConsulta() == null ? null : tratamento.getConsulta().getId(),
                tratamento.getMedicamento(),
                tratamento.getDosagem(),
                tratamento.getIntervaloHoras(),
                tratamento.getDuracaoDias(),
                tratamento.getInicioEm(),
                tratamento.terminaEm(),
                tratamento.getStatus(),
                tratamento.getObservacoes(),
                resumo.total(),
                resumo.administradas(),
                resumo.perdidas(),
                resumo.pendentes(),
                resumo.percentual(),
                tratamento.getDoses().stream().map(dose -> toDoseDTO(dose, agora)).toList()
        );
    }

    public DoseTratamentoDTO toDoseDTO(DoseTratamento dose, LocalDateTime agora) {
        Tratamento tratamento = dose.getTratamento();

        return new DoseTratamentoDTO(
                dose.getId(),
                tratamento.getId(),
                tratamento.getPet().getId(),
                tratamento.getPet().getNome(),
                tratamento.getMedicamento(),
                tratamento.getDosagem(),
                dose.getNumeroDose(),
                tratamento.getDoses().size(),
                dose.getHorarioPrevisto(),
                dose.getConfirmadoEm(),
                dose.atrasoEmMinutos(),
                planoDeDoses.situacaoDe(dose, agora)
        );
    }

    public List<DoseTratamentoDTO> toDosesDTO(List<DoseTratamento> doses, LocalDateTime agora) {
        return doses.stream().map(dose -> toDoseDTO(dose, agora)).toList();
    }
}
