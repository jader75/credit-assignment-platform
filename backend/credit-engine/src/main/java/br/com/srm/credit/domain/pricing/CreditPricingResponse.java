package br.com.srm.credit.domain.pricing;

import java.math.BigDecimal;

public record CreditPricingResponse(
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
