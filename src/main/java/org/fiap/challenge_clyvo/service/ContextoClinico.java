package org.fiap.challenge_clyvo.service;

import java.util.List;

/**
 * O que a API sabe sobre o pet alem das respostas do questionario. E justamente esta
 * informacao que o aplicativo nao tem, e que faz a mesma triagem resultar em
 * classificacoes diferentes para pets diferentes.
 */
public record ContextoClinico(
        List<String> vacinasAtrasadas,
        List<String> medicamentosEmUso,
        boolean possuiConsultaRecenteConcluida
) {
    public boolean temVacinaAtrasada() {
        return !vacinasAtrasadas.isEmpty();
    }

    public boolean estaEmTratamento() {
        return !medicamentosEmUso.isEmpty();
    }
}
