package com.navarro.microcredit.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        UUID clientId,

        @NotNull(message = "O valor solicitado é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal requestedAmount,

        @NotNull(message = "A quantidade de parcelas é obrigatória")
        @Min(value = 1, message = "O número mínimo de parcelas é 1")
        Integer installments
) { }
