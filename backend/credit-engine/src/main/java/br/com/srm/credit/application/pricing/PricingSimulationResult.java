package br.com.srm.credit.application.pricing;

import java.math.BigDecimal;

public record PricingSimulationResult(
        String operationReference,
        String receivablePricingRuleCode,
        BigDecimal faceAmount,
        BigDecimal baseTaxRate,
        BigDecimal appliedSpread,
        int termDays,
        BigDecimal discountedAmount,
        BigDecimal exchangeRate,
        BigDecimal netAmount,
        boolean crossCurrency) {}
