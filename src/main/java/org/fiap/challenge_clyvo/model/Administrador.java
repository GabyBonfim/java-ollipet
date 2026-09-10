package org.fiap.challenge_clyvo.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Administracao da clinica.
 *
 * Diferente de Responsavel e Veterinario, nao tem tabela propria: nao existem
 * atributos exclusivos do perfil, apenas os dados comuns a todo usuario. Na
 * heranca JOINED isso e valido — o discriminador em `usuario.tipo` basta para
 * identificar o perfil.
 *
 * E quem cadastra e remove veterinarios, para que essa operacao nao fique
 * aberta a qualquer pessoa que baixe o aplicativo.
 */
@Entity
@Table(name = "administrador")
@DiscriminatorValue("ADMIN")
@PrimaryKeyJoinColumn(name = "id_usuario")
@NoArgsConstructor
@Getter
@Setter
public class Administrador extends Usuario {

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
