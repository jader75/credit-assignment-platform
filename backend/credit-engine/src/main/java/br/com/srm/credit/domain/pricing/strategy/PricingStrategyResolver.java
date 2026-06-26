package br.com.srm.credit.domain.pricing.strategy;

import static br.com.srm.credit.domain.shared.DomainValidation.requireNonNull;

import br.com.srm.credit.domain.pricing.PricingBusinessException;
import br.com.srm.credit.domain.pricing.PricingMessage;
import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import java.util.List;

public class PricingStrategyResolver {

    private final List<PricingStrategy> strategies;

    public PricingStrategyResolver(List<PricingStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public PricingStrategy resolve(ReceivableTypePricingProfile receivableType) {
        requireNonNull(receivableType, () -> new PricingBusinessException(PricingMessage.RECEIVABLE_TYPE_INVALID));

        return strategies.stream()
                .filter(strategy -> strategy.ruleCode().equals(receivableType.pricingRuleCode()))
                .findFirst()
                .orElseThrow(() -> new PricingBusinessException(PricingMessage.PRICING_RULE_NOT_FOUND));
    }
}
