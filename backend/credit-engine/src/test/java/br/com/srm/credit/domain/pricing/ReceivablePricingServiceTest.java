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
        var request = CreditPricingRequest.of(
                "OP-001",
                "TRADE_RECEIVABLE",
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("1.00000000"));

        var result = service.price(request);

        assertThat(result.operationReference()).isEqualTo("OP-001");
        assertThat(result.receivablePricingRuleCode()).isEqualTo("TRADE_RECEIVABLE");
        assertThat(result.appliedSpread()).isEqualByComparingTo("0.0150");
        assertThat(result.discountedAmount()).isEqualByComparingTo("966.18");
        assertThat(result.netAmount()).isEqualByComparingTo("966.18");
        assertThat(result.crossCurrency()).isFalse();
    }

    @Test
    void shouldPricePostDatedCheckInCrossCurrency() {
        var request = CreditPricingRequest.of(
                "OP-002",
                "POST_DATED_CHECK",
                "BRL",
                "USD",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("5.00000000"));

        var result = service.price(request);

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
        var request = CreditPricingRequest.of(
                "OP-003",
                "POST_DATED_CHECK",
                "BRL",
                "BRL",
                new BigDecimal("1000.00"),
                new BigDecimal("0.0200"),
                30,
                new BigDecimal("1.00000000"));

        assertThatThrownBy(() -> serviceWithoutPostDatedCheckStrategy.price(request))
                .isInstanceOf(PricingBusinessException.class)
                .hasMessage(PricingMessage.PRICING_RULE_NOT_FOUND.message());
    }

    @Test
    void shouldFailWhenRequestIsNull() {
        assertThatThrownBy(() -> service.price(null))
                .isInstanceOf(PricingValidationException.class)
                .hasMessage(PricingMessage.PRICING_REQUEST_INVALID.message());
    }
}
