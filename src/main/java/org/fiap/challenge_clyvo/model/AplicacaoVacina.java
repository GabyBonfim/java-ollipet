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

import java.time.LocalDate;

@Entity
@Table(name = "aplicacao_vacina")
@NoArgsConstructor
@Getter
@Setter
public class AplicacaoVacina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aplicacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vacina", nullable = false)
    private Vacina vacina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_med_vet", nullable = false)
    private Veterinario veterinario;

    @Column(name = "numero_dose", nullable = false)
    private Integer numeroDose;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    /** Nulo quando o protocolo da vacina se encerrou nesta dose. */
    @Column(name = "proxima_dose")
    private LocalDate proximaDose;

    @Column(length = 40)
    private String lote;
}
