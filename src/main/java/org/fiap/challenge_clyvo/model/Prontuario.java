package org.fiap.challenge_clyvo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "prontuario")
@NoArgsConstructor
@Getter
@Setter
public class Prontuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prontuario")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    @JsonIgnoreProperties({"prontuarios", "responsavel"})
    private Pet pet;

    @Column(nullable = false, length = 255)
    private String procedimento;

    @Column(name = "data_procedimento", nullable = false)
    private LocalDate dataProcedimento;

    @Column(name = "local_atendimento", nullable = false, length = 120)
    private String localAtendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_med_vet", nullable = false)
    @JsonIgnoreProperties({"pets", "prontuarios"})
    private Veterinario veterinario;

    /** Preenchido quando o prontuario nasce do encerramento de uma consulta agendada. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", unique = true)
    @JsonIgnoreProperties({"pet", "veterinario"})
    private Consulta consulta;
}
