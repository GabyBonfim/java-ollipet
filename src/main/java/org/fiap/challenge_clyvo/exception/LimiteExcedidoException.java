package org.fiap.challenge_clyvo.exception;

/** Uso legitimo, porem acima do volume aceito em uma janela de tempo. */
public class LimiteExcedidoException extends RuntimeException {
    public LimiteExcedidoException(String message) {
        super(message);
    }
}
