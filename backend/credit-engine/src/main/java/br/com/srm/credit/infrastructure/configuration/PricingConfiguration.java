package br.com.srm.credit.infrastructure.configuration;

import br.com.srm.credit.application.pricing.PricingSimulationApplicationService;
import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.strategy.CommercialReceivablePricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PostDatedCheckPricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PricingConfiguration {

    @Bean
    public CommercialReceivablePricingStrategy commercialReceivablePricingStrategy() {
        return new CommercialReceivablePricingStrategy();
    }

    @Bean
    public PostDatedCheckPricingStrategy postDatedCheckPricingStrategy() {
        return new PostDatedCheckPricingStrategy();
    }

    @Bean
    public PricingStrategyResolver pricingStrategyResolver(
            CommercialReceivablePricingStrategy commercialReceivablePricingStrategy,
            PostDatedCheckPricingStrategy postDatedCheckPricingStrategy) {
        return new PricingStrategyResolver(
                java.util.List.of(commercialReceivablePricingStrategy, postDatedCheckPricingStrategy));
    }

    @Bean
    public ReceivablePricingService receivablePricingService(PricingStrategyResolver pricingStrategyResolver) {
        return new ReceivablePricingService(pricingStrategyResolver);
    }

    @Bean
    public PricingSimulationApplicationService pricingSimulationApplicationService(
            ReceivablePricingService receivablePricingService, MeterRegistry meterRegistry) {
        return new PricingSimulationApplicationService(receivablePricingService, meterRegistry);
    }
}
