package com.navarro.microcredit.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ClientDTO(
        UUID id,
        String name,
        String cpf,
        BigDecimal monthlyIncome
) { }
