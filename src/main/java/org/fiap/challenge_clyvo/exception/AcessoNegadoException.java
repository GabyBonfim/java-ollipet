package org.fiap.challenge_clyvo.exception;

/**
 * Usado quando o usuario esta autenticado e tem o perfil certo, mas o recurso pedido
 * pertence a outra pessoa (ex.: um tutor tentando abrir o pet de outro tutor).
 */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String message) {
        super(message);
    }
}
