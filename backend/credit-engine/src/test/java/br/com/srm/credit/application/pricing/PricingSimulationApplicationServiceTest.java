package br.com.srm.credit.application.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.srm.credit.domain.pricing.ReceivablePricingService;
import br.com.srm.credit.domain.pricing.strategy.CommercialReceivablePricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PostDatedCheckPricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingSimulationApplicationServiceTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final PricingSimulationApplicationService applicationService = new PricingSimulationApplicationService(
            new ReceivablePricingService(new PricingStrategyResolver(
                    List.of(new CommercialReceivablePricingStrategy(), new PostDatedCheckPricingStrategy()))),
            meterRegistry);

    @Test
    void shouldSimulateCommercialReceivablePricing() {
        var command = new PricingSimulationCommand(
                "OP-001",
                "TRADE_RECEIVABLE",
                br.com.srm.credit.domain.pricing.PricingRuleCode.TRADE_RECEIVABLE,
                new BigDecimal("0.0150"),
                true,
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("1.00000000"));

        var result = applicationService.simulate(command);

        assertThat(result.operationReference()).isEqualTo("OP-001");
        assertThat(result.receivablePricingRuleCode()).isEqualTo("TRADE_RECEIVABLE");
        assertThat(result.discountedAmount()).isEqualByComparingTo("966.18");
        assertThat(result.netAmount()).isEqualByComparingTo("966.18");
        assertThat(result.crossCurrency()).isFalse();
        assertThat(meterRegistry
                        .get("credit.pricing.simulation.requests")
                        .tag("outcome", "success")
                        .counter()
                        .count())
                .isEqualTo(1.0d);
    }
}
