package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para obter um token da propria API")
public record LoginRequestDTO(
        @Schema(description = "E-mail cadastrado", example = "camila.duarte@ollipet.com")
        @NotBlank(message = "E-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @Schema(description = "Senha", example = "123456")
        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {
}
