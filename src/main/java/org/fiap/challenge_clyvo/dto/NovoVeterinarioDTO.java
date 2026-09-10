package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados que a clinica informa ao cadastrar um veterinario na equipe.
 *
 * Nao ha senha: o acesso do veterinario acontece pelo Firebase, e o vinculo com
 * este cadastro e feito pelo e-mail no primeiro login.
 */
@Schema(description = "Cadastro de um veterinario pela administracao da clinica")
public record NovoVeterinarioDTO(
        @Schema(description = "Nome completo", example = "Dra. Ana Prado")
        @NotBlank(message = "Nome e obrigatorio")
        @Size(max = 100, message = "Nome deve ter no maximo 100 caracteres")
        String nome,

        @Schema(description = "E-mail profissional; e por ele que a conta do Firebase sera reconhecida",
                example = "ana.prado@ollipet.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "Formato de e-mail invalido")
        @Size(max = 120, message = "E-mail deve ter no maximo 120 caracteres")
        String email,

        @Schema(description = "CPF, somente numeros", example = "12345678901")
        @NotBlank(message = "CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos numericos")
        String cpf,

        @Schema(description = "Registro no conselho", example = "SP-54321")
        @NotBlank(message = "CRMV e obrigatorio")
        @Size(max = 20, message = "CRMV deve ter no maximo 20 caracteres")
        String crmv,

        @Schema(description = "Area de atuacao", example = "Dermatologia")
        @Size(max = 80, message = "Especialidade deve ter no maximo 80 caracteres")
        String especialidade
) {
}
