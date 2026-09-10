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

/** Texto de cuidado exibido ao tutor, variando por queixa e por classificacao. */
@Entity
@Table(name = "orientacao_triagem")
@NoArgsConstructor
@Getter
@Setter
public class OrientacaoTriagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orientacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_queixa", nullable = false)
    private Queixa queixa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassificacaoTriagem classificacao;

    @Column(nullable = false, length = 255)
    private String texto;

    @Column(nullable = false)
    private Integer ordem;
}
