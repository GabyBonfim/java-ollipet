package org.fiap.challenge_clyvo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.fiap.challenge_clyvo.model.Role;

@Schema(description = "Token de acesso emitido pela API")
public record LoginResponseDTO(
        @Schema(description = "Token JWT, enviado no header Authorization")
        String token,

        @Schema(description = "Esquema de autenticacao", example = "Bearer")
        String tipo,

        @Schema(description = "Validade do token em segundos", example = "28800")
        long expiraEmSegundos,

        @Schema(description = "Nome do usuario autenticado", example = "Dra. Camila Duarte")
        String nome,

        @Schema(description = "Perfil de acesso", example = "VETERINARIO")
        Role perfil
) {
    public static LoginResponseDTO bearer(String token, long expiraEmSegundos, String nome, Role perfil) {
        return new LoginResponseDTO(token, "Bearer", expiraEmSegundos, nome, perfil);
    }
}
