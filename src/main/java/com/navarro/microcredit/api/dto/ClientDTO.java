package com.navarro.microcredit.api.dto;

import com.navarro.microcredit.domain.entity.Client;

import java.math.BigDecimal;
import java.util.UUID;

public record ClientDTO(
        UUID id,
        String cpf,
        String name,
        BigDecimal monthlyIncome
) {
    public static ClientDTO toDto(Client client) {
        return new ClientDTO(
                client.getId(),
                client.getCpf(),
                client.getName(),
                client.getMonthlyIncome()
        );
    }
}
