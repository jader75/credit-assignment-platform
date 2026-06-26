package br.com.srm.credit.domain.pricing;

import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import java.math.BigDecimal;

public record CreditPricingRequest(
        String operationReference,
        PricingRuleCode receivablePricingRuleCode,
        String faceCurrencyCode,
        String paymentCurrencyCode,
        BigDecimal faceAmount,
        BigDecimal baseTaxRate,
        int termDays,
        BigDecimal exchangeRate) {

    public CreditPricingRequest {
        requireNonNull(
                operationReference, () -> new PricingValidationException(PricingMessage.OPERATION_REFERENCE_INVALID));
        requireNonNull(
                receivablePricingRuleCode,
                () -> new PricingValidationException(PricingMessage.PRICING_RULE_CODE_INVALID));
        requireNonNull(faceCurrencyCode, () -> new PricingValidationException(PricingMessage.FACE_CURRENCY_INVALID));
        requireNonNull(
                paymentCurrencyCode, () -> new PricingValidationException(PricingMessage.PAYMENT_CURRENCY_INVALID));
        requireNonNull(faceAmount, () -> new PricingValidationException(PricingMessage.FACE_AMOUNT_INVALID));
        requireNonNull(baseTaxRate, () -> new PricingValidationException(PricingMessage.BASE_TAX_RATE_INVALID));
        requireNonNull(exchangeRate, () -> new PricingValidationException(PricingMessage.EXCHANGE_RATE_INVALID));
        if (faceAmount.signum() <= 0) {
            throw new PricingValidationException(PricingMessage.FACE_AMOUNT_INVALID);
        }
        if (baseTaxRate.signum() < 0) {
            throw new PricingValidationException(PricingMessage.BASE_TAX_RATE_INVALID);
        }
        if (termDays < 0) {
            throw new PricingValidationException(PricingMessage.TERM_DAYS_INVALID);
        }
        if (exchangeRate.signum() <= 0) {
            throw new PricingValidationException(PricingMessage.EXCHANGE_RATE_INVALID);
        }
    }

    public static CreditPricingRequest of(
            String operationReference,
            String receivablePricingRuleCode,
            String faceCurrencyCode,
            String paymentCurrencyCode,
            BigDecimal faceAmount,
            BigDecimal baseTaxRate,
            int termDays,
            BigDecimal exchangeRate) {
        return new CreditPricingRequest(
                operationReference,
                PricingRuleCode.fromCode(receivablePricingRuleCode),
                faceCurrencyCode,
                paymentCurrencyCode,
                faceAmount,
                baseTaxRate,
                termDays,
                exchangeRate);
    }
}
