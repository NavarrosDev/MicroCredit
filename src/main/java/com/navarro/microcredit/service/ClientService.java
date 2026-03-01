package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;

    public Client createClient(String cpf, String name, BigDecimal monthlyIncome) {
        boolean existClient = clientRepository.existsByCpf(cpf);
        if (existClient) {
            throw new IllegalArgumentException("Cliente já existe.");
        }

        Client client = new Client();
        client.setCpf(cpf);
        client.setName(name);
        client.setMonthlyIncome(monthlyIncome);
        return clientRepository.save(client);
    }

    public void deleClient(String cpf) {
        Client client = clientRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente inexistente.") );

        if (loanRepository.existsByClientId(client.getId())) {
            throw new IllegalArgumentException("Cliente possui empréstimos vinculados.");
        }

        clientRepository.delete(client);
    }
}
