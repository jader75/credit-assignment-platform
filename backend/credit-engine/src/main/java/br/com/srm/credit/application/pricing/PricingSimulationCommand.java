package br.com.srm.credit.application.pricing;

import br.com.srm.credit.domain.pricing.PricingRuleCode;
import java.math.BigDecimal;

public record PricingSimulationCommand(
        String operationReference,
        String receivableTypeCode,
        PricingRuleCode receivablePricingRuleCode,
        BigDecimal receivableTypeBaseSpread,
        boolean receivableTypeActive,
        String faceCurrencyCode,
        String paymentCurrencyCode,
        BigDecimal faceAmount,
        BigDecimal baseTaxRate,
        int termDays,
        BigDecimal exchangeRate) {}
