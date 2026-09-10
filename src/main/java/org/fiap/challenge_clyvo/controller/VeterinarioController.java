package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.NovoVeterinarioDTO;
import org.fiap.challenge_clyvo.dto.VeterinarioDTO;
import org.fiap.challenge_clyvo.service.VeterinarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Somente leitura: a equipe clinica e cadastrada pela migration do Flyway, nao pela API.
 */
@Tag(name = "Veterinarios", description = "Consulta da equipe clinica")
@RestController
@RequestMapping("/api/v1/veterinarios")
public class VeterinarioController {
    private final VeterinarioService veterinarioService;

    public VeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @Operation(summary = "Cadastrar um veterinario na equipe",
            description = "Exclusivo da administracao da clinica. Nao define senha: o acesso "
                    + "acontece pelo Firebase, e o vinculo e feito pelo e-mail no primeiro login.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veterinario cadastrado"),
            @ApiResponse(responseCode = "403", description = "Apenas a administracao pode cadastrar"),
            @ApiResponse(responseCode = "409", description = "E-mail, CPF ou CRMV ja utilizados")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<VeterinarioDTO> cadastrar(@Valid @RequestBody NovoVeterinarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinarioService.cadastrar(dto));
    }

    @Operation(summary = "Remover um veterinario da equipe",
            description = "Desativa o cadastro: as consultas e prontuarios que ele atendeu "
                    + "permanecem no historico, mas ele deixa de aparecer para agendamento "
                    + "e perde o acesso ao aplicativo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veterinario removido da equipe"),
            @ApiResponse(responseCode = "403", description = "Apenas a administracao pode remover"),
            @ApiResponse(responseCode = "404", description = "Veterinario nao encontrado"),
            @ApiResponse(responseCode = "409", description = "O veterinario ja esta inativo")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        veterinarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verificar se um e-mail pertence a equipe clinica",
            description = "Consultado pela tela de primeiro acesso antes de criar a conta no "
                    + "Firebase: so quem a administracao cadastrou pode se tornar veterinario "
                    + "no aplicativo. Responde apenas true ou false, sem expor dados do cadastro.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada")
    @GetMapping("/e-da-equipe")
    public ResponseEntity<Boolean> ehDaEquipe(@RequestParam String email) {
        return ResponseEntity.ok(veterinarioService.ehVeterinarioCadastrado(email));
    }

    @Operation(summary = "Veterinarios disponiveis para agendamento",
            description = "Lista enxuta que o tutor usa na hora de escolher com quem marcar.")
    @GetMapping("/disponiveis")
    public ResponseEntity<List<VeterinarioDTO>> disponiveis() {
        return ResponseEntity.ok(veterinarioService.listarDisponiveis());
    }

    @Operation(summary = "Buscar veterinario por ID")
    @PreAuthorize("hasAnyRole('VETERINARIO', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(veterinarioService.buscarPorId(id));
    }

    @Operation(summary = "Listar veterinarios")
    @PreAuthorize("hasAnyRole('VETERINARIO', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<VeterinarioDTO>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        return ResponseEntity.ok(nome == null
                ? veterinarioService.listarTodos(pageable)
                : veterinarioService.buscarPorNome(nome, pageable));
    }
}
