package com.navarro.microcredit.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

public record ClientRequestDTO(
        @NotNull(message = "O valor solicitado é obrigatório")
        @CPF
        String cpf,

        @NotNull(message = "O valor solicitado é obrigatório")
        String name,

        @NotNull(message = "O valor solicitado é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal monthlyIncome
) { }
