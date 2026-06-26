package br.com.srm.credit.domain.pricing;

import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import java.math.BigDecimal;

public record ReceivableTypePricingProfile(
        String code, PricingRuleCode pricingRuleCode, BigDecimal baseSpread, boolean active) {

    public ReceivableTypePricingProfile {
        requireNonNull(code, () -> new PricingValidationException(PricingMessage.RECEIVABLE_TYPE_CODE_INVALID));
        requireNonNull(pricingRuleCode, () -> new PricingValidationException(PricingMessage.PRICING_RULE_CODE_INVALID));
        requireNonNull(baseSpread, () -> new PricingValidationException(PricingMessage.BASE_SPREAD_INVALID));
        if (baseSpread.signum() < 0) {
            throw new PricingValidationException(PricingMessage.BASE_SPREAD_INVALID);
        }
    }

    public static ReceivableTypePricingProfile of(
            String code, String pricingRuleCode, BigDecimal baseSpread, boolean active) {
        return new ReceivableTypePricingProfile(code, PricingRuleCode.fromCode(pricingRuleCode), baseSpread, active);
    }
}
