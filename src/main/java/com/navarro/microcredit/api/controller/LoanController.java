package com.navarro.microcredit.api.controller;

import com.navarro.microcredit.api.dto.LoanRequestDTO;
import com.navarro.microcredit.api.dto.LoanResponseDTO;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.service.LoanService;
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

    @PostMapping
    public ResponseEntity<LoanResponseDTO> loan(@RequestBody @Valid LoanRequestDTO request) {
        Loan loan = loanService.requestLoan(
                request.clientId(),
                request.requestedAmount(),
                request.installments()
        );

        LoanResponseDTO response = new LoanResponseDTO(
                loan.getId(),
                loan.getTotalValueIncludingInterest(),
                loan.getStateLoan().getTitle(),
                loan.getStateLoan().getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
