package com.navarro.microcredit.infraestructure.messaging;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.external.serasa.SerasaMockService;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanProcessorConsumer - Testes Unitários")
class LoanProcessorConsumerTest {

    @InjectMocks
    private LoanProcessorConsumer consumer;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private SerasaMockService serasaMockService;

    private UUID loanId;
    private Loan loan;
    private Client client;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();

        client = Client.builder()
                .cpf("12345678900")
                .name("João")
                .monthlyIncome(new BigDecimal("5000"))
                .build();

        loan = Loan.builder()
                .id(loanId)
                .client(client)
                .numberOfInstallments(10)
                .totalValueIncludingInterest(new BigDecimal("1000"))
                .build();
    }

    @Test
    @DisplayName("Sucesso - Aprovação: Emprestimo aprovado quando cliente é elegivel e o valor da parcela é menor que 30% da renda.")
    void shouldApproveLoanWhenEligibleAndInstallmentWithinLimit() {
        // Arrange
        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        when(serasaMockService.isEligible(client.getCpf()))
                .thenReturn(true);

        // Act
        consumer.processLoan(loanId.toString());

        // Assert
        assertEquals(StateLoan.APPROVED, loan.getStateLoan());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Sucesso - Reprovação: Cliente não é elegivel pelo Serasa.")
    void shouldDenyLoanWhenSerasaRejects() {
        // Arrange
        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        when(serasaMockService.isEligible(client.getCpf()))
                .thenReturn(false);

        // Act
        consumer.processLoan(loanId.toString());

        // Assert
        assertEquals(StateLoan.DENIED, loan.getStateLoan());
        verify(loanRepository).save(loan);
        verify(serasaMockService).isEligible(client.getCpf());
    }

    @Test
    @DisplayName("Sucesso - Reprova: Valor da parcela excede 30% da renda do cliente.")
    void shouldDenyLoanWhenInstallmentExceedsThirtyPercentOfIncome() {
        // Arrange
        loan.setTotalValueIncludingInterest(new BigDecimal("20000"));

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        when(serasaMockService.isEligible(client.getCpf()))
                .thenReturn(true);

        // Act
        consumer.processLoan(loanId.toString());

        // Assert
        assertEquals(StateLoan.DENIED, loan.getStateLoan());
        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Error: Emprestimo não existente.")
    void shouldNotSaveWhenLoanNotFound() {
        // Arrange
        when(loanRepository.findById(loanId))
                .thenReturn(Optional.empty());

        // Act
        consumer.processLoan(loanId.toString());

        // Assert
        verify(loanRepository, never()).save(any());
    }
}