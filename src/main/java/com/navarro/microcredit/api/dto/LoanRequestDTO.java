package com.navarro.microcredit.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanRequestDTO(
        @NotNull UUID clientId,
        @Positive BigDecimal requestedAmount,
        @Min(1) int installments
) { }
