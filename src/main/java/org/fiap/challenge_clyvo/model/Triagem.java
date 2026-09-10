package org.fiap.challenge_clyvo.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "triagem")
@NoArgsConstructor
@Getter
@Setter
public class Triagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_triagem")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_queixa", nullable = false)
    private Queixa queixa;

    @Column(nullable = false)
    private Integer pontuacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassificacaoTriagem classificacao;

    /**
     * Gravada em vez de deduzida da classificacao: ela depende do historico do pet no
     * momento da triagem, entao recalcular depois daria outro resultado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "acao_sugerida", nullable = false, length = 20)
    private AcaoSugerida acaoSugerida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTriagem status = StatusTriagem.CLASSIFICADA;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    /** Preenchida quando a triagem vira um agendamento. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", unique = true)
    private Consulta consulta;

    @OneToMany(mappedBy = "triagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaTriagem> respostas = new ArrayList<>();

    @OneToMany(mappedBy = "triagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObservacaoTriagem> observacoes = new ArrayList<>();

    public void adicionarResposta(PerguntaTriagem pergunta, OpcaoResposta opcao) {
        RespostaTriagem resposta = new RespostaTriagem();
        resposta.setTriagem(this);
        resposta.setPergunta(pergunta);
        resposta.setOpcao(opcao);
        respostas.add(resposta);
    }

    public void adicionarObservacao(String texto) {
        ObservacaoTriagem observacao = new ObservacaoTriagem();
        observacao.setTriagem(this);
        observacao.setTexto(texto);
        observacoes.add(observacao);
    }

    public boolean estaExpirada() {
        return LocalDateTime.now().isAfter(expiraEm);
    }

    public boolean pertenceA(Responsavel responsavel) {
        return pet.getResponsavel().getId().equals(responsavel.getId());
    }

    /** Aguardando providencia: ainda nao virou consulta e ainda vale. */
    public boolean estaPendente() {
        return status == StatusTriagem.CLASSIFICADA && !estaExpirada();
    }

    public void vincularA(Consulta consultaGerada) {
        this.consulta = consultaGerada;
        this.status = StatusTriagem.ENCAMINHADA;
    }
}
