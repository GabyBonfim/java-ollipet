package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Completa o cadastro de quem se autenticou no Firebase pelo app. Nome e e-mail vem
 * do proprio token, entao o app so precisa perguntar o que o Firebase nao tem.
 */
@Schema(description = "Dados complementares do tutor que se cadastrou pelo aplicativo")
public record RegistroFirebaseDTO(
        @Schema(description = "CPF do tutor, somente numeros", example = "12345678901")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos numericos")
        String cpf,

        @Schema(description = "Data de nascimento do tutor", example = "1995-08-10")
        @Past(message = "Data de nascimento deve estar no passado")
        LocalDate dataNascimento
) {
}
