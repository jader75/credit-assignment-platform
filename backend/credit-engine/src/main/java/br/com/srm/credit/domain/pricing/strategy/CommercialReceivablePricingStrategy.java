package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.PricingRuleCode;
import java.math.BigDecimal;

public class CommercialReceivablePricingStrategy implements PricingStrategy {
    private static final BigDecimal BASE_SPREAD = new BigDecimal("0.0150");

    @Override
    public PricingRuleCode ruleCode() {
        return PricingRuleCode.TRADE_RECEIVABLE;
    }

    @Override
    public BigDecimal baseSpread() {
        return BASE_SPREAD;
    }
}
