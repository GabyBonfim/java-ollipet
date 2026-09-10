package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.CancelamentoConsultaDTO;
import org.fiap.challenge_clyvo.dto.ConsultaRequestDTO;
import org.fiap.challenge_clyvo.dto.ConsultaResponseDTO;
import org.fiap.challenge_clyvo.dto.EncerramentoConsultaDTO;
import org.fiap.challenge_clyvo.service.ConsultaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Consultas", description = "Fluxo de agendamento: solicitar, confirmar, atender e concluir")
@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaController {
    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @Operation(summary = "Solicitar consulta",
            description = "O tutor escolhe pet, veterinario e horario. A consulta nasce como SOLICITADA e "
                    + "so ocupa a agenda depois que o veterinario confirma.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consulta solicitada"),
            @ApiResponse(responseCode = "403", description = "O pet pertence a outro tutor"),
            @ApiResponse(responseCode = "409", description = "Horario fora do expediente ou ja ocupado")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> solicitar(@Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.solicitar(dto));
    }

    @Operation(summary = "Confirmar consulta solicitada")
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ConsultaResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.confirmar(id));
    }

    @Operation(summary = "Iniciar o atendimento (check-in do pet na clinica)")
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<ConsultaResponseDTO> iniciar(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.iniciarAtendimento(id));
    }

    @Operation(summary = "Concluir o atendimento",
            description = "Encerra a consulta e grava o prontuario correspondente na mesma transacao.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta concluida e prontuario gerado"),
            @ApiResponse(responseCode = "409", description = "A consulta nao esta em atendimento")
    })
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ConsultaResponseDTO> concluir(@PathVariable Long id,
                                                        @Valid @RequestBody EncerramentoConsultaDTO dto) {
        return ResponseEntity.ok(consultaService.concluir(id, dto));
    }

    @Operation(summary = "Registrar que o pet nao compareceu")
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/falta")
    public ResponseEntity<ConsultaResponseDTO> registrarFalta(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.registrarFalta(id));
    }

    @Operation(summary = "Cancelar consulta",
            description = "Tutores respeitam a antecedencia minima quando a consulta ja esta confirmada; "
                    + "a clinica pode cancelar a qualquer momento.")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaResponseDTO> cancelar(@PathVariable Long id,
                                                        @Valid @RequestBody CancelamentoConsultaDTO dto) {
        return ResponseEntity.ok(consultaService.cancelar(id, dto));
    }

    @Operation(summary = "Buscar consulta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @Operation(summary = "Consultas dos meus pets")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/minhas")
    public ResponseEntity<Page<ConsultaResponseDTO>> minhas(
            @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(consultaService.minhasConsultas(pageable));
    }

    @Operation(summary = "Historico de consultas de um pet")
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<ConsultaResponseDTO>> historicoDoPet(@PathVariable Long petId) {
        return ResponseEntity.ok(consultaService.historicoDoPet(petId));
    }
}
