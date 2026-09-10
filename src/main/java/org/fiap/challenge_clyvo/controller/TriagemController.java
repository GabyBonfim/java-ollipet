package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.ProtocoloTriagemDTO;
import org.fiap.challenge_clyvo.dto.QueixaResumoDTO;
import org.fiap.challenge_clyvo.dto.TriagemFilaDTO;
import org.fiap.challenge_clyvo.dto.TriagemRequestDTO;
import org.fiap.challenge_clyvo.dto.TriagemResponseDTO;
import org.fiap.challenge_clyvo.service.TriagemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Triagem", description = "Questionario de urgencia que antecede o agendamento")
@RestController
@RequestMapping("/api/v1/triagem")
public class TriagemController {
    private final TriagemService triagemService;

    public TriagemController(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    @Operation(summary = "Queixas disponiveis para um pet",
            description = "Filtra o catalogo pela especie do pet. Primeira tela da triagem.")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/queixas/pet/{petId}")
    public ResponseEntity<List<QueixaResumoDTO>> queixas(@PathVariable Long petId) {
        return ResponseEntity.ok(triagemService.queixasPara(petId));
    }

    @Operation(summary = "Protocolo de uma queixa",
            description = "Perguntas e opcoes para o aplicativo montar o formulario. Os pesos de cada "
                    + "resposta nao sao expostos: a pontuacao e responsabilidade do servidor.")
    @GetMapping("/protocolos/{queixaId}")
    public ResponseEntity<ProtocoloTriagemDTO> protocolo(@PathVariable Long queixaId) {
        return ResponseEntity.ok(triagemService.protocoloDe(queixaId));
    }

    @Operation(summary = "Enviar respostas e obter a classificacao",
            description = "A API soma os pesos, aplica os sinais de alerta e cruza com o cadastro do pet "
                    + "(idade, vacinacao em atraso, atendimento recente) antes de classificar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Triagem classificada"),
            @ApiResponse(responseCode = "403", description = "O pet pertence a outro tutor"),
            @ApiResponse(responseCode = "409", description = "Respostas inconsistentes ou queixa incompativel"),
            @ApiResponse(responseCode = "429", description = "Limite de triagens por pet em 24h atingido")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @PostMapping
    public ResponseEntity<TriagemResponseDTO> registrar(@Valid @RequestBody TriagemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(triagemService.registrar(dto));
    }

    @Operation(summary = "Refazer a triagem com novas respostas",
            description = "Reavalia a triagem do zero com as respostas enviadas. A classificacao pode "
                    + "mudar. Nao se aplica a triagem que ja originou uma consulta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Triagem reavaliada"),
            @ApiResponse(responseCode = "403", description = "A triagem pertence a outro tutor"),
            @ApiResponse(responseCode = "404", description = "Triagem nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Triagem ja encaminhada ou respostas inconsistentes")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @PutMapping("/{id}")
    public ResponseEntity<TriagemResponseDTO> refazer(@PathVariable Long id,
                                                      @Valid @RequestBody TriagemRequestDTO dto) {
        return ResponseEntity.ok(triagemService.refazer(id, dto));
    }

    @Operation(summary = "Excluir uma triagem do historico",
            description = "Remove a triagem. Nao se aplica a triagem que ja originou uma consulta, "
                    + "por ser a justificativa clinica do agendamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Triagem excluida"),
            @ApiResponse(responseCode = "403", description = "A triagem pertence a outro tutor"),
            @ApiResponse(responseCode = "404", description = "Triagem nao encontrada"),
            @ApiResponse(responseCode = "409", description = "Triagem ja encaminhada a uma consulta")
    })
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        triagemService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar triagem por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TriagemResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(triagemService.buscarPorId(id));
    }

    @Operation(summary = "Historico de triagens dos meus pets")
    @PreAuthorize("hasRole('RESPONSAVEL')")
    @GetMapping("/minhas")
    public ResponseEntity<Page<TriagemResponseDTO>> minhas(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(triagemService.minhasTriagens(pageable));
    }

    @Operation(summary = "Fila de triagens graves",
            description = "Triagens classificadas como emergencia ou urgente que ainda nao viraram "
                    + "consulta e continuam dentro do prazo de validade.")
    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping("/fila")
    public ResponseEntity<List<TriagemFilaDTO>> fila() {
        return ResponseEntity.ok(triagemService.filaDaClinica());
    }
}
