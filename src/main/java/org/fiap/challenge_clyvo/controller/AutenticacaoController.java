package org.fiap.challenge_clyvo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.fiap.challenge_clyvo.dto.LoginRequestDTO;
import org.fiap.challenge_clyvo.dto.LoginResponseDTO;
import org.fiap.challenge_clyvo.dto.RegistroFirebaseDTO;
import org.fiap.challenge_clyvo.service.AutenticacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticacao", description = "Emissao de tokens e vinculo de contas do Firebase")
@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacaoController {
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @Operation(summary = "Login com e-mail e senha",
            description = "Emite um token da propria API. Usado pelo Swagger, pelo Insomnia e por "
                    + "clientes que nao passam pelo Firebase.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido"),
            @ApiResponse(responseCode = "401", description = "Credenciais invalidas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(autenticacaoService.autenticar(dto));
    }

    @Operation(summary = "Vincular a conta do Firebase ao cadastro da clinica",
            description = "Chamado pelo app logo apos o cadastro no Firebase Authentication, enviando o "
                    + "ID token no header Authorization. Nome e e-mail vem do token; o app so informa "
                    + "os dados que o Firebase nao tem. A chamada e idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastro criado ou ja existente"),
            @ApiResponse(responseCode = "401", description = "Token do Firebase ausente ou invalido"),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail ja usados por outro cadastro")
    })
    @PostMapping("/registrar")
    public ResponseEntity<LoginResponseDTO> registrar(@Valid @RequestBody RegistroFirebaseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(autenticacaoService.registrarContaDoFirebase(dto));
    }
}
