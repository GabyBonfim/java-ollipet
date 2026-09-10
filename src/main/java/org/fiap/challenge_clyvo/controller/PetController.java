package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.PetRequestDTO;
import org.fiap.challenge_clyvo.dto.PetResponseDTO;
import org.fiap.challenge_clyvo.service.PetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pets", description = "Cadastro de pets")
@RestController
@RequestMapping("/api/v1/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @Operation(summary = "Cadastrar pet", description = "O pet e vinculado automaticamente ao tutor autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @PostMapping
    public ResponseEntity<PetResponseDTO> salvar(@Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.salvar(dto));
    }

    @Operation(summary = "Meus pets", description = "Pets do tutor autenticado.")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/meus")
    public ResponseEntity<List<PetResponseDTO>> meusPets() {
        return ResponseEntity.ok(petService.listarDoTutorLogado());
    }

    @Operation(summary = "Buscar pet por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet encontrado"),
            @ApiResponse(responseCode = "403", description = "O pet pertence a outro tutor"),
            @ApiResponse(responseCode = "404", description = "Pet nao encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(petService.buscarPorId(id));
    }

    @Operation(summary = "Listar pets da clinica", description = "Visao completa, exclusiva da equipe clinica.")
    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping
    public ResponseEntity<Page<PetResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String raca,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        if (nome != null) {
            return ResponseEntity.ok(petService.buscarPorNome(nome, pageable));
        }
        if (raca != null) {
            return ResponseEntity.ok(petService.buscarPorRaca(raca, pageable));
        }
        return ResponseEntity.ok(petService.listarTodos(pageable));
    }

    @Operation(summary = "Atualizar pet")
    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(petService.atualizar(id, dto));
    }

    @Operation(summary = "Remover pet")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
