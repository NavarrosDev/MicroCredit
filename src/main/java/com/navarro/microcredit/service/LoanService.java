package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.configuration.rabbitmq.RabbitMQConfig;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import com.navarro.microcredit.service.strategy.InterestCalculatorStrategy;
import com.navarro.microcredit.service.strategy.calculations.HighRiskInterestStrategy;
import com.navarro.microcredit.service.strategy.calculations.LowRiskInterestStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;
    private final RabbitTemplate rabbitTemplate;

    private final LowRiskInterestStrategy lowRiskStrategy;
    private final HighRiskInterestStrategy highRiskStrategy;

    @Transactional
    public Loan requestLoan(UUID clientId, BigDecimal requestedAmount, int installments) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

        InterestCalculatorStrategy strategy = chooseStrategyBasedOnIncome(client.getMonthlyIncome());
        BigDecimal totalValue = strategy.calculate(requestedAmount, installments);

        Loan loan = new Loan();
        loan.setClient(client);
        loan.setRequestedAmount(requestedAmount);
        loan.setTotalValueIncludingInterest(totalValue);
        loan.setNumberOfInstallments(installments);
        loan.setStateLoan(StateLoan.PENDING);

        Loan savedLoan = loanRepository.save(loan);
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_REQUEST_QUEUE, savedLoan.getId().toString());

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
