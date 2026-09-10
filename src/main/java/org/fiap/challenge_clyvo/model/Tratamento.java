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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fiap.challenge_clyvo.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tratamento")
@NoArgsConstructor
@Getter
@Setter
public class Tratamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tratamento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_med_vet", nullable = false)
    private Veterinario veterinario;

    /** Consulta que originou a prescricao. Nulo em prescricao avulsa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta")
    private Consulta consulta;

    @Column(nullable = false, length = 120)
    private String medicamento;

    @Column(nullable = false, length = 80)
    private String dosagem;

    @Column(name = "intervalo_horas", nullable = false)
    private Integer intervaloHoras;

    @Column(name = "duracao_dias", nullable = false)
    private Integer duracaoDias;

    @Column(name = "inicio_em", nullable = false)
    private LocalDateTime inicioEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTratamento status = StatusTratamento.EM_ANDAMENTO;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "encerrado_em")
    private LocalDateTime encerradoEm;

    @OneToMany(mappedBy = "tratamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroDose ASC")
    private List<DoseTratamento> doses = new ArrayList<>();

    public void adicionarDose(int numeroDose, LocalDateTime horarioPrevisto) {
        DoseTratamento dose = new DoseTratamento();
        dose.setTratamento(this);
        dose.setNumeroDose(numeroDose);
        dose.setHorarioPrevisto(horarioPrevisto);
        doses.add(dose);
    }

    public void encerrar(String observacaoFinal) {
        exigirEmAndamento();
        this.status = StatusTratamento.CONCLUIDO;
        this.encerradoEm = LocalDateTime.now();
        acrescentarObservacao(observacaoFinal);
    }

    public void interromper(String motivo) {
        exigirEmAndamento();
        this.status = StatusTratamento.INTERROMPIDO;
        this.encerradoEm = LocalDateTime.now();
        acrescentarObservacao("Interrompido: " + motivo);
    }

    public LocalDateTime terminaEm() {
        return inicioEm.plusDays(duracaoDias);
    }

    public boolean pertenceA(Responsavel responsavel) {
        return pet.getResponsavel().getId().equals(responsavel.getId());
    }

    private void exigirEmAndamento() {
        if (!status.estaAtivo()) {
            throw new BusinessException("Este tratamento ja esta " + status.getDescricao().toLowerCase());
        }
    }

    private void acrescentarObservacao(String texto) {
        if (texto == null || texto.isBlank()) {
            return;
        }
        this.observacoes = observacoes == null || observacoes.isBlank()
                ? texto
                : observacoes + " | " + texto;
    }
}
