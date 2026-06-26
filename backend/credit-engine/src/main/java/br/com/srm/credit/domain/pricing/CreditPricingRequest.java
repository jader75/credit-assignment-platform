package br.com.srm.credit.domain.pricing;

import java.math.BigDecimal;

public record CreditPricingRequest(
        String operationReference,
        String receivablePricingRuleCode,
        String faceCurrencyCode,
        String paymentCurrencyCode,
        BigDecimal faceAmount,
        BigDecimal baseTaxRate,
        int termDays,
        BigDecimal exchangeRate) {}
