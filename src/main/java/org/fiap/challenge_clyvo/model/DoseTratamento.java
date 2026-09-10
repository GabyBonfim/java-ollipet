package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fiap.challenge_clyvo.exception.BusinessException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "dose_tratamento")
@NoArgsConstructor
@Getter
@Setter
public class DoseTratamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dose")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tratamento", nullable = false)
    private Tratamento tratamento;

    @Column(name = "numero_dose", nullable = false)
    private Integer numeroDose;

    @Column(name = "horario_previsto", nullable = false)
    private LocalDateTime horarioPrevisto;

    /** Nulo enquanto o tutor nao confirmar que deu o medicamento. */
    @Column(name = "confirmado_em")
    private LocalDateTime confirmadoEm;

    /**
     * A dose vira PERDIDA sozinha quando o horario previsto mais a tolerancia passa sem
     * confirmacao. Nada e gravado para isso acontecer.
     */
    public StatusDose situacaoEm(LocalDateTime agora, Duration tolerancia) {
        if (confirmadoEm != null) {
            return StatusDose.ADMINISTRADA;
        }
        return agora.isAfter(horarioPrevisto.plus(tolerancia)) ? StatusDose.PERDIDA : StatusDose.PENDENTE;
    }

    /** Quanto tempo depois do previsto o tutor confirmou. Zero quando deu no horario. */
    public long atrasoEmMinutos() {
        if (confirmadoEm == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.MINUTES.between(horarioPrevisto, confirmadoEm));
    }

    public void confirmar(LocalDateTime agora) {
        if (confirmadoEm != null) {
            throw new BusinessException("A dose %d ja foi confirmada em %s".formatted(numeroDose, confirmadoEm));
        }
        if (horarioPrevisto.isAfter(agora)) {
            throw new BusinessException("A dose %d so pode ser confirmada a partir de %s"
                    .formatted(numeroDose, horarioPrevisto));
        }
        this.confirmadoEm = agora;
    }
}
