package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.domain.event.LoanCreateEvent;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import com.navarro.microcredit.service.strategy.calculations.HighRiskInterestStrategy;
import com.navarro.microcredit.service.strategy.calculations.LowRiskInterestStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private LowRiskInterestStrategy lowRiskStrategy;
    @Mock private HighRiskInterestStrategy highRiskStrategy;

    private Loan loan;
    private UUID clientId;
    private BigDecimal requestedAmount;
    private int installments;

    @BeforeEach
    void setUp() {
        loan = new Loan(
                UUID.randomUUID(),
                new Client(),
                new BigDecimal(1000),
                new BigDecimal(1300),
                5,
                StateLoan.APPROVED
        );
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
        verifyNoInteractions(eventPublisher);
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

        verify(eventPublisher).publishEvent(any(LoanCreateEvent.class));
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

        verify(eventPublisher).publishEvent(any(LoanCreateEvent.class));
        verify(highRiskStrategy).calculate(requestedAmount, installments);
        verify(lowRiskStrategy, never()).calculate(any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando tentar quitar um empréstimo inexistente")
    void shouldThrowExceptionWhenLoanNotFoundInPayLoan() {
        // 1. Arrange
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // 2 & 3. Act & Assert
        assertThrows(IllegalArgumentException.class, () -> loanService.payLoan(loanId));

        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName(value = "Deve quitar o empréstimo com sucesso")
    void shouldSuccessfullyPayLoan() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan result =  loanService.payLoan(loanId);

        assertEquals(StateLoan.RAID, result.getStateLoan());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando tentar cancelar um empréstimo inexistente")
    void shouldThrowExceptionWhenLoanNotFoundInCancelLoan() {
        // 1. Arrange
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // 2 & 3. Act & Assert
        assertThrows(IllegalArgumentException.class, () -> loanService.cancelLoan(loanId));

        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName(value = "Deve cancelar o empréstimo com sucesso")
    void shouldSuccessfullyCancelLoan() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan result =  loanService.cancelLoan(loanId);

        assertEquals(StateLoan.CANCELED, result.getStateLoan());
    }

    @Test
    @DisplayName(value = "Deve disparar erro ao tentar cancelar ou quitar um empréstimo com estatus de 'APROVED'")
    void shouldSThrowExceptionWhenStateLoanIsNotApproved() {
        UUID loanId = UUID.randomUUID();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(new Loan()));

        var res = assertThrows(IllegalStateException.class, () -> loanService.cancelLoan(loanId));

        assertEquals("Empréstimo precisa ser aprovado para que a situação seja alterada.", res.getMessage());
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