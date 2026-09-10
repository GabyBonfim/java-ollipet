package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.ConsultaResponseDTO;
import org.fiap.challenge_clyvo.model.Consulta;
import org.springframework.stereotype.Component;

@Component
public class ConsultaMapper {

    public ConsultaResponseDTO toDTO(Consulta consulta) {
        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getPet().getId(),
                consulta.getPet().getNome(),
                consulta.getVeterinario().getId(),
                consulta.getVeterinario().getNome(),
                consulta.getPet().getResponsavel().getNome(),
                consulta.getDataHora(),
                consulta.getMotivo(),
                consulta.getStatus(),
                consulta.getObservacoes(),
                consulta.getStatus().proximosPossiveis()
        );
    }
}
