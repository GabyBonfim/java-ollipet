package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fiap.challenge_clyvo.dto.ProntuarioResponseDTO;
import org.fiap.challenge_clyvo.service.ProntuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Prontuarios sao apenas consultados: eles nascem automaticamente quando o veterinario
 * conclui um atendimento, e nao por cadastro manual.
 */
@Tag(name = "Prontuarios", description = "Historico clinico dos pets")
@RestController
@RequestMapping("/api/v1/prontuarios")
public class ProntuarioController {
    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @Operation(summary = "Buscar prontuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProntuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(prontuarioService.buscarPorId(id));
    }

    @Operation(summary = "Historico clinico de um pet")
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<ProntuarioResponseDTO>> historicoDoPet(@PathVariable Long petId) {
        return ResponseEntity.ok(prontuarioService.historicoDoPet(petId));
    }

    @Operation(summary = "Prontuarios que eu registrei")
    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping("/meus")
    public ResponseEntity<Page<ProntuarioResponseDTO>> meus(
            @PageableDefault(size = 10, sort = "dataProcedimento", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(prontuarioService.listarDoVeterinarioLogado(pageable));
    }
}
