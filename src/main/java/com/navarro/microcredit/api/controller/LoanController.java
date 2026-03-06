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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @Operation(
            summary = "Quita o empréstimo",
            description = "Quita um emprestimo, alterando seu status para Quitado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empréstimo quitado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<LoanResponseDTO> payLoan(@PathVariable UUID id) {
        Loan loan = loanService.payLoan(id);
        LoanResponseDTO response = LoanResponseDTO.toDto(loan);

        return  ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancela empréstimo",
            description = "Cancela um emprestimo, alterando seu status para Cancelado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empréstimo cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de regra de negócio")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<LoanResponseDTO> cancelLoan(@PathVariable UUID id) {
        Loan loan = loanService.cancelLoan(id);
        LoanResponseDTO response = LoanResponseDTO.toDto(loan);

        return  ResponseEntity.ok(response);
    }
}
