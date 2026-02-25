package com.navarro.microcredit.service.strategy.calculations;

import com.navarro.microcredit.service.strategy.InterestCalculatorStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HighRiskInterestStrategy implements InterestCalculatorStrategy {
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.10");

    @Override
    public BigDecimal calculate(BigDecimal requestAmount, int installments) {
        BigDecimal multiplier = BigDecimal.ONE.add(INTEREST_RATE).pow(installments);
        BigDecimal totalValue = requestAmount.multiply(multiplier);

        return totalValue.setScale(2, RoundingMode.HALF_UP);
    }
}
