package org.fiap.challenge_clyvo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "responsavel")
@DiscriminatorValue("RESPONSAVEL")
@PrimaryKeyJoinColumn(name = "id_usuario")
@NoArgsConstructor
@Getter
@Setter
public class Responsavel extends Usuario {
    @Column(name = "data_nasc")
    private LocalDate dataNascimento;

    @OneToMany(mappedBy = "responsavel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Pet> pets = new ArrayList<>();

    @Override
    public Role getRole() {
        return Role.RESPONSAVEL;
    }

    public boolean ehDonoDe(Pet pet) {
        return pet.getResponsavel() != null && pet.getResponsavel().getId().equals(getId());
    }
}
