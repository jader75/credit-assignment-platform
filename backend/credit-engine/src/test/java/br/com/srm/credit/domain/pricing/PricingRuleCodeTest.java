package br.com.srm.credit.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PricingRuleCodeTest {

    @Test
    void shouldResolveKnownCodesAndRejectUnknownOnes() {
        assertThat(PricingRuleCode.fromCode("trade_receivable")).isEqualTo(PricingRuleCode.TRADE_RECEIVABLE);
        assertThat(PricingRuleCode.fromCode("POST_DATED_CHECK")).isEqualTo(PricingRuleCode.POST_DATED_CHECK);
        assertThatThrownBy(() -> PricingRuleCode.fromCode("missing"))
                .isInstanceOf(PricingBusinessException.class)
                .hasMessage(PricingMessage.PRICING_RULE_NOT_FOUND.message());
    }
}
