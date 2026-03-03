package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.configuration.rabbitmq.RabbitMQConfig;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import com.navarro.microcredit.service.strategy.calculations.HighRiskInterestStrategy;
import com.navarro.microcredit.service.strategy.calculations.LowRiskInterestStrategy;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService - Testes Unitários")
class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock private ClientRepository clientRepository;
    @Mock private LoanRepository loanRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private LowRiskInterestStrategy lowRiskStrategy;
    @Mock private HighRiskInterestStrategy highRiskStrategy;

    private UUID clientId;
    private BigDecimal requestedAmount;
    private int installments;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        requestedAmount = new BigDecimal("1000");
        installments = 10;
    }

    @Test
    @DisplayName(value = "Erro: Cliente não existe.")
    void shouldThrowExceptionWhenClientDoesNotExist() {
        // Arrange
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> loanService.requestLoan(clientId, requestedAmount, installments));

        verifyNoInteractions(loanRepository);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName(value = "Sucesso: Estratégia de baixo risco.")
    void shouldUseLowRiskStrategyWhenIncomeIsGreaterOrEqualThreshold() {
        // Arrange
        Client client = new Client();
        client.setMonthlyIncome(new BigDecimal("6000"));

        BigDecimal calculateTotal = new BigDecimal("1100");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(lowRiskStrategy.calculate(requestedAmount, installments)).thenReturn(calculateTotal);

        mockLoanSave();

        // Act
        Loan result =  loanService.requestLoan(clientId, requestedAmount, installments);

        // Assert
        assertEquals(calculateTotal, result.getTotalValueIncludingInterest());
        assertEquals(StateLoan.PENDING, result.getStateLoan());

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOAN_REQUEST_QUEUE),
                eq(result.getId().toString())
        );
        verify(lowRiskStrategy).calculate(requestedAmount, installments);
        verify(highRiskStrategy, never()).calculate(any(), anyInt());
    }

    @Test
    @DisplayName(value = "Sucesso: Estratégia de alto risco.")
    void shouldUseHighRiskStrategyWhenIncomeIsGreaterOrEqualThreshold() {
        // Arrange
        Client client = new Client();
        client.setMonthlyIncome(new BigDecimal("3000"));

        BigDecimal calculateTotal = new BigDecimal("1300");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(highRiskStrategy.calculate(requestedAmount, installments)).thenReturn(calculateTotal);

        mockLoanSave();

        // Act
        Loan result =  loanService.requestLoan(clientId, requestedAmount, installments);

        // Assert
        assertEquals(calculateTotal, result.getTotalValueIncludingInterest());
        assertEquals(StateLoan.PENDING, result.getStateLoan());

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.LOAN_REQUEST_QUEUE),
                eq(result.getId().toString())
        );
        verify(highRiskStrategy).calculate(requestedAmount, installments);
        verify(lowRiskStrategy, never()).calculate(any(), anyInt());
    }

    private void mockLoanSave() {
        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> {
                    Loan loan = invocation.getArgument(0);
                    loan.setId(UUID.randomUUID());
                    return loan;
                });
    }
}