package org.fiap.challenge_clyvo.model;

/**
 * Especies atendidas pela clinica.
 *
 * A coluna no banco e VARCHAR, entao acrescentar um valor aqui nao exige
 * migration. O que muda de fato sao as regras que consultam a especie:
 *
 * - o catalogo de vacinas so tem itens para CAO e GATO; as demais especies
 *   ficam com a carteira vazia ate que vacinas sejam cadastradas para elas;
 * - as queixas de triagem do protocolo inicial valem para todas as especies
 *   (especie nula), entao um roedor ou uma ave respondem o mesmo questionario;
 * - o ajuste de jejum prolongado e exclusivo de GATO, por causa do risco de
 *   lipidose hepatica, e simplesmente nao se aplica as outras.
 */
public enum Especie {
    CAO,
    GATO,
    AVE,
    ROEDOR,
    LAGOMORFO,
    REPTIL,
    EQUINO,
    OUTRO
}
