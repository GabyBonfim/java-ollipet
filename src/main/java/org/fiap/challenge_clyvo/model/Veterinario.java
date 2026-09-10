package org.fiap.challenge_clyvo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "med_vet")
@DiscriminatorValue("VETERINARIO")
@PrimaryKeyJoinColumn(name = "id_usuario")
@NoArgsConstructor
@Getter
@Setter
public class Veterinario extends Usuario {
    @Column(nullable = false, unique = true, length = 20)
    private String crmv;

    @Column(length = 80)
    private String especialidade;

    @OneToMany(mappedBy = "veterinario", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("veterinario")
    private List<Prontuario> prontuarios = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.VETERINARIO;
    }
}
