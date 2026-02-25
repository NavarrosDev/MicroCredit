package com.navarro.microcredit.service;

import com.navarro.microcredit.domain.entity.Client;
import com.navarro.microcredit.domain.entity.Loan;
import com.navarro.microcredit.domain.enums.StateLoan;
import com.navarro.microcredit.infraestructure.external.serasa.SerasaMockService;
import com.navarro.microcredit.infraestructure.repository.ClientRepository;
import com.navarro.microcredit.infraestructure.repository.LoanRepository;
import com.navarro.microcredit.service.strategy.InterestCalculatorStrategy;
import com.navarro.microcredit.service.strategy.calculations.HighRiskInterestStrategy;
import com.navarro.microcredit.service.strategy.calculations.LowRiskInterestStrategy;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LoanService {

    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;
    private final SerasaMockService serasaMockService;

    private final LowRiskInterestStrategy lowRiskStrategy;
    private final HighRiskInterestStrategy highRiskStrategy;

    @Transactional
    public Loan requestLoan(UUID clientId, BigDecimal requestedAmount, int installments) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

        if (!serasaMockService.isEligible(client.getCpf())) {
            throw new IllegalArgumentException("Empréstimo negado: Cliente possui restrições no Serasa.");
        }

        InterestCalculatorStrategy strategy = chooseStrategyBasedOnIncome(client.getMonthlyIncome());
        BigDecimal totalValue = strategy.calculate(requestedAmount, installments);

        BigDecimal installmentValue = totalValue.divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_UP);
        BigDecimal maxAllowedInstallment = client.getMonthlyIncome().multiply(new BigDecimal("0.30"));

        if (installmentValue.compareTo(maxAllowedInstallment) > 0) {
            throw new IllegalArgumentException("Empréstimo negado: O valor da parcela compromete mais de 30% da renda do cliente.");
        }

        Loan loan = new Loan();
        loan.setClient(client);
        loan.setRequestedAmount(requestedAmount);
        loan.setTotalValueIncludingInterest(totalValue);
        loan.setNumberOfInstallments(installments);
        loan.setStateLoan(StateLoan.APPROVED);

        return loanRepository.save(loan);
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
