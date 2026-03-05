package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.domain.event.LoanCreateEvent;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import com.navarro.microcredit.service.strategy.InterestCalculatorStrategy;
import com.navarro.microcredit.service.strategy.calculations.HighRiskInterestStrategy;
import com.navarro.microcredit.service.strategy.calculations.LowRiskInterestStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final LowRiskInterestStrategy lowRiskStrategy;
    private final HighRiskInterestStrategy highRiskStrategy;

    @Transactional
    public Loan requestLoan(UUID clientId, BigDecimal requestedAmount, int installments) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

        InterestCalculatorStrategy strategy = chooseStrategyBasedOnIncome(client.getMonthlyIncome());
        BigDecimal totalValue = strategy.calculate(requestedAmount, installments);

        Loan loan = Loan.builder()
                .client(client)
                .requestedAmount(requestedAmount)
                .totalValueIncludingInterest(totalValue)
                .numberOfInstallments(installments)
                .stateLoan(StateLoan.PENDING)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        eventPublisher.publishEvent(new LoanCreateEvent(savedLoan.getId()));

        return savedLoan;
    }

    private InterestCalculatorStrategy chooseStrategyBasedOnIncome(BigDecimal monthlyIncome) {
        BigDecimal incomeThreshold = new BigDecimal("5000.00");

        if (monthlyIncome.compareTo(incomeThreshold) >= 0) {
            return lowRiskStrategy;
        } else {
            return highRiskStrategy;
        }
    }
}
