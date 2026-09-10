package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.PetRequestDTO;
import org.fiap.challenge_clyvo.dto.PetResponseDTO;
import org.fiap.challenge_clyvo.model.Pet;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PetMapper {

    public PetResponseDTO toDTO(Pet pet) {
        return new PetResponseDTO(
                pet.getId(),
                pet.getNome(),
                pet.getDescricao(),
                pet.getRaca(),
                pet.getEspecie(),
                pet.getDataNascimento(),
                Period.between(pet.getDataNascimento(), LocalDate.now()).getYears(),
                pet.getResponsavel().getId(),
                pet.getResponsavel().getNome()
        );
    }

    /** Aplica os campos editaveis sobre a entidade; dono e identidade nunca vem do corpo. */
    public void copiarParaEntidade(PetRequestDTO dto, Pet destino) {
        destino.setNome(dto.nome());
        destino.setDescricao(dto.descricao());
        destino.setRaca(dto.raca());
        destino.setEspecie(dto.especie());
        destino.setDataNascimento(dto.dataNascimento());
    }
}
