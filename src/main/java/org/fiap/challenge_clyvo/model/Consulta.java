package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.fiap.challenge_clyvo.exception.TransicaoInvalidaException;

import java.time.LocalDateTime;

@Entity
@Table(name = "consulta")
@NoArgsConstructor
@Getter
@Setter
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_med_vet", nullable = false)
    private Veterinario veterinario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.SOLICITADA;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    /**
     * Unico ponto de mudanca de estado da consulta: rejeita qualquer salto que o
     * grafo de {@link StatusConsulta} nao autorize.
     */
    public void moverPara(StatusConsulta destino) {
        if (!status.permiteTransicaoPara(destino)) {
            throw new TransicaoInvalidaException(status, destino);
        }
        this.status = destino;
        this.atualizadoEm = LocalDateTime.now();
    }

    public boolean pertenceA(Responsavel responsavel) {
        return pet.getResponsavel().getId().equals(responsavel.getId());
    }

    public boolean ehDoVeterinario(Veterinario outro) {
        return veterinario.getId().equals(outro.getId());
    }
}
