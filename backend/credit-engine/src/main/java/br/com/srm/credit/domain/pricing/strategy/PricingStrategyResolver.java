package br.com.srm.credit.domain.pricing.strategy;

import br.com.srm.credit.domain.pricing.ReceivableTypePricingProfile;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PricingStrategyResolver {

    private final List<PricingStrategy> strategies;

    public PricingStrategyResolver(List<PricingStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public PricingStrategy resolve(ReceivableTypePricingProfile receivableType) {
        Objects.requireNonNull(receivableType, "receivableType must not be null");

        return strategies.stream()
                .filter(strategy -> strategy.ruleCode().equals(receivableType.pricingRuleCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No pricing strategy found for rule code " + receivableType.pricingRuleCode()));
    }
}
