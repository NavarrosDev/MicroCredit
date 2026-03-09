package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Page<Client> findAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Client getClientByCpf(String cpf) {
        return clientRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente inexistente."));
    }

    public Client createClient(String cpf, String name, BigDecimal monthlyIncome) {
        boolean existClient = clientRepository.existsByCpf(cpf);
        if (existClient) {
            throw new IllegalArgumentException("Cliente já existe.");
        }

        Client client = Client.builder()
                .cpf(cpf)
                .name(name)
                .monthlyIncome(monthlyIncome)
                .build();

        return clientRepository.save(client);
    }

    public void deleClient(String cpf) {
        Client client = clientRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente inexistente.") );

        client.getLoans().forEach(loan -> {
            if (loan.getStateLoan() == StateLoan.APPROVED) {
                throw new IllegalArgumentException("Cliente possui empréstimos vinculados.");
            }
        });

        clientRepository.delete(client);
    }
}
