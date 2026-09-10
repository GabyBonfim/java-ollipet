package org.fiap.challenge_clyvo.model;

/**
 * Agrupa as queixas por sistema afetado. E o que permite a API aplicar regras clinicas
 * sem depender do id de uma pergunta especifica — por exemplo, cruzar qualquer queixa
 * gastrointestinal com o atraso de vacinacao do pet.
 */
public enum CategoriaQueixa {
    GASTROINTESTINAL,
    DERMATOLOGICO,
    ALIMENTAR,
    RESPIRATORIO
}
