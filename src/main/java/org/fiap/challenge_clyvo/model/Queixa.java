package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Motivo pelo qual o tutor abriu a triagem. Os limites de pontuacao ficam aqui, e nao
 * no codigo, porque cada queixa tem uma sensibilidade diferente: um problema de pele
 * quase nunca e urgente, um problema respiratorio quase sempre e.
 */
@Entity
@Table(name = "queixa")
@NoArgsConstructor
@Getter
@Setter
public class Queixa {
    @Id
    @Column(name = "id_queixa")
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaQueixa categoria;

    /** Nulo quando a queixa vale para todas as especies. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Especie especie;

    @Column(name = "limite_urgente", nullable = false)
    private Integer limiteUrgente;

    @Column(name = "limite_pouco_urgente", nullable = false)
    private Integer limitePoucoUrgente;

    @Column(nullable = false)
    private Integer ordem;

    @OneToMany(mappedBy = "queixa")
    @OrderBy("ordem ASC")
    private List<PerguntaTriagem> perguntas = new ArrayList<>();

    public boolean aplicaSeA(Especie especieDoPet) {
        return especie == null || especie == especieDoPet;
    }

    public ClassificacaoTriagem classificar(int pontuacao) {
        if (pontuacao >= limiteUrgente) {
            return ClassificacaoTriagem.URGENTE;
        }
        if (pontuacao >= limitePoucoUrgente) {
            return ClassificacaoTriagem.POUCO_URGENTE;
        }
        return ClassificacaoTriagem.ORIENTACAO;
    }
}
