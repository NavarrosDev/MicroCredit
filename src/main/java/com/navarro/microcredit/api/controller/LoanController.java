package com.navarro.microcredit.api.controller;

import com.navarro.microcredit.api.dto.LoanRequestDTO;
import com.navarro.microcredit.api.dto.LoanResponseDTO;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Solicita um novo empréstimo",
            description = "Cria um empréstimo com o status PENDING e envia para processamento assíncrono."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empréstimo solicitado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> loan(@RequestBody @Valid LoanRequestDTO request) {
        Loan loan = loanService.requestLoan(
                request.clientId(),
                request.requestedAmount(),
                request.installments()
        );
        LoanResponseDTO response = LoanResponseDTO.toDto(loan);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
