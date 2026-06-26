package br.com.srm.credit.domain.pricing;

import java.math.BigDecimal;

public record ReceivableTypePricingProfile(
        String code, String pricingRuleCode, BigDecimal baseSpread, boolean active) {}
