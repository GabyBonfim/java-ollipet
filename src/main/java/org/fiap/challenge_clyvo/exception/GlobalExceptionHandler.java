package org.fiap.challenge_clyvo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** Traduz as excecoes da aplicacao no formato de erro unico da API. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponseDTO> handleAcessoNegado(Exception ex, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(LimiteExcedidoException.class)
    public ResponseEntity<ErrorResponseDTO> handleLimite(LimiteExcedidoException ex, HttpServletRequest request) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        String mensagem = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /** Id ou parametro que nao converte para o tipo esperado e erro do cliente, nao do servidor. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "Valor invalido para o parametro '%s': %s".formatted(ex.getName(), ex.getValue()), request);
    }

    /**
     * O Spring MVC ja classifica os proprios erros — rota inexistente, metodo nao
     * permitido, parametro ausente — implementando {@link ErrorResponse}. Sem respeitar
     * esse status, todos eles virariam 500 e esconderiam o motivo real.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse erroClassificado) {
            HttpStatus status = HttpStatus.valueOf(erroClassificado.getStatusCode().value());
            return responder(status, erroClassificado.getBody().getDetail(), request);
        }
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponseDTO> responder(HttpStatus status, String mensagem,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponseDTO.de(status, mensagem, request.getRequestURI()));
    }
}
