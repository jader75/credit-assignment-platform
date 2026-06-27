package br.com.srm.credit.domain.pricing.strategy;

import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import br.com.srm.credit.domain.pricing.PricingBusinessException;
import br.com.srm.credit.domain.pricing.PricingMessage;
import br.com.srm.credit.domain.pricing.PricingRuleCode;
import java.util.List;

public class PricingStrategyResolver {

    private final List<PricingStrategy> strategies;

    public PricingStrategyResolver(List<PricingStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public PricingStrategy resolve(PricingRuleCode ruleCode) {
        requireNonNull(ruleCode, () -> new PricingBusinessException(PricingMessage.PRICING_RULE_CODE_INVALID));
        return strategies.stream()
                .filter(strategy -> strategy.ruleCode().equals(ruleCode))
                .findFirst()
                .orElseThrow(() -> new PricingBusinessException(PricingMessage.PRICING_RULE_NOT_FOUND));
    }
}
