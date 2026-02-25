package com.navarro.microcredit.service.strategy.calculations;

import com.navarro.microcredit.service.strategy.InterestCalculatorStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LowRiskInterestStrategy implements InterestCalculatorStrategy {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.02");

    @Override
    public BigDecimal calculate(BigDecimal requestAmount, int installments) {
        BigDecimal totalInterest = requestAmount
                .multiply(INTEREST_RATE)
                .multiply(BigDecimal.valueOf(installments));

        return requestAmount.add(totalInterest);
    }
}
