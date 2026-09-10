package org.fiap.challenge_clyvo.service;

public record ResumoDeAderencia(
        int total,
        int administradas,
        int perdidas,
        int pendentes,
        int percentual
) {
}
