package org.fiap.challenge_clyvo.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Schema(description = "Formato padrao de erro da API")
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
    public static ErrorResponseDTO de(HttpStatus status, String message, String path) {
        return new ErrorResponseDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
