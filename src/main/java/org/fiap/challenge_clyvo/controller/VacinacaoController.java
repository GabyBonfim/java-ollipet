package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.AplicacaoVacinaRequestDTO;
import org.fiap.challenge_clyvo.dto.AplicacaoVacinaResponseDTO;
import org.fiap.challenge_clyvo.dto.CarteiraVacinacaoDTO;
import org.fiap.challenge_clyvo.dto.PendenciaVacinaDTO;
import org.fiap.challenge_clyvo.dto.VacinaDTO;
import org.fiap.challenge_clyvo.model.Especie;
import org.fiap.challenge_clyvo.service.VacinacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Vacinacao", description = "Carteira de vacinacao, protocolo de doses e pendencias")
@RestController
@RequestMapping("/api/v1/vacinacao")
public class VacinacaoController {
    private final VacinacaoService vacinacaoService;

    public VacinacaoController(VacinacaoService vacinacaoService) {
        this.vacinacaoService = vacinacaoService;
    }

    @Operation(summary = "Registrar aplicacao de dose",
            description = "O numero da dose e o vencimento da proxima sao calculados pelo protocolo da "
                    + "vacina; o cliente nao envia nem um nem outro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dose registrada"),
            @ApiResponse(responseCode = "409", description = "Especie incompativel, protocolo concluido "
                    + "ou intervalo minimo nao respeitado")
    })
    @PreAuthorize("hasRole('VETERINARIO')")
    @PostMapping("/pets/{petId}/doses")
    public ResponseEntity<AplicacaoVacinaResponseDTO> registrar(@PathVariable Long petId,
                                                                @Valid @RequestBody AplicacaoVacinaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacinacaoService.registrarAplicacao(petId, dto));
    }

    @Operation(summary = "Carteira de vacinacao do pet",
            description = "Uma linha por vacina prevista para a especie, com a situacao consolidada.")
    @GetMapping("/pets/{petId}/carteira")
    public ResponseEntity<CarteiraVacinacaoDTO> carteira(@PathVariable Long petId) {
        return ResponseEntity.ok(vacinacaoService.carteiraDoPet(petId));
    }

    @Operation(summary = "Historico de doses aplicadas no pet")
    @GetMapping("/pets/{petId}/doses")
    public ResponseEntity<List<AplicacaoVacinaResponseDTO>> historico(@PathVariable Long petId) {
        return ResponseEntity.ok(vacinacaoService.historicoDoPet(petId));
    }

    @Operation(summary = "Doses vencidas ou proximas do vencimento",
            description = "Fonte natural para o disparo de notificacao push pelo aplicativo.")
    @PreAuthorize("hasRole('VETERINARIO')")
    @GetMapping("/pendencias")
    public ResponseEntity<List<PendenciaVacinaDTO>> pendencias() {
        return ResponseEntity.ok(vacinacaoService.pendencias());
    }

    @Operation(summary = "Catalogo de vacinas por especie")
    @GetMapping("/catalogo/{especie}")
    public ResponseEntity<List<VacinaDTO>> catalogo(@PathVariable Especie especie) {
        return ResponseEntity.ok(vacinacaoService.catalogoPara(especie));
    }
}
