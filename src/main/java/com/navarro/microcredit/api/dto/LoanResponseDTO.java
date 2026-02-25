package com.navarro.microcredit.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanResponseDTO(
        UUID loanId,
        BigDecimal totalWithInterest,
        String status,
        String message
) { }
