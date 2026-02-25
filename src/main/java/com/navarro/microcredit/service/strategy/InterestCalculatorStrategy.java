package com.navarro.microcredit.service.strategy;

import java.math.BigDecimal;

public interface InterestCalculatorStrategy {
    BigDecimal calculate(BigDecimal requestAmount, int installments);
}
