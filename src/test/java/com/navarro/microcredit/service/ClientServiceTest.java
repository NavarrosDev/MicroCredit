package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService - Testes Unitários")
class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve retornar uma página de clientes com sucesso")
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
}