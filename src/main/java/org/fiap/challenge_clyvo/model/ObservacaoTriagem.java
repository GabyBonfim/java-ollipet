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

/**
 * Achado que a API deduziu cruzando as respostas com o cadastro do pet. Fica gravado
 * porque depende do estado do pet no momento da triagem: recalcular semanas depois
 * daria outro resultado.
 */
@Entity
@Table(name = "observacao_triagem")
@NoArgsConstructor
@Getter
@Setter
public class ObservacaoTriagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_observacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_triagem", nullable = false)
    private Triagem triagem;

    @Column(nullable = false, length = 255)
    private String texto;
}
