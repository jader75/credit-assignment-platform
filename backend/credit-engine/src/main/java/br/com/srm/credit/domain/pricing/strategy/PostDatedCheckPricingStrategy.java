package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.PricingRuleCode;
import java.math.BigDecimal;

public class PostDatedCheckPricingStrategy implements PricingStrategy {
    private static final BigDecimal BASE_SPREAD = new BigDecimal("0.0250");

    @Override
    public PricingRuleCode ruleCode() {
        return PricingRuleCode.POST_DATED_CHECK;
    }

    @Override
    public BigDecimal baseSpread() {
        return BASE_SPREAD;
    }
}
