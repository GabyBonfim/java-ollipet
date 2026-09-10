package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fiap.challenge_clyvo.dto.ConsultaResponseDTO;
import org.fiap.challenge_clyvo.service.ConsultaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Agenda", description = "Visao da clinica sobre os proprios atendimentos")
@RestController
@RequestMapping("/api/v1/agenda")
@PreAuthorize("hasRole('VETERINARIO')")
public class AgendaController {
    private final ConsultaService consultaService;

    public AgendaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @Operation(summary = "Agenda de um dia", description = "Atendimentos do veterinario autenticado na data informada.")
    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> doDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(consultaService.agendaDoDia(data == null ? LocalDate.now() : data));
    }

    @Operation(summary = "Solicitacoes aguardando confirmacao")
    @GetMapping("/pendentes")
    public ResponseEntity<Page<ConsultaResponseDTO>> pendentes(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(consultaService.solicitacoesPendentes(pageable));
    }
}
