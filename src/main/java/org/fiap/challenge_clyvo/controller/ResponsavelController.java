package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fiap.challenge_clyvo.dto.ResponsavelDTO;
import org.fiap.challenge_clyvo.service.ResponsavelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Somente leitura: o cadastro do tutor nasce em POST /api/v1/auth/registrar, a partir
 * da conta do Firebase criada no aplicativo.
 */
@Tag(name = "Tutores", description = "Consulta dos responsaveis pelos pets")
@RestController
@RequestMapping("/api/v1/responsaveis")
public class ResponsavelController {
    private final ResponsavelService responsavelService;

    public ResponsavelController(ResponsavelService responsavelService) {
        this.responsavelService = responsavelService;
    }

    @Operation(summary = "Meu perfil", description = "Dados do tutor autenticado.")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/eu")
    public ResponseEntity<ResponsavelDTO> meuPerfil() {
        return ResponseEntity.ok(responsavelService.meuPerfil());
    }

    @Operation(summary = "Buscar tutor por ID")
    @PreAuthorize("hasAnyRole('VETERINARIO', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponsavelDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(responsavelService.buscarPorId(id));
    }

    @Operation(summary = "Listar tutores")
    @PreAuthorize("hasAnyRole('VETERINARIO', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ResponsavelDTO>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        return ResponseEntity.ok(nome == null
                ? responsavelService.listarTodos(pageable)
                : responsavelService.buscarPorNome(nome, pageable));
    }
}
