package com.navarro.microcredit.api.controller;

import com.navarro.microcredit.api.dto.*;
import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @Operation(
            summary = "Busca de um cliente por cpf.",
            description = "Busca um cpf pelo seu cpf e ja lista todos seus empréstimos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @GetMapping("/{cpf}")
    public ResponseEntity<ClientLoansDTO> getClientByCpf(@PathVariable String cpf) {
        Client client = clientService.getClientByCpf(cpf);
        return ResponseEntity.ok(ClientLoansDTO.toDto(client));
    }

    @Operation(
            summary = "Criação de um novo Cliente",
            description = "Cria um novo cliente e salva no banco de dados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody @Valid ClientRequestDTO request) {
        Client client = clientService.createClient(
                request.cpf(),
                request.name(),
                request.monthlyIncome()
        );

        ClientResponseDTO response =
                new ClientResponseDTO("Cliente criado com sucesso!", ClientDTO.toDto(client));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Deleta um Cliente",
            description = "Deleta um cliente caso não tiver pendencias"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @DeleteMapping("{cpf}")
    public ResponseEntity<String> deleteClient(@PathVariable String cpf) {
        clientService.deleClient(cpf);
        return ResponseEntity.ok("Cliente deletado com sucesso!");
    }
}
