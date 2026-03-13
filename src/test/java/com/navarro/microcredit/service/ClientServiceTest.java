package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Testes Unitários")
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock private ClientRepository clientRepository;

    private Client client;
    private Loan loan;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(UUID.randomUUID())
                .cpf("46067314790")
                .name("Cliente 1")
                .monthlyIncome(new BigDecimal("1500"))
                .build();

        loan = new Loan();
    }

    @Test
    @DisplayName(value = "Deve retornar uma página de clientes com sucesso")
    void shouldReturnPageOfClients() {
        // 1. Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Client client1 = new Client();
        client1.setId(UUID.randomUUID());
        Client client2 = new Client();
        client2.setId(UUID.randomUUID());

        List<Client> clientList = List.of(client1, client2);
        Page<Client> mockPage = new PageImpl<>(clientList, pageable, clientList.size());

        when(clientRepository.findAll(pageable)).thenReturn(mockPage);

        // 2. Act
        Page<Client> result = clientService.findAllClients(pageable);

        // 3. Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(clientRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName(value = "Deve retornar cliente de acordo com CPF inserido.")
    void shouldReturnClientByCpf() {
        Client client = new Client();
        client.setCpf("46067314790");
        client.setId(UUID.randomUUID());

        when(clientRepository.findByCpf(client.getCpf())).thenReturn(Optional.of(client));

        Client res = clientService.getClientByCpf(client.getCpf());

        assertNotNull(res);
        assertEquals(client, res);
    }

    @Test
    @DisplayName(value = "Deve retornar erro após incerir cpf que nao existe na base.")
    void shouldThrowExceptionWhenSearchClientByCpf() {
        String cpf = "46067314790";
        when(clientRepository.findByCpf(cpf)).thenReturn(Optional.empty());

        var res = assertThrows(IllegalArgumentException.class, () -> clientService.getClientByCpf(cpf));

        assertEquals("Cliente inexistente.", res.getMessage());
    }

    @Test
    @DisplayName(value = "Deve criar cliente com sucesso.")
    void shouldCreateClientWithSuccess() {
        when(clientRepository.existsByCpf(client.getCpf())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client res = clientService.createClient(
                client.getCpf(),
                client.getName(),
                client.getMonthlyIncome()
        );

        assertNotNull(res);
        assertEquals(client.getCpf(), res.getCpf());
        assertEquals(client.getName(), res.getName());

        verify(clientRepository, times(1)).existsByCpf(client.getCpf());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName(value = "Deve retornar erro ao tentar criar cliente.")
    void shouldCreateClientWithError() {
        when(clientRepository.existsByCpf(client.getCpf())).thenReturn(true);

        var res =  assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(
                        client.getCpf(),
                        client.getName(),
                        client.getMonthlyIncome()
                ));

        assertEquals("Cliente já existe.", res.getMessage());

        verify(clientRepository, times(1)).existsByCpf(client.getCpf());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar um cliente inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentClient() {
        when(clientRepository.findByCpf(client.getCpf())).thenReturn(Optional.empty());

        var response = assertThrows(IllegalArgumentException.class,
                () -> clientService.deleClient(client.getCpf()));

        assertEquals("Cliente inexistente.", response.getMessage());

        verify(clientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar cliente com empréstimo aprovado")
    void shouldThrowExceptionWhenClientHasApprovedLoans() {
        loan.setStateLoan(StateLoan.APPROVED);
        client.setLoans(List.of(loan));

        when(clientRepository.findByCpf(client.getCpf())).thenReturn(Optional.of(client));

        var response = assertThrows(IllegalArgumentException.class,
                () -> clientService.deleClient(client.getCpf()));

        assertEquals("Cliente possui empréstimos vinculados.", response.getMessage());

        verify(clientRepository, times(1)).findByCpf(client.getCpf());
        verify(clientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso quando não houver empréstimos aprovados")
    void shouldDeleteClientWithSuccess() {
        loan.setStateLoan(StateLoan.RAID);
        client.setLoans(List.of(loan));

        when(clientRepository.findByCpf(client.getCpf())).thenReturn(Optional.of(client));

        clientService.deleClient(client.getCpf());

        verify(clientRepository, times(1)).findByCpf(client.getCpf());
        verify(clientRepository, times(1)).delete(client);
    }
}