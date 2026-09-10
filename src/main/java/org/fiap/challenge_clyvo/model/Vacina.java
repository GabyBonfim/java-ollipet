package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Catalogo de vacinas. Cada vacina carrega o proprio protocolo, entao o calculo de
 * vencimento nao precisa de nenhum "if" por nome de vacina espalhado nos servicos.
 */
@Entity
@Table(name = "vacina")
@NoArgsConstructor
@Getter
@Setter
public class Vacina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacina")
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Especie especie;

    /** Quantidade de doses da serie inicial (ex.: V10 = 3). */
    @Column(name = "doses_protocolo", nullable = false)
    private Integer dosesProtocolo;

    /** Dias de espera entre duas doses da serie inicial. */
    @Column(name = "intervalo_dias", nullable = false)
    private Integer intervaloDias;

    /** Periodicidade do reforco depois da serie concluida. Nulo = sem reforco. */
    @Column(name = "meses_reforco")
    private Integer mesesReforco;

    /**
     * Data em que a proxima dose vence, ou {@code null} quando o protocolo se encerra.
     * Dentro da serie inicial vale o intervalo em dias; concluida a serie, vale o reforco.
     */
    public LocalDate calcularVencimentoAposDose(int numeroDose, LocalDate dataAplicacao) {
        if (numeroDose < dosesProtocolo) {
            return dataAplicacao.plusDays(intervaloDias);
        }
        return mesesReforco == null ? null : dataAplicacao.plusMonths(mesesReforco);
    }

    public boolean serieConcluidaCom(int numeroDose) {
        return numeroDose >= dosesProtocolo;
    }
}
