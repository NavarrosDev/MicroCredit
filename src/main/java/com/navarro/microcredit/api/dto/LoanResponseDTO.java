package com.navarro.microcredit.api.dto;

import com.navarro.microcredit.domain.entity.Loan;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanResponseDTO(
        UUID loanId,
        BigDecimal totalWithInterest,
        String status,
        String message
) {
    public static LoanResponseDTO toDto(Loan loan) {
        return new LoanResponseDTO(
                loan.getId(),
                loan.getTotalValueIncludingInterest(),
                loan.getStateLoan().getTitle(),
                loan.getStateLoan().getDescription()
        );
    }
}
