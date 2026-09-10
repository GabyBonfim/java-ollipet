package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pergunta_triagem")
@NoArgsConstructor
@Getter
@Setter
public class PerguntaTriagem {
    @Id
    @Column(name = "id_pergunta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_queixa", nullable = false)
    private Queixa queixa;

    @Column(nullable = false, length = 255)
    private String texto;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false)
    private boolean obrigatoria = true;

    /**
     * Quando preenchido, a pergunta so deve ser exibida — e so e cobrada — se esta
     * opcao tiver sido escolhida em outra pergunta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcao_dependencia")
    private OpcaoResposta opcaoDependencia;

    @OneToMany(mappedBy = "pergunta")
    @OrderBy("ordem ASC")
    private List<OpcaoResposta> opcoes = new ArrayList<>();

    public boolean exigidaCom(List<Long> opcoesEscolhidas) {
        if (!obrigatoria) {
            return false;
        }
        return opcaoDependencia == null || opcoesEscolhidas.contains(opcaoDependencia.getId());
    }
}
