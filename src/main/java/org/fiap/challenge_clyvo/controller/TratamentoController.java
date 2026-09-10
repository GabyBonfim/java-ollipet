package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.DoseTratamentoDTO;
import org.fiap.challenge_clyvo.dto.EncerramentoTratamentoDTO;
import org.fiap.challenge_clyvo.dto.TratamentoRequestDTO;
import org.fiap.challenge_clyvo.dto.TratamentoResponseDTO;
import org.fiap.challenge_clyvo.service.TratamentoService;
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

@Tag(name = "Tratamentos", description = "Prescricao para casa e controle de adesao")
@RestController
@RequestMapping("/api/v1/tratamentos")
public class TratamentoController {
    private final TratamentoService tratamentoService;

    public TratamentoController(TratamentoService tratamentoService) {
        this.tratamentoService = tratamentoService;
    }

    @Operation(summary = "Prescrever tratamento",
            description = "A API transforma intervalo e duracao em horarios concretos. O cliente nao "
                    + "envia a lista de doses: ela e calculada aqui.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tratamento prescrito com o plano de doses"),
            @ApiResponse(responseCode = "403", description = "Pet ou consulta de outro profissional"),
            @ApiResponse(responseCode = "409", description = "Medicamento ja em uso, plano fora dos limites "
                    + "ou consulta nao concluida")
    })
    @PreAuthorize("hasRole('VETERINARIO')")
    @PostMapping
    public ResponseEntity<TratamentoResponseDTO> prescrever(@Valid @RequestBody TratamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tratamentoService.prescrever(dto));
    }

    @Operation(summary = "Doses de hoje",
            description = "Checklist do dia somando todos os pets do tutor. E a tela principal do fluxo "
                    + "e a origem natural do lembrete por notificacao.")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/hoje")
    public ResponseEntity<List<DoseTratamentoDTO>> dosesDeHoje() {
        return ResponseEntity.ok(tratamentoService.dosesDeHoje());
    }

    @Operation(summary = "Confirmar que a dose foi dada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dose confirmada"),
            @ApiResponse(responseCode = "403", description = "A dose e de um pet de outro tutor"),
            @ApiResponse(responseCode = "409", description = "Dose futura, ja confirmada, ou tratamento encerrado")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @PatchMapping("/doses/{doseId}/confirmar")
    public ResponseEntity<DoseTratamentoDTO> confirmarDose(@PathVariable Long doseId) {
        return ResponseEntity.ok(tratamentoService.confirmarDose(doseId));
    }

    @Operation(summary = "Buscar tratamento por ID", description = "Traz o plano completo e a adesao apurada.")
    @GetMapping("/{id}")
    public ResponseEntity<TratamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tratamentoService.buscarPorId(id));
    }

    @Operation(summary = "Tratamentos de um pet")
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<TratamentoResponseDTO>> historicoDoPet(@PathVariable Long petId) {
        return ResponseEntity.ok(tratamentoService.historicoDoPet(petId));
    }

    @Operation(summary = "Encerrar tratamento concluido")
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<TratamentoResponseDTO> encerrar(@PathVariable Long id,
                                                          @Valid @RequestBody EncerramentoTratamentoDTO dto) {
        return ResponseEntity.ok(tratamentoService.encerrar(id, dto));
    }

    @Operation(summary = "Interromper tratamento antes do previsto")
    @PreAuthorize("hasRole('VETERINARIO')")
    @PatchMapping("/{id}/interromper")
    public ResponseEntity<TratamentoResponseDTO> interromper(@PathVariable Long id,
                                                             @Valid @RequestBody EncerramentoTratamentoDTO dto) {
        return ResponseEntity.ok(tratamentoService.interromper(id, dto));
    }

    @Operation(summary = "Tratamentos com adesao baixa",
            description = "Pets que estao deixando doses passar, para a clinica intervir antes do retorno.")
    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping("/aderencia-baixa")
    public ResponseEntity<List<TratamentoResponseDTO>> aderenciaBaixa() {
        return ResponseEntity.ok(tratamentoService.aderenciaBaixa());
    }
}
