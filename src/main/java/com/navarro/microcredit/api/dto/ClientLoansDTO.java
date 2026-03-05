package com.navarro.microcredit.api.dto;

import com.navarro.microcredit.domain.entity.Client;

import java.util.List;

public record ClientLoansDTO(
        ClientDTO client,
        List<LoanResponseDTO> loans
) {
    public static ClientLoansDTO toDto(Client client) {

        ClientDTO clientDTO = new ClientDTO(
                client.getId(),
                client.getCpf(),
                client.getName(),
                client.getMonthlyIncome()
        );

        List<LoanResponseDTO> loansDTO = client.getLoans().stream()
                .map(LoanResponseDTO::toDto)
                .toList();

        return new ClientLoansDTO(clientDTO, loansDTO);
    }
}
