package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.PricingRuleCode;
import java.math.BigDecimal;

public interface PricingStrategy {

    PricingRuleCode ruleCode();

    BigDecimal baseSpread();
}
