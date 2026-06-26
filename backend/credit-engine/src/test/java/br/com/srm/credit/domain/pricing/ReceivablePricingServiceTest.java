package br.com.srm.credit.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.srm.credit.domain.pricing.strategy.CommercialReceivablePricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PostDatedCheckPricingStrategy;
import br.com.srm.credit.domain.pricing.strategy.PricingStrategyResolver;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReceivablePricingServiceTest {

    private final ReceivablePricingService service = new ReceivablePricingService(new PricingStrategyResolver(
            List.of(new CommercialReceivablePricingStrategy(), new PostDatedCheckPricingStrategy())));

    @Test
    void shouldPriceCommercialReceivableInSameCurrency() {
        var receivableType = new ReceivableTypePricingProfile(
                "TRADE_RECEIVABLE", "COMMERCIAL_RECEIVABLE", new BigDecimal("0.0150"), true);
        var request = new CreditPricingRequest(
                "OP-001",
                "TRADE_RECEIVABLE",
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("1.00000000"));

        var result = service.price(request, receivableType);

        assertThat(result.operationReference()).isEqualTo("OP-001");
        assertThat(result.receivablePricingRuleCode()).isEqualTo("COMMERCIAL_RECEIVABLE");
        assertThat(result.appliedSpread()).isEqualByComparingTo("0.0150");
        assertThat(result.discountedAmount()).isEqualByComparingTo("966.18");
        assertThat(result.netAmount()).isEqualByComparingTo("966.18");
        assertThat(result.crossCurrency()).isFalse();
    }

    @Test
    void shouldPricePostDatedCheckInCrossCurrency() {
        var receivableType = new ReceivableTypePricingProfile(
                "POST_DATED_CHECK", "POST_DATED_CHECK", new BigDecimal("0.0250"), true);
        var request = new CreditPricingRequest(
                "OP-002",
                "POST_DATED_CHECK",
                "BRL",
                "USD",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("5.00000000"));

        var result = service.price(request, receivableType);

        assertThat(result.receivablePricingRuleCode()).isEqualTo("POST_DATED_CHECK");
        assertThat(result.appliedSpread()).isEqualByComparingTo("0.0250");
        assertThat(result.discountedAmount()).isEqualByComparingTo("956.94");
        assertThat(result.netAmount()).isEqualByComparingTo("4784.69");
        assertThat(result.crossCurrency()).isTrue();
    }

    @Test
    void shouldFailWhenNoStrategyExistsForRuleCode() {
        var resolver = new PricingStrategyResolver(List.of(new CommercialReceivablePricingStrategy()));
        var serviceWithoutPostDatedCheckStrategy = new ReceivablePricingService(resolver);
        var receivableType = new ReceivableTypePricingProfile(
                "POST_DATED_CHECK", "POST_DATED_CHECK", new BigDecimal("0.0250"), true);
        var request = new CreditPricingRequest(
                "OP-003",
                "POST_DATED_CHECK",
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("1.00000000"));

        assertThatThrownBy(() -> serviceWithoutPostDatedCheckStrategy.price(request, receivableType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No pricing strategy found");
    }
}
