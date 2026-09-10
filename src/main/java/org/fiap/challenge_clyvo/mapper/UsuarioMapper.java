package org.fiap.challenge_clyvo.mapper;

import org.fiap.challenge_clyvo.dto.ResponsavelDTO;
import org.fiap.challenge_clyvo.dto.VeterinarioDTO;
import org.fiap.challenge_clyvo.model.Responsavel;
import org.fiap.challenge_clyvo.model.Veterinario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public ResponsavelDTO toDTO(Responsavel responsavel) {
        return new ResponsavelDTO(
                responsavel.getId(),
                responsavel.getNome(),
                responsavel.getEmail(),
                responsavel.getCpf(),
                responsavel.getDataNascimento()
        );
    }

    public VeterinarioDTO toDTO(Veterinario veterinario) {
        return new VeterinarioDTO(
                veterinario.getId(),
                veterinario.getNome(),
                veterinario.getEmail(),
                veterinario.getCpf(),
                veterinario.getCrmv(),
                veterinario.getEspecialidade()
        );
    }
}
