package br.com.srm.credit.application.pricing;

public record PricingSimulationCommand(
        String operationReference,
        String receivableTypeCode,
        String faceCurrencyCode,
        String paymentCurrencyCode,
        java.math.BigDecimal faceAmount,
        java.math.BigDecimal baseTaxRate,
        int termDays) {}
