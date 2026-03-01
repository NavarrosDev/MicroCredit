package com.navarro.microcredit.api.controller;

import com.navarro.microcredit.api.dto.ClientDTO;
import com.navarro.microcredit.api.dto.ClientRequestDTO;
import com.navarro.microcredit.api.dto.ClientResponseDTO;
import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.service.ClientService;
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

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@RequestBody @Valid ClientRequestDTO request) {
        Client client = clientService.createClient(
                request.cpf(),
                request.name(),
                request.monthlyIncome()
        );

        ClientDTO clientDTO = new ClientDTO(
                client.getId(),
                client.getCpf(),
                client.getName(),
                client.getMonthlyIncome()
        );

        ClientResponseDTO response = new ClientResponseDTO("Cliente criado com sucesso!", clientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("{cpf}")
    public ResponseEntity<String> deleteClient(@PathVariable String cpf) {
        clientService.deleClient(cpf);
        return ResponseEntity.ok("Cliente deletado com sucesso!");
    }
}
