package org.fiap.challenge_clyvo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opcao_resposta")
@NoArgsConstructor
@Getter
@Setter
public class OpcaoResposta {
    @Id
    @Column(name = "id_opcao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pergunta", nullable = false)
    private PerguntaTriagem pergunta;

    @Column(nullable = false, length = 120)
    private String texto;

    /** Quanto esta resposta soma na pontuacao. Nunca sai para o aplicativo. */
    @Column(nullable = false)
    private Integer peso = 0;

    /**
     * Resposta que descreve um quadro potencialmente fatal. Uma unica marcada leva a
     * triagem direto para EMERGENCIA, sem depender da soma dos pesos.
     */
    @Column(name = "sinal_alerta", nullable = false)
    private boolean sinalAlerta;

    /**
     * Rotulo semantico para regras que dependem desta resposta especifica, evitando
     * que o codigo compare id de opcao.
     */
    @Column(length = 40)
    private String marcador;

    @Column(nullable = false)
    private Integer ordem;

    public boolean pertenceA(PerguntaTriagem outra) {
        return pergunta.getId().equals(outra.getId());
    }
}
