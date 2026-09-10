package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.ProntuarioResponseDTO;
import org.fiap.challenge_clyvo.model.Prontuario;
import org.springframework.stereotype.Component;

@Component
public class ProntuarioMapper {

    public ProntuarioResponseDTO toDTO(Prontuario prontuario) {
        return new ProntuarioResponseDTO(
                prontuario.getId(),
                prontuario.getProcedimento(),
                prontuario.getDataProcedimento(),
                prontuario.getLocalAtendimento(),
                prontuario.getPet().getId(),
                prontuario.getPet().getNome(),
                prontuario.getVeterinario().getId(),
                prontuario.getVeterinario().getNome(),
                prontuario.getConsulta() == null ? null : prontuario.getConsulta().getId()
        );
    }
}
